package de.benpicco.libchan.handler;

import de.benpicco.libchan.PostHandler;
import de.benpicco.libchan.imageboards.Post;

public class PostCountHandler implements PostHandler {
	final int	treshold;
	int			count	= 0;

	public PostCountHandler(int treshold) {
		this.treshold = treshold;
	}

	@Override
	public void onAddPost(Post post) {
		if (++count > treshold)
			System.out.println("Thread has " + count + " replies.");
	}

	@Override
	public void onPostsParsingDone() {
		System.out.println("Thread with " + count + " posts received.");
	}
}
