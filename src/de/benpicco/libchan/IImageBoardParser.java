package de.benpicco.libchan;

import java.io.InputStream;

public interface IImageBoardParser {
	public void getMessages(InputStream in, IPostReceiver rec);

	public void getThreads(InputStream in, IPostReceiver rec);
}
