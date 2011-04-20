package de.benpicco.libchan.clichan;

import de.benpicco.libchan.imageboards.AsyncImageBoardParser;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.interfaces.ThreadHandler;

public class ChanCrawler {
	public static void lookFor(final String[] names, final String board, int startpage, int endpage, String config) {
		System.out.print("Searching " + board + " for ");
		for (int i = 0; i < names.length; ++i)
			System.out.print((i > 0 ? ", " : "") + names[i]);
		System.out.println();

		ChanManager manager = new ChanManager(config);
		if (manager.getParser(board) == null) {
			System.err.println("No .chan specification for " + board + " present.");
			return;
		}

		for (int i = startpage; i < endpage; ++i)
			new Thread(new PageCrawler(board + i, manager, names)).run();
	}
}

class PageCrawler implements Runnable, PostHandler, ThreadHandler {
	private final String[]	names;
	AsyncImageBoardParser	parser	= null;
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
		parser.getThreads(page, this);
	}

	@Override
	public void onAddPost(Post post) {
		for (String name : names)
			if (post.user.toLowerCase().contains(name.toLowerCase()))
				System.out.println(name + ": " + parser.composeUrl(page, post));
			else if (post.message.toLowerCase().contains(name.toLowerCase()))
				System.out.println("mentioned " + name + ": " + parser.composeUrl(page, post));

	}

	@Override
	public void onAddThread(final de.benpicco.libchan.imageboards.Thread thread) {
		parser.getPosts(thread.getUrl(), this);
	}

	@Override
	public void onPostsParsingDone() {
	}

	@Override
	public void onThreadsParsingDone() {
	}
}
