package de.benpicco.libchan.clichan;

import java.io.IOException;
import java.util.List;

import de.benpicco.libchan.IImageBoardParser;
import de.benpicco.libchan.IPostReceiver;
import de.benpicco.libchan.IThreadReceiver;
import de.benpicco.libchan.imageboards.Post;

public class ChanCrawler {
	public static void lookFor(List<String> names, final String board) {
		IImageBoardParser parser = new ChanManager("chans/").getParser(board);
		for (int i = 0; i < 15; i++)
			new Thread(new PageCrawler(board + i, parser, names)).run();
	}
}

class PageCrawler implements Runnable, IPostReceiver, IThreadReceiver {
	private final String			page;
	private final IImageBoardParser	parser;
	private final List<String>		names;

	public PageCrawler(String page, IImageBoardParser parser, List<String> names) {
		this.page = page;
		this.parser = parser;
		this.names = names;
	}

	@Override
	public void run() {
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
				System.out.println(post + "\n" + parser.composeUrl(page, post) + "\n\n");
	}

	@Override
	public void onAddThread(final de.benpicco.libchan.Thread thread) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					parser.getMessages(thread.getUrl(), PageCrawler.this);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).run();
	}

	@Override
	public void onPostParsingDone() {
	}

	@Override
	public void onThreadParsingDone() {
	}
}
