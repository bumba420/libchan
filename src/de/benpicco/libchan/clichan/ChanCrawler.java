package de.benpicco.libchan.clichan;

import java.io.IOException;
import java.util.Arrays;

import de.benpicco.libchan.imageboards.GenericImageBoardParser;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.interfaces.ThreadHandler;
import de.benpicco.libchan.util.Logger;
import de.benpicco.libchan.util.Misc;

public class ChanCrawler {
	public static void lookFor(final String[] names, final String board, int startpage, int endpage, String config) {
		Logger.get().println("Searching " + board + " for " + Misc.printNames(names));

		ChanManager manager = new ChanManager(config, null);
		GenericImageBoardParser parser = manager.getParser(board);
		if (parser == null) {
			Logger.get().error("No .chan specification for " + board + " present.");
			return;
		}

		for (int i = startpage; i < endpage; ++i)
			new Thread(new PageCrawler(parser.getPage(i), manager, names)).run();
	}
}

class PageCrawler implements Runnable, PostHandler, ThreadHandler {
	private final String[]	names;
	GenericImageBoardParser	threadParser	= null;
	GenericImageBoardParser	postParser		= null;

	int[]					occurence;
	int[]					mentioned;
	int						threadId		= 0;

	public PageCrawler(String page, ChanManager manager, final String[] names) {
		threadParser = manager.getParser(page);
		threadParser.setThreadHandler(this);
		postParser = manager.getParser(page);
		postParser.setPostHandler(this);
		this.names = new String[names.length];
		for (int i = 0; i < names.length; ++i)
			this.names[i] = names[i].toLowerCase();
		occurence = new int[names.length];
		mentioned = new int[names.length];
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
		// we only have one thread here
		if (threadId > 0 && threadId != post.op)
			printResults();
		threadId = post.op;
		for (int i = 0; i < names.length; ++i)
			if (post.user.toLowerCase().contains(names[i]))
				occurence[i]++;
			else if (post.message.toLowerCase().contains(names[i]))
				mentioned[i]++;
	}

	private void printResults() {
		for (int i = 0; i < names.length; ++i)
			if (occurence[i] > 0 || mentioned[i] > 0)
				Logger.get().println(
						occurence[i] + " posts from " + names[i]
								+ (mentioned[i] > 0 ? " and " + mentioned[i] + " mentions" : "") + " in "
								+ postParser.composeUrl(threadId));
		Arrays.fill(occurence, 0);
		Arrays.fill(mentioned, 0);
		threadId = 0;
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
		printResults();
	}

	@Override
	public void onThreadsParsingDone() {
	}
}
