package de.benpicco.libchan.clichan;

import java.io.IOException;

import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.ImageBoardParser;
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
	ImageBoardParser		parser	= null;
	final String			page;
	ChanManager				manager;

	public PageCrawler(String page, ChanManager manager, final String[] names) {
		parser = manager.getParser(page);
		this.names = names;
		this.page = page;
		this.manager = manager;
	}

	@Override
	public void run() {
		if (names == null)
			return;
		try {
			parser.getThreads(page, this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onAddPost(Post post) {
		for (String name : names)
			if (post.user.toLowerCase().contains(name.toLowerCase()))
				Logger.get().println(name + ": " + parser.composeUrl(page, post));
			else if (post.message.toLowerCase().contains(name.toLowerCase()))
				Logger.get().println("mentioned " + name + ": " + parser.composeUrl(page, post));
	}

	@Override
	public void onAddThread(final de.benpicco.libchan.imageboards.Thread thread) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					parser.getPosts(thread.getUrl(), PageCrawler.this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void onPostsParsingDone() {
	}

	@Override
	public void onThreadsParsingDone() {
	}
}
