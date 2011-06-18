package de.benpicco.libchan.interfaces;

import de.benpicco.libchan.imageboards.Post;

public interface PostProcessor extends PostHandler {
	public void onPostModified(Post oldPost, Post newPost);
}
