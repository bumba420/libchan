package de.benpicco.libchan.clichan;

import java.io.IOException;

import de.benpicco.libchan.IPostReceiver;
import de.benpicco.libchan.IThreadReceiver;
import de.benpicco.libchan.imageboards.AsyncImageBoardParser;
import de.benpicco.libchan.imageboards.Post;

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

class PageCrawler implements Runnable, IPostReceiver, IThreadReceiver {
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
		try {
			parser.getThreads(page, this);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
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
	public void onAddThread(final de.benpicco.libchan.Thread thread) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				AsyncImageBoardParser parser = manager.getParser(thread.getUrl());
				try {
					parser.getPosts(thread.getUrl(), PageCrawler.this);
					parser.dispose();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).run();
	}

	@Override
	public void onPostsParsingDone() {
		// System.out.print(".");
	}

	@Override
	public void onThreadsParsingDone() {
		// System.out.print("!");
		parser.dispose();
	}
}
