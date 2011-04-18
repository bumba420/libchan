package de.benpicco.libchan.imageboards;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import de.benpicco.libchan.interfaces.BoardHandler;
import de.benpicco.libchan.interfaces.IImageBoardParser;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.interfaces.ThreadHandler;
import de.benpicco.libchan.streamparser.IParseDataReceiver;
import de.benpicco.libchan.streamparser.StreamParser;
import de.benpicco.libchan.util.Tuple;

public class GenericImageBoardParser implements IImageBoardParser, IParseDataReceiver {

	private PostHandler					receiver;

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

	private String absolute(String relUrl) {
		return relUrl.startsWith("/") ? baseUrl + relUrl : relUrl;
	}

	public GenericImageBoardParser(String baseUrl, List<Tags> postStarter, List<Tags> postEnder, List<Tags> imageEnder,
			StreamParser parser, StreamParser boardParser, String threadMark, String imgPrefix, String thumbPrefix,
			String countryPrefix, Tuple<String, String> threadURL) {
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
	}

	@Override
	public void getPosts(String url, PostHandler rec) throws MalformedURLException, IOException {
		int maxtries = 5;
		while (maxtries-- > 0) {

			InputStream in = new BufferedInputStream(new URL(url).openStream());

			receiver = rec;
			try {
				parser.parseStream(in, this);
				rec.onPostsParsingDone();
				return;
			} catch (IOException e) {
				System.out.println("Unable to read " + url + ", retrying…");
			}
		}
		System.err.println("Failed downloading " + url);
		rec.onPostsParsingDone();
	}

	@Override
	public void parsedString(Tags tag, String data) {
		// System.out.println(tag + " - " + data);

		if (currentPost == null)
			if (postStarter.contains(tag))
				currentPost = new Post();
			else
				return;

		switch (tag) {
		case POST_ID:
			currentPost.id = Integer.parseInt(data);
			break;
		case POST_USER:
			currentPost.user = data;
			break;
		case POST_DATE:
			currentPost.date = data;
			break;
		case POST_IMGURL:
			if (currentImage == null)
				currentImage = new Image();
			currentImage.url = absolute(imgPrefix + data.trim());
			break;
		case POST_THUMBNAIL:
			if (currentImage == null)
				currentImage = new Image();
			currentImage.thumbnailUrl = absolute(thumbPrefix + data.trim());
			break;
		case POST_FILENAME:
			if (currentImage == null)
				currentImage = new Image();
			currentImage.filename = data.trim();
			break;
		case POST_COUNTRY:
			currentPost.countryball = absolute(countryPrefix + data);
			break;
		case POST_TITLE:
			currentPost.title = data;
			break;
		case POST_MESSAGE:
			currentPost.message = data;
			break;
		case POST_THREAD:
			currentPost.isFirstPost = threadMark.length() == 0 ? data.trim().length() == 0 : data.contains(threadMark);
			break;
		case NULL:
			break;
		default:
			System.err.println("unhandled case " + tag + ": " + data);
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
			currentPost.cleanup();
			receiver.onAddPost(currentPost);

			currentPost = null;
		}
	}

	@Override
	public void getThreads(String url, ThreadHandler rec) throws IOException {
		getPosts(url, new ThreadParser(url, rec));
	}

	class ThreadParser implements PostHandler {
		private ThreadHandler	rec;
		private String			url;

		public ThreadParser(String url, ThreadHandler rec) {
			this.rec = rec;
			this.url = url;
		}

		@Override
		public void onAddPost(Post post) {
			if (post.isFirstPost)
				rec.onAddThread(new Thread(post, composeUrl(url, post), 0));
		}

		@Override
		public void onPostsParsingDone() {
			rec.onThreadsParsingDone();
		}
	}

	private String getBoard(String url) {
		String board = url.substring(baseUrl.length() + 1);

		int slash = board.indexOf("/");
		if (slash > 0)
			board = board.substring(0, slash);
		return "/" + board + "/";
	}

	public String composeUrl(String url, Post post) {
		return baseUrl + getBoard(url) + threadURL.first + post.op + threadURL.second
				+ (post.isFirstPost ? "" : "#" + post.id);
	}

	public String composeUrl(String url, int post) {
		return baseUrl + getBoard(url) + threadURL.first + post + threadURL.second;
	}

	public String getUrl() {
		return baseUrl;
	}

	@Override
	public void getBoards(final BoardHandler rec) throws IOException {
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
					rec.onAddBoard(tmp);
				}
			}
		};

		InputStream in = new BufferedInputStream(new URL(baseUrl).openStream());
		boardParser.parseStream(in, parseDataReceiver);
		rec.onBoardParsingDone();
	}
}
