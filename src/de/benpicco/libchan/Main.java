package de.benpicco.libchan;

import java.io.IOException;
import java.net.MalformedURLException;

import de.benpicco.libchan.imageboards.Board;
import de.benpicco.libchan.imageboards.ChanSpecification;
import de.benpicco.libchan.imageboards.GenericImageBoardParser;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.imageboards.Thread;
import de.benpicco.libchan.interfaces.BoardHandler;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.interfaces.ThreadHandler;

public class Main {
	public static void main(final String[] args) {

		String url = "http://boards.4chan.org/soc/res/3695630";

		GenericImageBoardParser parser = new ChanSpecification("chans/4chan.chan").getImageBoardParser("4chan");

		if (parser == null) {
			System.err.println("No parser found");
			System.exit(-1);
		}

		try {
			parser.getPosts(url, new SimplePostReceiver());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
