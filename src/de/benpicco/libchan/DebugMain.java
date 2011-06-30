package de.benpicco.libchan;

import java.io.IOException;
import java.net.MalformedURLException;

import de.benpicco.clichan.StdLogger;
import de.benpicco.libchan.clichan.ArchiveOptions;
import de.benpicco.libchan.clichan.ChanManager;
import de.benpicco.libchan.clichan.ThreadArchiver;
import de.benpicco.libchan.imageboards.Board;
import de.benpicco.libchan.imageboards.GenericImageBoardParser;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.imageboards.Thread;
import de.benpicco.libchan.interfaces.BoardHandler;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.interfaces.ThreadHandler;
import de.benpicco.libchan.util.Logger;

public class DebugMain {

	private static void archiveThread(String url) {
		ArchiveOptions options = new ArchiveOptions();
		options.config = "chans/";
		options.target = "/tmp/libChan/";
		// options.saveHtml = true;
		options.saveImages = true;
		options.threadFolders = true;
		options.interval = 30000;
		options.delete = true;
		options.followUpTag = "new thread";

		ThreadArchiver archiver = new ThreadArchiver(options);
		archiver.addThread(url);
		archiver.run();
	}

	public static void main(final String[] args) throws MalformedURLException, IOException, InterruptedException {
		Logger.add(new StdLogger());

		String url = "http://krautchan.net/t/thread-16345.html";
		// String url = "http://www.0chan.ru/e/";
		// String url = "http://operatorchan.org/k/";

		archiveThread(url);

		ChanManager mngr = new ChanManager("chans/");
		GenericImageBoardParser parser = mngr.getParser(url);
		if (parser == null)
			parser = mngr.guessParser(url);

		if (parser == null) {
			System.err.println("No parser found");
			System.exit(-1);
		}

		// SimplePostReceiver rec = new SimplePostReceiver();
		// parser.setPostHandler(rec);
		// parser.setThreadHandler(rec);
		// parser.setBoardHandler(rec);
		//
		// parser.getPosts();

		// while (true) {
		// parser.getPosts();
		// java.lang.Thread.sleep(5000);
		// }
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
