package de.benpicco.libchan.clichan;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.benpicco.libchan.handler.ArchiveHtmlHandler;
import de.benpicco.libchan.handler.DownloadImageHandler;
import de.benpicco.libchan.handler.FollowupThreadHandler;
import de.benpicco.libchan.handler.PostCountHandler;
import de.benpicco.libchan.handler.StatisticsHandler;
import de.benpicco.libchan.handler.UserNotifyHandler;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.ImageBoardParser;
import de.benpicco.libchan.interfaces.NewThreadReceiver;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.util.Logger;

public class ThreadArchiver implements NewThreadReceiver, Runnable {
	private final ChanManager				manager;
	private final List<ImageBoardParser>	threads;
	private final List<ImageBoardParser>	newThreads;

	private final ArchiveOptions			o;

	public ThreadArchiver(ArchiveOptions options) {
		this.o = options;
		threads = new ArrayList<ImageBoardParser>();
		newThreads = new ArrayList<ImageBoardParser>();
		manager = new ChanManager(o.config);
	}

	private boolean contains(List<ImageBoardParser> parsers, String url) {
		for (ImageBoardParser parser : parsers)
			if (parser.getUrl().equals(url))
				return true;
		return false;
	}

	public void addThread(String url) {
		if (contains(threads, url) || contains(newThreads, url))
			return;

		ImageBoardParser parser = manager.getParser(url);
		if (parser == null)
			parser = manager.guessParser(url);

		if (parser == null) {
			Logger.get().error("No suitable parser for " + url);
			return;
		}

		Logger.get().println("Adding " + url);

		ArrayList<PostHandler> handler = new ArrayList<PostHandler>();

		if (o.archiveThread)
			handler.add(new DownloadImageHandler(o.target, o.threadFolders));
		handler.add(new PostCountHandler(500)); // TODO: remove magic number
		if (o.saveHtml)
			handler.add(new ArchiveHtmlHandler(o.target, o.threadFolders));
		if (o.followUpTag != null)
			handler.add(new FollowupThreadHandler(parser, o.followUpTag, this));
		if (o.names != null)
			handler.add(new UserNotifyHandler(o.names));
		if (o.recordStats)
			handler.add(new StatisticsHandler(o.target, o.threadFolders));

		parser.setPostHandler(new PostArchiver(handler));
		newThreads.add(parser);
	}

	class PostArchiver implements PostHandler {
		List<PostHandler>	handler;
		private int			lastId	= 0;

		public PostArchiver(List<PostHandler> handler) {
			this.handler = handler;
		}

		@Override
		public void onAddPost(final Post post) {
			if (post.id > lastId) {
				lastId = post.id;
				for (PostHandler h : handler)
					h.onAddPost(post);
			}
		}

		@Override
		public void onPostsParsingDone() {
			for (PostHandler h : handler)
				h.onPostsParsingDone();
		}
	}

	@Override
	public void run() {
		do {
			threads.addAll(newThreads);
			newThreads.clear();

			Logger.get().println(
					"There are " + threads.size() + " thread" + (threads.size() > 1 ? "s" : "") + " in the queue.");
			Iterator<ImageBoardParser> iter = threads.iterator();

			while (iter.hasNext()) {
				ImageBoardParser thread = iter.next();

				Logger.get().println("Receiving " + thread.getUrl());
				try {
					thread.getPosts();
				} catch (MalformedURLException e) {
					Logger.get().error("Ivalid URL: " + thread.getUrl());
					iter.remove();
				} catch (FileNotFoundException e) {
					Logger.get().error("Thread " + thread.getUrl() + " does not exist.");
					iter.remove();
				} catch (IOException e) {
					Logger.get().error("Error downloading " + thread.getUrl() + ": " + e.getMessage());
				}
			}

			if (o.interval > 0)
				try {
					Thread.sleep(o.interval);
				} catch (InterruptedException e) {
				}
			else
				return;
		} while (threads.size() > 0);
	}
}