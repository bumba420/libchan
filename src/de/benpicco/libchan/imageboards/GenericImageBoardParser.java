package de.benpicco.libchan.imageboards;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.benpicco.libchan.IImageBoardParser;
import de.benpicco.libchan.IPostReceiver;
import de.benpicco.libchan.streamparser.IParseDataReceiver;
import de.benpicco.libchan.streamparser.StreamParser;

public class GenericImageBoardParser implements IImageBoardParser, IParseDataReceiver {

	private IPostReceiver		receiver;

	private Post				currentPost		= null;
	private Image				currentImage	= null;

	private final String		url;
	private final List<Tags>	threadStarter;
	private final List<Tags>	postStarter;
	private final List<Tags>	postEnder;
	private final List<Tags>	imageEnder;

	private final StreamParser	parser;

	private final String		imgPrefix;
	private final String		thumbPrefix;

	public GenericImageBoardParser(String url, List<Tags> threadStarter, List<Tags> postStarter, List<Tags> postEnder,
			List<Tags> imageEnder, StreamParser parser, String imgPrefix, String thumbPrefix) {
		this.url = url;
		this.threadStarter = threadStarter;
		this.postStarter = postStarter;
		this.postEnder = postEnder;
		this.imageEnder = imageEnder;
		this.parser = parser;
		this.imgPrefix = imgPrefix;
		this.thumbPrefix = thumbPrefix;
	}

	@Override
	public void getMessages(InputStream in, IPostReceiver rec) {
		receiver = rec;
		try {
			parser.parseStream(in, this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		rec.parsingDone();
	}

	@Override
	public void getThreads(InputStream in, IPostReceiver rec) {
		// TODO Auto-generated method stub

	}

	@Override
	public void parsedString(Tags tag, String data) {

		if (currentPost == null)
			if (threadStarter.contains(tag)) {
				currentPost = new Post();
				currentPost.isFirstPost = true;
			} else if (postStarter.contains(tag)) {
				currentPost = new Post();
			} else
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
			currentImage.url = imgPrefix + data.trim();
			if (currentImage.url.startsWith("/"))
				currentImage.url = url + currentImage.url;
			break;
		case POST_THUMBNAIL:
			if (currentImage == null)
				currentImage = new Image();
			currentImage.thumbnailUrl = thumbPrefix + data.trim();
			if (currentImage.thumbnailUrl.startsWith("/"))
				currentImage.thumbnailUrl = url + currentImage.thumbnailUrl;
			break;
		case POST_FILENAME:
			if (currentImage == null)
				currentImage = new Image();
			currentImage.filename = data.trim();
			break;
		case POST_TITLE:
			currentPost.title = data;
			break;
		case POST_MESSAGE:
			currentPost.message = data;
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
			currentPost.cleanup();
			receiver.addPost(currentPost);
			currentPost = null;
		}
	}
}
