package de.benpicco.libchan;

import java.io.InputStream;

public interface ImageBoardParser {
	public void parseThread(InputStream in, PostReceiver rec);

	public void getThreads(InputStream in, PostReceiver rec);
}
