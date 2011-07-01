package de.benpicco.libchan.imageboards;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringEscapeUtils;

import de.benpicco.libchan.interfaces.BoardHandler;
import de.benpicco.libchan.interfaces.ImageBoardParser;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.interfaces.ThreadHandler;
import de.benpicco.libchan.streamparser.IParseDataReceiver;
import de.benpicco.libchan.util.Logger;

public class GenericImageBoardParser implements ImageBoardParser, IParseDataReceiver {
	private final ParserOptions	o;
	private final String		baseUrl;

	private PostHandler			postReceiver	= null;
	private ThreadHandler		threadReceiver	= null;
	private BoardHandler		boardReceiver	= null;

	private Post				currentPost		= null;
	private Image				currentImage	= null;
	private Post				firstPost		= null;

	private int					lastPosAbs		= 0;
	private int					lastPos			= 0;
	private int					newLastPos		= 0;
	private int					lastId			= 0;
	private boolean				refreshing;

	private String				url;
	private boolean				isFirstPost;

	private String absolute(String relUrl) {
		return relUrl.startsWith("/") ? baseUrl + relUrl : relUrl;
	}

	public GenericImageBoardParser(String url, String baseUrl, ParserOptions o) {
		this.url = url;
		this.baseUrl = baseUrl;
		this.o = o;
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
				o.parser.parseStream(in, GenericImageBoardParser.this);
				break;
			} catch (IOException e) {
				reset();
				lastIdPre = 0;
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
		if (lastId < lastIdPre || refreshing) {
			Logger.get().println("Deletion detected, refreshing entire page");
			reset();
			getPosts();
		}
		postReceiver.onPostsParsingDone();
	}

	private void reset() {
		lastId = 0;
		lastPos = 0;
		lastPosAbs = 0;
		newLastPos = 0;
		refreshing = false;
	}

	@Override
	public void parsedString(Tags tag, String data) {
		if (currentPost == null)
			if (o.postStarter.contains(tag))
				currentPost = new Post();
			else
				return;

		data = data.trim();

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
				currentImage.url = absolute(o.imgPrefix + data);
			break;
		case POST_THUMBNAIL:
			if (currentImage == null)
				currentImage = new Image();
			if (currentImage.thumbnailUrl == null)
				currentImage.thumbnailUrl = absolute(o.thumbPrefix + data);
			break;
		case POST_FILENAME:
			if (currentImage == null)
				currentImage = new Image();
			if (currentImage.filename == null)
				currentImage.filename = data;
			break;
		case POST_COUNTRY:
			if (currentPost.countryball == null)
				currentPost.countryball = absolute(o.countryPrefix + data);
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
			isFirstPost = o.threadMark.length() == 0 ? data.length() > 0 : data.contains(o.threadMark);
			break;
		case NULL:
			break;
		default:
			Logger.get().error("Warning: unhandled case " + tag + ": " + data);
		}

		if (currentImage != null && (o.imageEnder.contains(tag) || currentImage.isReady())) {
			currentImage.cleanup();
			currentPost.addImage(currentImage);
			currentImage = null;
		}

		if (o.postEnder.contains(tag)) {
			if (isFirstPost || firstPost == null)
				firstPost = currentPost;
			currentPost.op = firstPost.id;
			isFirstPost = false;

			if (refreshing) {
				refreshing = false;
				if (lastId != currentPost.id) { // deletion
					reset();
					o.parser.halt();
				}
				currentPost = null;
			}

			lastPos = newLastPos;
			newLastPos = lastPosAbs + o.parser.getPos();

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
		reset();
		postReceiver = new ThreadParser();
		getPosts();
		postReceiver = oldRecceiver;
	}

	class ThreadParser implements PostHandler {

		@Override
		public void onAddPost(Post post) {
			if (post.isFirstPost())
				threadReceiver.onAddThread(new Thread(post, composeUrl(post), 0));
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
		return baseUrl + getBoard(url) + o.threadURL.first + post + o.threadURL.second;
	}

	public String composeUrl(Post post) {
		return baseUrl + getBoard(url) + o.threadURL.first + post.op + o.threadURL.second
				+ (post.isFirstPost() ? "" : "#" + post.id);
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
				data = data.trim();
				if (data.length() == 0)
					return;

				if (board == null)
					board = new Board();

				switch (tag) {
				case BOARD_URL:
					board.url = data;
					if (board.url.startsWith("/"))
						board.url = getBaseUrl() + board.url;
					break;
				case BOARD_TITLE:
					// filter html and make escaped characters readable again -
					// some boards want to do fancy stuff in their navigation…
					board.name = StringEscapeUtils.unescapeHtml4(data.replaceAll("\\<.*?\\>", "")).trim();
					break;
				default:
					return;
				}

				if (board.name != null && board.url != null) {
					Board tmp = board;
					board = null;
					boardReceiver.onAddBoard(tmp);
				}
			}
		};

		InputStream in = new BufferedInputStream(new URL(baseUrl + o.boardIndex).openStream());
		o.boardParser.parseStream(in, parseDataReceiver);
		boardReceiver.onBoardParsingDone();
	}

	@Override
	public void setPostHandler(PostHandler rec) {
		reset();
		postReceiver = rec;
	}

	@Override
	public void setThreadHandler(ThreadHandler rec) {
		reset();
		threadReceiver = rec;
	}

	@Override
	public void setBoardHandler(BoardHandler rec) {
		reset();
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
