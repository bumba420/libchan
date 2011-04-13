package de.benpicco.libchan.clichan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.benpicco.libchan.PostHandler;
import de.benpicco.libchan.handler.ArchiveHtmlHandler;
import de.benpicco.libchan.handler.DownloadImageHandler;
import de.benpicco.libchan.handler.FollowupThreadHandler;
import de.benpicco.libchan.handler.NewThreadReceiver;
import de.benpicco.libchan.handler.PostCountHandler;
import de.benpicco.libchan.handler.UserNotifyHandler;
import de.benpicco.libchan.imageboards.AsyncImageBoardParser;
import de.benpicco.libchan.imageboards.Post;

public class ThreadArchiver implements NewThreadReceiver {
	final String				target;
	final int					interval;
	final String				oldThread;
	final String				followUpTag;
	final boolean				saveHtml;
	private final ChanManager	manager;
	private final List<String>	names;

	public ThreadArchiver(String thread, String target, String config, int interval, String followUpTag,
			boolean saveHtml, List<String> names) {
		this.followUpTag = followUpTag != null ? followUpTag.toUpperCase() : null;
		this.oldThread = thread;
		this.target = target;
		this.interval = interval;
		this.saveHtml = saveHtml;
		this.names = names;
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
				AsyncImageBoardParser parser = manager.getParser(oldThread);
				if (parser == null) {
					System.err.println("URL scheme not supported by any parser");
					return;
				}

				String thread = oldThread;
				if (id > 0)
					thread = parser.composeUrl(oldThread, id);

				String t = target + File.separator + thread.substring(thread.lastIndexOf('/') + 1);
				System.out.println("Saving pictures from " + thread + " to " + t);

				List<PostHandler> handler = new ArrayList<PostHandler>();
				handler.add(new DownloadImageHandler(t));
				handler.add(new PostCountHandler(500));
				if (saveHtml)
					handler.add(new ArchiveHtmlHandler(t));
				if (followUpTag != null)
					handler.add(new FollowupThreadHandler(followUpTag, ThreadArchiver.this));
				if (names != null)
					handler.add(new UserNotifyHandler(names));

				try {
					if (interval >= 0)
						(new ThreadWatcher(thread, interval, new PostArchiver(handler), parser)).run();
					else {
						parser.getPosts(thread, new PostArchiver(handler));
						parser.dispose();
					}
				} catch (IOException e) {
					System.out.println("Unable to parse " + thread + ", " + e);
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