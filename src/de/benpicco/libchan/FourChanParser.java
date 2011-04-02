package de.benpicco.libchan;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import de.benpicco.libchan.streamparser.IParseDataReceiver;
import de.benpicco.libchan.streamparser.StreamParser;

public class FourChanParser implements ImageBoardParser {

	public void parseThread(InputStream responseStream, PostReceiver receiver) {
		new FourChanThreadParser().parseThread(responseStream, receiver);
	}

	public void getThreads(InputStream responseStream, PostReceiver receiver) {
		new FourChanThreadsParser().getThreads(responseStream, receiver);
	}
}

class FourChanThreadParser implements IParseDataReceiver {

	final static int		POST_ID			= 0;
	final static int		POST_USER		= 1;
	final static int		POST_TITLE		= 2;
	final static int		POST_THUMBNAIL	= 3;
	final static int		POST_IMGURL		= 4;
	final static int		POST_FILENAME	= 5;
	final static int		POST_DATE		= 6;
	final static int		POST_MESSAGE	= 7;

	private PostReceiver	receiver;
	private MutablePost		currentPost		= null;
	private boolean			firstpost		= false;

	public void parseThread(InputStream responseStream, PostReceiver receiver) {
		this.receiver = receiver;
		StreamParser parser = new StreamParser(this);
		parser.addTag(POST_ID, "<input type=checkbox name=\"", "\"");
		parser.addTag(POST_USER, "postername\">", "</span>");
		parser.addTag(POST_TITLE, "title\">", "</span>");
		parser.addTag(POST_THUMBNAIL, "<img src=", " ");
		parser.addTag(POST_IMGURL, "<br><a href=\"", "\"");
		parser.addTag(POST_FILENAME, "<span title=\"", "\"");
		parser.addTag(POST_DATE, "</span> ", " <");
		parser.addTag(POST_MESSAGE, "<blockquote>", "</blockquote>");

		try {
			parser.parseStream(responseStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		receiver.parsingDone();
	}

	@Override
	public void parsedString(int id, String data) {

		switch (id) {
		case POST_ID:
			if (currentPost == null || currentPost.id != 0)
				currentPost = new MutablePost();
			currentPost.id = Integer.parseInt(data);
			break;
		case POST_USER:
			if (currentPost == null)
				return;
			currentPost.user = data;
			break;
		case POST_DATE:
			if (currentPost == null)
				return;
			currentPost.date = data;
			break;
		case POST_FILENAME:
			if (!firstpost) {
				firstpost = true;
				currentPost = new MutablePost();
			}
			if (currentPost == null)
				return;
			currentPost.filename = data;
			break;
		case POST_IMGURL:
			if (currentPost == null)
				return;
			currentPost.imgUrl = data;
			break;
		case POST_THUMBNAIL:
			if (currentPost == null)
				return;
			currentPost.thumbnail = data;
			break;
		case POST_TITLE:
			if (currentPost == null)
				return;
			currentPost.title = data;
			break;
		case POST_MESSAGE:
			if (currentPost == null)
				return;
			currentPost.message = data;
			receiver.addPost(currentPost.toPost());
			break;
		default:
			System.err.println("unhandled case " + id + ": " + data);
		}

	}

	class MutablePost {
		String		imgUrl		= null;
		String		thumbnail	= null;
		String		filename	= null;
		String		mail		= "";
		String		title		= "";
		String		user		= "";
		String		date		= "";
		String		message		= "";
		int			id			= 0;
		List<Image>	images		= new LinkedList<Image>();

		public Post toPost() {
			if (imgUrl != null)
				images.add(new Image(thumbnail, imgUrl, filename));

			message = message.replace("<br />", "\n").replaceAll("\\<.*?>", "");
			message = StringEscapeUtils.unescapeHtml4(message);

			mail = StringUtils.substringBetween(user, "<a href=\"mailto:", "\"");
			user = StringEscapeUtils.unescapeHtml4(user);
			title = StringEscapeUtils.unescapeHtml4(title);

			if (title.length() == 0)
				title = null;

			return new Post(id, date, title, user, mail, message, images);
		}
	}
}

class FourChanThreadsParser implements IParseDataReceiver {

	final static int		THREAD_URL	= 0;
	private PostReceiver	receiver;

	public void getThreads(InputStream responseStream, PostReceiver receiver) {
		this.receiver = receiver;
		StreamParser parser = new StreamParser(this);

		parser.addTag(THREAD_URL, "[<a href=\"res/", "\"");

		try {
			parser.parseStream(responseStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void parsedString(int id, String data) {
		switch (id) {
		case THREAD_URL:
			receiver.addThread(new Thread(null, "res/" + data, 0));
			break;

		default:
			break;
		}
	}
}
