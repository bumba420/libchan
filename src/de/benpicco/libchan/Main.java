package de.benpicco.libchan;

import de.benpicco.libchan.imageboards.AsyncImageBoardParser;
import de.benpicco.libchan.imageboards.Board;
import de.benpicco.libchan.imageboards.ChanSpecification;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.imageboards.Thread;
import de.benpicco.libchan.interfaces.BoardHandler;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.interfaces.ThreadHandler;

public class Main {
	public static void main(final String[] args) {

		// ChanSpecification spec = new ChanSpecification("chans/4chan.chan");

		// String url = "http://krautchan.net/b/thread-2855681.html";
		String url = "http://boards.4chan.org/c/res/1676190";

		AsyncImageBoardParser parser = new ChanSpecification("chans/4chan.chan").getImageBoardParser("4chan");

		if (parser == null) {
			System.err.println("No parser found");
			System.exit(-1);
		}

		parser.getPosts(url, new SimplePostReceiver());
	}
}

class SimplePostReceiver implements PostHandler, ThreadHandler, BoardHandler {
	int	postCount	= 0;
	int	threadCount	= 0;
	int	boardCount	= 0;

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

	@Override
	public void onAddBoard(Board board) {
		boardCount++;
		System.out.println(board);
	}

	@Override
	public void onBoardParsingDone() {
		System.out.println(boardCount + " boards received.");
	}
}
