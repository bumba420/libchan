package de.benpicco.libchan.interfaces;

import java.io.IOException;

public interface ImageBoardParser {

	public void setPostHandler(PostHandler rec);

	public void setThreadHandler(ThreadHandler rec);

	public void setBoardHandler(BoardHandler rec);

	public void getPosts() throws IOException;

	public void getThreads() throws IOException;

	public void getBoards() throws IOException;

	public String composeUrl(int post);

	public String getUrl();
}
