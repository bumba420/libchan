package de.benpicco.libchan;

import java.io.InputStream;

public interface ThreadParser {
	public void parseThread(InputStream in, PostReceiver rec);

	public void getThreads(InputStream in, PostReceiver rec);
}
