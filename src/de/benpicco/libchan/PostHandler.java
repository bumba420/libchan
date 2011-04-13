package de.benpicco.libchan;

import de.benpicco.libchan.imageboards.Post;

public interface PostHandler {
	public void onAddPost(Post post);

	public void onPostsParsingDone();
}