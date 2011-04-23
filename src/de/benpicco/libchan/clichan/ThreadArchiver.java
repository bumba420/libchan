package de.benpicco.libchan.clichan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.benpicco.libchan.handler.ArchiveHtmlHandler;
import de.benpicco.libchan.handler.DownloadImageHandler;
import de.benpicco.libchan.handler.FollowupThreadHandler;
import de.benpicco.libchan.handler.NewThreadReceiver;
import de.benpicco.libchan.handler.PostCountHandler;
import de.benpicco.libchan.handler.StatisticsHandler;
import de.benpicco.libchan.handler.UserNotifyHandler;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.IImageBoardParser;
import de.benpicco.libchan.interfaces.PostHandler;

public class ThreadArchiver implements NewThreadReceiver {
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
		new Thread(new Runnable() {

			@Override
			public void run() {
				IImageBoardParser parser = manager.getParser(oldThread);
				if (parser == null) {
					System.err.println("URL scheme not supported by any parser");
					return;
				}

				String thread = oldThread;
				if (id > 0)
					thread = parser.composeUrl(oldThread, id);

				String t = target;
				if (threadFolders)
					t += thread.substring(thread.lastIndexOf('/') + 1);
				System.out.println("Saving items from " + thread + " to " + t);

				List<PostHandler> handler = new ArrayList<PostHandler>();
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
					handler.add(new StatisticsHandler());

				if (interval >= 0)
					(new ThreadWatcher(thread, interval, new PostArchiver(handler), parser)).run();
				else
					try {
						parser.getPosts(thread, new PostArchiver(handler));
					} catch (FileNotFoundException e) {
						System.out.println("Thread " + thread + " does not exist.");
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
}