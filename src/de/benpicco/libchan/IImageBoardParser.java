package de.benpicco.libchan;

import java.io.IOException;

import de.benpicco.libchan.imageboards.Post;

public interface IImageBoardParser {
	public void getMessages(String url, IPostReceiver rec) throws IOException;

	public void getThreads(String url, IThreadReceiver rec) throws IOException;

	public String composeUrl(String url, Post post);
}
