package de.benpicco.libchan.clichan;

import java.io.IOException;
import java.util.List;

import de.benpicco.libchan.IPostReceiver;
import de.benpicco.libchan.IThreadReceiver;
import de.benpicco.libchan.imageboards.AsyncImageBoardParser;
import de.benpicco.libchan.imageboards.Post;

public class ChanCrawler {
	public static void lookFor(List<String> names, final String board) {
		for (int i = 0; i < 15; i++)
			new Thread(new PageCrawler(board + i, names)).run();
	}
}

class PageCrawler implements Runnable, IPostReceiver, IThreadReceiver {
	private final String		page;
	private final List<String>	names;
	AsyncImageBoardParser		parser	= null;

	public PageCrawler(String page, List<String> names) {
		this.page = page;
		this.names = names;
	}

	@Override
	public void run() {
		parser = new ChanManager("chans/").getParser(page);
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
			if (post.user.contains(name))
				System.out.println(name + ": " + parser.composeUrl(page, post));
			else if (post.message.compareToIgnoreCase(name) == 0)
				System.out.println("mentioned " + name + ": " + parser.composeUrl(page, post));
	}

	@Override
	public void onAddThread(final de.benpicco.libchan.Thread thread) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				AsyncImageBoardParser parser = new ChanManager("chans/").getParser(page);
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
