package de.benpicco.libchan;

import java.io.IOException;

import de.benpicco.libchan.imageboards.Post;

public interface IImageBoardParser {
	public void getPosts(String url, PostHandler rec) throws IOException;

	public void getThreads(String url, ThreadHandler rec) throws IOException;

	public String composeUrl(String url, Post post);

	public String composeUrl(String url, int post);
}
