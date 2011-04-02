package de.benpicco.libchan.imageboards;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import de.benpicco.libchan.IImageBoardParser;
import de.benpicco.libchan.IPostReceiver;
import de.benpicco.libchan.Image;
import de.benpicco.libchan.Post;
import de.benpicco.libchan.streamparser.IParseDataReceiver;
import de.benpicco.libchan.streamparser.StreamParser;

public class KrautchanParser implements IImageBoardParser {

	@Override
	public void parseThread(InputStream in, IPostReceiver rec) {
		new KrautchanThreadParser().parseThread(in, rec);
	}

	@Override
	public void getThreads(InputStream in, IPostReceiver rec) {
		// TODO Auto-generated method stub

	}
}

class KrautchanThreadParser implements IParseDataReceiver {

	final static int		POST_ID			= 0;
	final static int		POST_USER		= 1;
	final static int		POST_TITLE		= 2;
	final static int		POST_THUMBNAIL	= 3;
	final static int		POST_IMGURL		= 4;
	final static int		POST_FILENAME	= 5;
	final static int		POST_DATE		= 6;
	final static int		POST_MESSAGE	= 7;

	private IPostReceiver	receiver;
	private MutablePost		currentPost		= null;
	private Image			currentImage	= null;

	public void parseThread(InputStream responseStream, IPostReceiver receiver) {
		this.receiver = receiver;
		StreamParser parser = new StreamParser(this);
		parser.addTag(POST_ID, "<input name=\"post_", "\"");
		parser.addTag(POST_USER, "<span class=\"postername\">", "</span>");
		parser.addTag(POST_TITLE, "<span class=\"postsubject\">", "</span>");
		parser.addTag(POST_THUMBNAIL, " src=/thumbnails/", "\n");
		parser.addTag(POST_IMGURL, "<span class=\"filename\"><a href=\"", "\"");
		parser.addTag(POST_DATE, "<span class=\"postdate\">", "</span>");
		parser.addTag(POST_MESSAGE, "<blockquote>", "</blockquote>");

		try {
			parser.parseStream(responseStream);
		} catch (IOException e) {
			System.err.println("Parsing thread failed:");
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
		case POST_IMGURL:
			if (currentPost == null)
				return;
			currentImage = new Image(null, "http://krautchan.net" + data, data.substring(data.lastIndexOf('/') + 1));
			break;
		case POST_THUMBNAIL:
			if (currentPost == null)
				return;
			currentImage.thumbnailUrl = "http://krautchan.net/thumbnails/" + data.trim();
			currentPost.images.add(currentImage);

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
			currentPost = null;
			break;
		default:
			System.err.println("unhandled case " + id + ": " + data);
		}

	}

	class MutablePost {
		boolean		isFirstPost	= false;
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
			if (imgUrl != null) {
				if (filename == null)
					filename = imgUrl.substring(imgUrl.lastIndexOf('/') + 1);
				images.add(new Image(thumbnail, imgUrl, filename));
			}

			message = message.replace("<br>", "\n").replaceAll("\\<.*?>", "").trim();
			message = StringEscapeUtils.unescapeHtml4(message);

			mail = StringUtils.substringBetween(user, "<a href=\"mailto:", "\"");
			user = StringEscapeUtils.unescapeHtml4(user.replaceAll("\\<.*?>", ""));
			title = StringEscapeUtils.unescapeHtml4(title);

			if (title.length() == 0)
				title = null;

			return new Post(id, isFirstPost, "", date, title, user, mail, message, images);
		}
	}
}
