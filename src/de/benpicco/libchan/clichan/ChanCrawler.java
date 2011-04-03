package de.benpicco.libchan.clichan;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import de.benpicco.libchan.IImageBoardParser;
import de.benpicco.libchan.IPostReceiver;
import de.benpicco.libchan.imageboards.FourChanParser;
import de.benpicco.libchan.imageboards.Post;

public class ChanCrawler {
	public static void lookFor(List<String> names, final String board) {
		IImageBoardParser parser = new FourChanParser();
		for (int i = 0; i < 15; i++)
			new Thread(new PageCrawler(board + i, parser, names)).run();
	}
}

class PageCrawler implements Runnable, IPostReceiver {
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
			InputStream in = new BufferedInputStream(new URL(page).openStream());
			parser.getThreads(in, this);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

	}

	@Override
	public void addPost(Post post) {
		for (String name : names)
			if (post.user.contains(name))
				System.out.println(post + "\n" + post.url + "\n\n");
	}

	@Override
	public void addThread(final de.benpicco.libchan.Thread thread) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					InputStream in = new BufferedInputStream(new URL(thread.getUrl()).openStream());
					parser.getMessages(in, PageCrawler.this);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).run();
	}

	@Override
	public void parsingDone() {
	}
}
