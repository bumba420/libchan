package de.benpicco.libchan.interfaces;

import java.io.IOException;

import de.benpicco.libchan.imageboards.Post;

public interface ImageBoardParser {

	public void setPostHandler(PostHandler rec);

	public void setBoardHandler(BoardHandler rec);

	public void getPosts() throws IOException;

	public void getBoards() throws IOException;

	public String composeUrl(int post);

	public String composeUrl(Post post);

	public String getUrl();
}
