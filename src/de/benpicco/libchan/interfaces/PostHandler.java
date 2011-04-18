package de.benpicco.libchan.interfaces;

import de.benpicco.libchan.imageboards.Post;

public interface PostHandler {
	public void onAddPost(Post post);

	public void onPostsParsingDone();
}