package de.benpicco.libchan.imageboards;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import de.benpicco.libchan.interfaces.BoardHandler;
import de.benpicco.libchan.interfaces.ImageBoardParser;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.interfaces.ThreadHandler;
import de.benpicco.libchan.streamparser.IParseDataReceiver;
import de.benpicco.libchan.streamparser.StreamParser;
import de.benpicco.libchan.util.Logger;
import de.benpicco.libchan.util.Tuple;

public class GenericImageBoardParser implements ImageBoardParser, IParseDataReceiver {

	private PostHandler					postReceiver	= null;
	private ThreadHandler				threadReceiver	= null;
	private BoardHandler				boardReceiver	= null;

	private Post						currentPost		= null;
	private Image						currentImage	= null;
	private Post						firstPost		= null;

	private final String				baseUrl;
	private final List<Tags>			postStarter;
	private final List<Tags>			postEnder;
	private final List<Tags>			imageEnder;

	private final StreamParser			parser;
	private final StreamParser			boardParser;

	private final String				imgPrefix;
	private final String				thumbPrefix;
	private final String				countryPrefix;
	private final Tuple<String, String>	threadURL;
	private final String				threadMark;

	private int							lastPosAbs		= 0;
	private int							lastPos			= 0;
	private int							newLastPos		= 0;
	private int							lastId			= 0;
	private boolean						refreshing;

	private String						url;

	private String absolute(String relUrl) {
		return relUrl.startsWith("/") ? baseUrl + relUrl : relUrl;
	}

	public GenericImageBoardParser(String baseUrl, List<Tags> postStarter, List<Tags> postEnder, List<Tags> imageEnder,
			StreamParser parser, StreamParser boardParser, String threadMark, String imgPrefix, String thumbPrefix,
			String countryPrefix, Tuple<String, String> threadURL, String url) {
		this.baseUrl = baseUrl;
		this.postStarter = postStarter;
		this.postEnder = postEnder;
		this.imageEnder = imageEnder;
		this.parser = parser;
		this.boardParser = boardParser;
		this.imgPrefix = imgPrefix;
		this.thumbPrefix = thumbPrefix;
		this.countryPrefix = countryPrefix;
		this.threadURL = threadURL;
		this.threadMark = threadMark;

		this.url = url;
	}

	@Override
	public synchronized void getPosts() throws MalformedURLException, IOException {
		if (postReceiver == null)
			return;

		int lastIdPre = lastId;
		if (lastId > 0) {
			refreshing = true;
			lastPosAbs = lastPos;
			newLastPos = lastPos;
		}

		int tries = 5;
		while (tries-- > 0) {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setInstanceFollowRedirects(true);
			if (lastPos > 0)
				connection.setRequestProperty("Range", "bytes=" + lastPos + "-");
			InputStream in = new BufferedInputStream(connection.getInputStream());

			try {
				parser.parseStream(in, GenericImageBoardParser.this);
				break;
			} catch (IOException e) {
				if (tries == 0)
					Logger.get().error("Failed downloading " + url + ": " + e);
				else {
					try {
						java.lang.Thread.sleep(500);
					} catch (InterruptedException e2) {
					}
				}
			}
		}
		if (lastId < lastIdPre) {
			Logger.get().println("Deletion detected, refreshing entire page");
			getPosts();
		}
		postReceiver.onPostsParsingDone();
	}

	private void reset() {
		lastId = 0;
		lastPos = 0;
		lastPosAbs = 0;
		newLastPos = 0;
	}

	@Override
	public void parsedString(Tags tag, String data) {
		if (currentPost == null)
			if (postStarter.contains(tag))
				currentPost = new Post();
			else
				return;

		switch (tag) {
		case POST_ID:
			if (currentPost.id == 0)
				currentPost.id = Integer.parseInt(data);
			break;
		case POST_USER:
			if (currentPost.user == null)
				currentPost.user = data;
			break;
		case POST_TRIP:
			if (currentPost.tripcode == null)
				currentPost.tripcode = data;
			break;
		case POST_DATE:
			if (currentPost.date == null)
				currentPost.date = data;
			break;
		case POST_IMGURL:
			if (currentImage == null)
				currentImage = new Image();
			if (currentImage.url == null)
				currentImage.url = absolute(imgPrefix + data.trim());
			break;
		case POST_THUMBNAIL:
			if (currentImage == null)
				currentImage = new Image();
			if (currentImage.thumbnailUrl == null)
				currentImage.thumbnailUrl = absolute(thumbPrefix + data.trim());
			break;
		case POST_FILENAME:
			if (currentImage == null)
				currentImage = new Image();
			if (currentImage.filename == null)
				currentImage.filename = data.trim();
			break;
		case POST_COUNTRY:
			if (currentPost.countryball == null)
				currentPost.countryball = absolute(countryPrefix + data);
			break;
		case POST_TITLE:
			if (currentPost.title == null)
				currentPost.title = data;
			break;
		case POST_MESSAGE:
			if (currentPost.message == null)
				currentPost.message = data;
			break;
		case POST_THREAD:
			if (currentPost.message == null)
				currentPost.isFirstPost = threadMark.length() == 0 ? data.trim().length() == 0 : data
						.contains(threadMark);
			break;
		case NULL:
			break;
		default:
			Logger.get().error("Warning: unhandled case " + tag + ": " + data);
		}

		if (currentImage != null && imageEnder.contains(tag)) {
			currentImage.cleanup();
			currentPost.addImage(currentImage);
			currentImage = null;
		}

		if (postEnder.contains(tag)) {
			if (currentPost.isFirstPost || firstPost == null)
				firstPost = currentPost;
			currentPost.op = firstPost.id;

			if (refreshing) {
				refreshing = false;
				if (lastId > 0 && lastId != currentPost.id) { // deletion
					reset();
					parser.halt();
				}
				currentPost = null;
			}

			lastPos = newLastPos;
			newLastPos = lastPosAbs + parser.getPos();

			if (currentPost == null)
				return;

			lastId = currentPost.id;

			currentPost.cleanup();
			for (Image img : currentPost.images) { // get unique filenames
				if (img.filename == null)
					continue;
				int dot = img.filename.lastIndexOf('.');
				if (dot < 0)
					dot = img.filename.length();
				img.filename = img.filename.substring(0, dot) + "_" + currentPost.id + img.filename.substring(dot);
			}
			postReceiver.onAddPost(currentPost);

			currentPost = null;
		}
	}

	@Override
	public void getThreads() throws IOException {
		if (threadReceiver == null)
			return;

		PostHandler oldRecceiver = postReceiver;
		postReceiver = new ThreadParser();
		getPosts();
		postReceiver = oldRecceiver;
	}

	class ThreadParser implements PostHandler {

		@Override
		public void onAddPost(Post post) {
			if (post.isFirstPost)
				threadReceiver.onAddThread(new Thread(post, composeUrl(post.id), 0));
			// TODO: count replies & parse omittedInfo
		}

		@Override
		public void onPostsParsingDone() {
			threadReceiver.onThreadsParsingDone();
		}
	}

	private String getBoard(String url) {
		String board = url.substring(baseUrl.length() + 1);

		int slash = board.indexOf("/");
		if (slash > 0)
			board = board.substring(0, slash);
		return "/" + board + "/";
	}

	public String composeUrl(int post) {
		return baseUrl + getBoard(url) + threadURL.first + post + threadURL.second;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	@Override
	public synchronized void getBoards() throws IOException {
		if (boardReceiver == null)
			return;

		IParseDataReceiver parseDataReceiver = new IParseDataReceiver() {
			Board	board	= null;

			@Override
			public void parsedString(Tags tag, String data) {
				if (data.length() == 0)
					return;

				boolean finished = board != null;
				if (!finished)
					board = new Board();

				switch (tag) {
				case BOARD_URL:
					board.url = data;
					break;
				case BOARD_TITLE:
					board.name = StringEscapeUtils.unescapeHtml4(data);
					break;
				default:
					return;
				}

				if (finished) {
					Board tmp = board;
					board = null;
					boardReceiver.onAddBoard(tmp);
				}
			}
		};

		InputStream in = new BufferedInputStream(new URL(baseUrl).openStream());
		boardParser.parseStream(in, parseDataReceiver);
		boardReceiver.onBoardParsingDone();
	}

	@Override
	public void setPostHandler(PostHandler rec) {
		postReceiver = rec;
	}

	@Override
	public void setThreadHandler(ThreadHandler rec) {
		threadReceiver = rec;
	}

	@Override
	public void setBoardHandler(BoardHandler rec) {
		boardReceiver = rec;
	}

	public void setUrl(String url) {
		reset();
		this.url = url;
	}

	public String getUrl() {
		return url;
	}
}
