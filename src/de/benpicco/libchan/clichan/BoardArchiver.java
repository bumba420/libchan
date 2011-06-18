package de.benpicco.libchan.clichan;

import java.io.IOException;
import java.util.ArrayList;

import de.benpicco.libchan.interfaces.ImageBoardParser;
import de.benpicco.libchan.interfaces.ThreadHandler;
import de.benpicco.libchan.util.Logger;

// nasty code duplication ahead :/
// quick & dirty version, this should be merged with ThreadArchiver
public class BoardArchiver implements Runnable, ThreadHandler {

	private final ThreadArchiver				archiver;
	private final int							interval;
	private final ArrayList<ImageBoardParser>	boards;
	private ChanManager							manager;

	public BoardArchiver(ArchiveOptions options) {
		archiver = new ThreadArchiver(options);
		interval = options.boardInterval;
		boards = new ArrayList<ImageBoardParser>();

		manager = new ChanManager(options.config);
	}

	public void addBoard(String url) {
		ImageBoardParser p = manager.getParser(url);
		if (p == null)
			p = manager.guessParser(url);
		if (p == null)
			Logger.get().error("No parser found for " + url);

		p.setThreadHandler(this);
		boards.add(p);
	}

	@Override
	public void run() {
		do {
			for (ImageBoardParser parser : boards) {
				Logger.get().println("Refreshing " + parser.getUrl());
				try {
					parser.getThreads();
				} catch (IOException e) {
					Logger.get().error("Can't refresh " + parser.getUrl());
				}
			}

			if (interval > 0)
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
				}
			else
				return;
		} while (boards.size() > 0);
	}

	@Override
	public void onAddThread(de.benpicco.libchan.imageboards.Thread thread) {
		archiver.addThread(thread.getUrl());
	}

	@Override
	public void onThreadsParsingDone() {
		if (!archiver.isRunnig())
			new Thread(archiver).start();
	}
}
