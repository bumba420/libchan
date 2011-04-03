package de.benpicco.libchan;

import de.benpicco.libchan.imageboards.Post;

public interface IPostReceiver {
	public void onAddPost(Post post);

	public void onPostParsingDone();
}