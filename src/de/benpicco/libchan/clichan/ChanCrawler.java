package de.benpicco.libchan.clichan;

import java.io.IOException;

import de.benpicco.libchan.imageboards.GenericImageBoardParser;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.interfaces.ThreadHandler;
import de.benpicco.libchan.util.Logger;

public class ChanCrawler {
	public static void lookFor(final String[] names, final String board, int startpage, int endpage, String config) {
		String printNames = "";
		for (int i = 0; i < names.length; ++i)
			printNames += (i > 0 ? ", " : "") + names[i];
		Logger.get().println("Searching " + board + " for " + printNames);

		ChanManager manager = new ChanManager(config);
		if (manager.getParser(board) == null) {
			Logger.get().error("No .chan specification for " + board + " present.");
			return;
		}

		for (int i = startpage; i < endpage; ++i)
			new Thread(new PageCrawler(board + i, manager, names)).run();
	}
}

class PageCrawler implements Runnable, PostHandler, ThreadHandler {
	private final String[]	names;
	GenericImageBoardParser	threadParser	= null;
	GenericImageBoardParser	postParser		= null;

	public PageCrawler(String page, ChanManager manager, final String[] names) {
		threadParser = manager.getParser(page);
		threadParser.setThreadHandler(this);
		postParser = manager.getParser(page);
		postParser.setPostHandler(this);
		this.names = names;
	}

	@Override
	public void run() {
		if (names == null)
			return;
		try {
			threadParser.getThreads();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onAddPost(Post post) {
		for (String name : names)
			if (post.user.toLowerCase().contains(name.toLowerCase()))
				Logger.get().println(name + ": " + postParser.composeUrl(post));
			else if (post.message.toLowerCase().contains(name.toLowerCase()))
				Logger.get().println("mentioned " + name + ": " + postParser.composeUrl(post));
	}

	@Override
	public void onAddThread(final de.benpicco.libchan.imageboards.Thread thread) {

		postParser.setUrl(thread.getUrl());
		try {
			postParser.getPosts();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onPostsParsingDone() {
	}

	@Override
	public void onThreadsParsingDone() {
	}
}
