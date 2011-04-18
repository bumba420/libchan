package de.benpicco.libchan;

import de.benpicco.libchan.imageboards.AsyncImageBoardParser;
import de.benpicco.libchan.imageboards.ChanSpecification;
import de.benpicco.libchan.imageboards.Post;

public class Main {
	public static void main(final String[] args) {

		// ChanSpecification spec = new ChanSpecification("chans/4chan.chan");

		// String url = "http://krautchan.net/b/thread-2855681.html";
		String url = "http://boards.4chan.org/soc/res/3525012";

		AsyncImageBoardParser parser = new ChanSpecification("chans/4chan.chan").getImageBoardParser("4chan");

		if (parser == null) {
			System.err.println("No parser found");
			System.exit(-1);
		}

		parser.getPosts(url, new SimplePostReceiver());
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
