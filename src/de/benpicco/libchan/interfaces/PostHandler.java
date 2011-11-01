package de.benpicco.libchan.interfaces;

import de.benpicco.libchan.imageboards.Post;

/**
 * If a PostHandler keeps state specific to a certain Thread, it has to
 * implement a clone() function that returns a 'fresh' PostHandler that is able
 * to handle a new Thread.
 */
public interface PostHandler extends Cloneable {
	public void onAddPost(Post post);

	public void onPostsParsingDone();
}