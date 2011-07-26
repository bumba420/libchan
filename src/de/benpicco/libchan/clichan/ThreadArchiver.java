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
import de.benpicco.libchan.handler.VocarrooHandler;
import de.benpicco.libchan.interfaces.ImageBoardParser;
import de.benpicco.libchan.interfaces.NewThreadReceiver;
import de.benpicco.libchan.util.Logger;

public class ThreadArchiver implements NewThreadReceiver, Runnable {
	public final static String				VERSION	= "0.3.2";

	private final ChanManager				manager;
	private final List<ImageBoardParser>	threads;
	private final List<ImageBoardParser>	newThreads;

	private final ArchiveOptions			o;
	private boolean							running;

	public ThreadArchiver(ArchiveOptions options) {
		this.o = options;
		threads = new ArrayList<ImageBoardParser>();
		newThreads = new ArrayList<ImageBoardParser>();
		manager = new ChanManager(o.chanConfig);
	}

	private boolean contains(List<ImageBoardParser> parsers, String url) {
		for (ImageBoardParser parser : parsers)
			if (parser.getUrl().equals(url))
				return true;
		return false;
	}

	public void addThread(String url) {
		addThread(url, o.target);
	}

	public synchronized void addThread(String url, String target) {
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

		PostArchiver handler = new PostArchiver(o.delete);

		if (o.saveImages)
			handler.addHandler(new DownloadImageHandler(target, o.threadFolders));
		// TODO: remove magic number
		handler.addHandler(new PostCountHandler(handler, 500));
		if (o.saveHtml)
			handler.addHandler(new ArchiveHtmlHandler(target, o.htmlTemplate, o.threadFolders));
		if (o.followUpTag != null)
			handler.addHandler(new FollowupThreadHandler(parser, o.followUpTag, this));
		if (o.names != null)
			handler.addHandler(new UserNotifyHandler(o.names));
		if (o.recordStats)
			handler.addHandler(new StatisticsHandler(target, o.threadFolders));
		if (o.vocaroo != null)
			handler.addHandler(new VocarrooHandler(target, o.threadFolders, o.vocaroo));

		parser.setPostHandler(handler);
		newThreads.add(parser);
	}

	@Override
	public void run() {
		running = true;
		do {
			threads.addAll(newThreads);
			newThreads.clear();

			Logger.get().println("Monitoring " + threads.size() + " thread" + (threads.size() != 1 ? "s" : ""));
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
			else if (newThreads.size() > 0) // download follow up thread
				continue;
			else
				return;
		} while (threads.size() > 0);
		running = false;
	}

	public int threads() {
		return threads.size() + newThreads.size();
	}

	public boolean isRunnig() {
		return running;
	}
}