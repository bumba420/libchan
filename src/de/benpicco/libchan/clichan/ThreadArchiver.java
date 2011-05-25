package de.benpicco.libchan.clichan;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.benpicco.libchan.handler.ArchiveHtmlHandler;
import de.benpicco.libchan.handler.DownloadImageHandler;
import de.benpicco.libchan.handler.FollowupThreadHandler;
import de.benpicco.libchan.handler.PostCountHandler;
import de.benpicco.libchan.handler.StatisticsHandler;
import de.benpicco.libchan.handler.UserNotifyHandler;
import de.benpicco.libchan.imageboards.GenericImageBoardParser;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.NewThreadReceiver;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.util.Logger;
import de.benpicco.libchan.util.Tuple;

public class ThreadArchiver implements NewThreadReceiver, Runnable {
	private final ChanManager						manager;
	private final List<Tuple<String, PostArchiver>>	threads;
	private GenericImageBoardParser					parser;

	private final ArchiveOptions					o;

	public ThreadArchiver(ArchiveOptions options) {
		this.o = options;
		threads = new CopyOnWriteArrayList<Tuple<String, PostArchiver>>();
		manager = new ChanManager(o.config);
	}

	/**
	 * Archives the thread with the id that was part of the original url.
	 */
	public void saveThread() {
		addThread(-1);
	}

	public void addThread(String url) {
		if (threads.contains(url))
			return;

		Logger.get().println("Adding " + url);

		ArrayList<PostHandler> handler = new ArrayList<PostHandler>();

		if (o.archiveThread)
			handler.add(new DownloadImageHandler(o.target, o.threadFolders));
		handler.add(new PostCountHandler(500)); // TODO: remove magic number
		if (o.saveHtml)
			handler.add(new ArchiveHtmlHandler(o.target, o.threadFolders));
		if (o.followUpTag != null)
			handler.add(new FollowupThreadHandler(o.followUpTag, this));
		if (o.names != null)
			handler.add(new UserNotifyHandler(o.names));
		if (o.recordStats)
			handler.add(new StatisticsHandler(o.target, o.threadFolders));

		threads.add(new Tuple<String, PostArchiver>(url, new PostArchiver(handler)));

	}

	/**
	 * Archives the thread with the specified id on the same board as the
	 * original url.
	 */
	public void addThread(final int id) {
		addThread(parser.composeUrl(threads.get(0).first, id)); // XXX
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

		parser = manager.getParser(threads.get(0).first); // XXX
		if (parser == null) {
			Logger.get().error("Can't find suitable parser for URL");
			return;
		}

		while (threads.size() > 0) {
			Iterator<Tuple<String, PostArchiver>> iter = threads.iterator();

			while (iter.hasNext()) {
				Tuple<String, PostArchiver> thread = iter.next();
				final String url = thread.first;

				Logger.get().println("Receiving " + url);
				try {
					parser.getPosts(url, thread.second);
				} catch (MalformedURLException e) {
					Logger.get().error("Ivalid URL: " + url);
					iter.remove();
				} catch (FileNotFoundException e) {
					Logger.get().error("Thread " + url + " does not exist.");
					iter.remove();
				} catch (IOException e) {
					Logger.get().error("Error downloading " + url + ": " + e.getMessage());
				}
			}

			if (o.interval > 0)
				try {
					Thread.sleep(o.interval);
				} catch (InterruptedException e) {
				}
			else
				return;
		}
	}
}