package de.benpicco.libchan;

import java.io.IOException;

import de.benpicco.libchan.imageboards.AsyncImageBoardParser;
import de.benpicco.libchan.imageboards.ChanSpecification;
import de.benpicco.libchan.imageboards.Post;

public class Main {
	public static void main(final String[] args) {

		ChanSpecification spec = new ChanSpecification("chans/4chan.chan");

		// String url = "http://krautchan.net/b/thread-2855681.html";
		String url = "http://boards.4chan.org/soc/res/3406080";

		AsyncImageBoardParser parser = new ChanSpecification("chans/4chan.chan").getImageBoardParser("4chan");
		try {
			parser.getPosts(url, new SimplePostReceiver());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

	@Override
	public void onAddPost(final Post post) {
		System.out.println(post);
	}

	@Override
	public void onPostsParsingDone() {
		System.out.println("posts parsing done.");
		System.exit(0);
	}

	@Override
	public void onAddThread(Thread thread) {
		System.out.println(thread.getUrl());

	}

	@Override
	public void onThreadsParsingDone() {
		System.out.println("threads parsing done.");
	}
}
