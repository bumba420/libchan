package de.benpicco.libchan.interfaces;

import de.benpicco.libchan.imageboards.Thread;


public interface ThreadHandler {
	public void onAddThread(Thread thread);

	public void onThreadsParsingDone();
}
