package de.benpicco.libchan;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import de.benpicco.libchan.clichan.HtmlConverter;
import de.benpicco.libchan.handler.UserNotifyHandler;
import de.benpicco.libchan.imageboards.ChanSpecification;
import de.benpicco.libchan.imageboards.Image;
import de.benpicco.libchan.imageboards.Post;

public class Main {
	public static void main(final String[] args) {

		ChanSpecification spec = new ChanSpecification("chans/4chan.chan");

		// String url = "http://krautchan.net/b/thread-2855681.html";
		String url = "http://boards.4chan.org/soc/res/3248788";

		Image img = new Image();
		img.thumbnailUrl = "http://0.thumbs.4chan.org/soc/thumb/1302716950081s.jpg";

		Post p = new Post();
		p.user = "Test";
		p.message = "Lorem ipsum lirum larum foo bar";
		p.images.add(img);

		List<String> names = new LinkedList<String>();
		names.add("test");

		new UserNotifyHandler(names).onAddPost(p);

		// new ThreadWatcher(url, 5, new SimplePostReceiver()).run();

		// new ThreadArchiver(url, ".", "chans/", -1, null,
		// true).archiveThread();

		// IImageBoardParser parser = new ChanManager("chans/").getParser(url);

		// if (parser == null) {
		// System.err.println("No parser found");
		// return;
		// }
		//
		// try {
		// parser.getPosts(url, new SimplePostReceiver());
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

	}
}

class SimplePostReceiver implements PostHandler, ThreadHandler {

	Writer				writer	= null;
	final HtmlConverter	converter;

	public SimplePostReceiver() {
		converter = new HtmlConverter("template/");
	}

	@Override
	public void onAddPost(final Post post) {
		// System.out.println(post);
		if (writer == null)
			try {
				writer = converter.threadToHtml(post, "");
			} catch (IOException e) {
				e.printStackTrace();
			}

		try {
			writer.write(converter.postToHtml(post));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPostsParsingDone() {
		System.out.println("Thread parsing done.");
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	@Override
	public void onAddThread(Thread thread) {
		System.out.println(thread.getUrl());

	}

	@Override
	public void onThreadsParsingDone() {
	}
}
