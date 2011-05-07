package de.benpicco.libchan.clichan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.benpicco.libchan.handler.ArchiveHtmlHandler;
import de.benpicco.libchan.handler.DownloadImageHandler;
import de.benpicco.libchan.handler.FollowupThreadHandler;
import de.benpicco.libchan.handler.NewThreadReceiver;
import de.benpicco.libchan.handler.PostCountHandler;
import de.benpicco.libchan.handler.StatisticsHandler;
import de.benpicco.libchan.handler.UserNotifyHandler;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.ImageBoardParser;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.util.Logger;

public class ThreadArchiver implements NewThreadReceiver, Runnable {
	final String				target;
	final int					interval;
	final String				oldThread;
	final String				followUpTag;
	final boolean				saveHtml;
	final boolean				threadFolders;
	final boolean				archiveThread;
	final boolean				recordStats;
	private final ChanManager	manager;
	private final List<String>	names;
	private final List<String>	threads;

	public ThreadArchiver(String thread, String target, boolean threadFolders, String config, int interval,
			String followUpTag, boolean archiveThread, boolean saveHtml, boolean recordStats, List<String> names) {
		this.followUpTag = followUpTag != null ? followUpTag.toUpperCase() : null;
		this.oldThread = thread;
		this.target = target.endsWith(File.separator) ? target : target + File.separator;
		this.interval = interval;
		this.saveHtml = saveHtml;
		this.recordStats = recordStats;
		this.names = names;
		this.threadFolders = threadFolders;
		this.archiveThread = archiveThread;
		threads = new LinkedList<String>();

		manager = new ChanManager(config);
	}

	/**
	 * Archives the thread with the id that was part of the original url.
	 */
	public void saveThread() {
		saveThread(-1);
	}

	/**
	 * Archives the thread with the specified id on the same board as the
	 * original url.
	 */
	public void saveThread(final int id) {
		final ImageBoardParser parser = manager.getParser(oldThread);
		if (parser == null) {
			Logger.get().error("URL \"" + oldThread + "\" not supported by any parser");
			return;
		}

		final String thread = id > 0 ? parser.composeUrl(oldThread, id) : oldThread;

		if (threads.contains(thread))
			return;
		threads.add(thread);

		new Thread(new Runnable() {

			@Override
			public void run() {

				String t = target;
				if (threadFolders)
					t += thread.substring(thread.lastIndexOf('/') + 1);
				Logger.get().println("Saving items from " + thread + " to " + t);

				List<PostHandler> handler = new ArrayList<PostHandler>();

				ThreadWatcher watcher = null;
				if (interval >= 0)
					watcher = new ThreadWatcher(thread, interval, new PostArchiver(handler), parser);

				if (archiveThread)
					handler.add(new DownloadImageHandler(t));
				handler.add(new PostCountHandler(500));
				if (saveHtml)
					handler.add(new ArchiveHtmlHandler(t));
				if (followUpTag != null)
					handler.add(new FollowupThreadHandler(followUpTag, ThreadArchiver.this));
				if (names != null)
					handler.add(new UserNotifyHandler(names));
				if (recordStats)
					handler.add(new StatisticsHandler(t));

				if (watcher != null)
					watcher.run();
				else
					try {
						parser.getPosts(thread, new PostArchiver(handler));
					} catch (FileNotFoundException e) {
						Logger.get().println("Thread " + thread + " does not exist.");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}).start();
	}

	class PostArchiver implements PostHandler {
		List<PostHandler>	handler;

		public PostArchiver(List<PostHandler> handler) {
			this.handler = handler;
		}

		@Override
		public void onAddPost(final Post post) {
			for (PostHandler h : handler)
				h.onAddPost(post);
		}

		@Override
		public void onPostsParsingDone() {
			for (PostHandler h : handler)
				h.onPostsParsingDone();
		}
	}

	@Override
	public void run() {
		saveThread();
	}
}