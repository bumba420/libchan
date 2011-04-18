package de.benpicco.libchan;

import de.benpicco.libchan.imageboards.AsyncImageBoardParser;
import de.benpicco.libchan.imageboards.ChanSpecification;
import de.benpicco.libchan.imageboards.Post;

public class Main {
	public static void main(final String[] args) {

		// ChanSpecification spec = new ChanSpecification("chans/4chan.chan");

		// String url = "http://krautchan.net/b/thread-2855681.html";
		String url = "http://boards.4chan.org/soc/";

		AsyncImageBoardParser parser = new ChanSpecification("chans/4chan.chan").getImageBoardParser("4chan");

		if (parser == null) {
			System.err.println("No parser found");
			System.exit(-1);
		}

		parser.getThreads(url, new SimplePostReceiver());
	}
}

class SimplePostReceiver implements PostHandler, ThreadHandler {
	int	postCount	= 0;
	int	threadCount	= 0;

	@Override
	public void onAddPost(final Post post) {
		postCount++;
		System.out.println(post);
	}

	@Override
	public void onPostsParsingDone() {
		System.out.println(postCount + " posts received.");
	}

	@Override
	public void onAddThread(Thread thread) {
		threadCount++;
		System.out.println(thread.getUrl());
	}

	@Override
	public void onThreadsParsingDone() {
		System.out.println(threadCount + " threads received.");
	}
}
