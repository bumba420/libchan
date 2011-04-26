package de.benpicco.libchan.handler;

import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.util.Logger;

public class PostCountHandler implements PostHandler {
	final int	treshold;
	int			count	= 0;

	public PostCountHandler(int treshold) {
		this.treshold = treshold;
	}

	@Override
	public void onAddPost(Post post) {
		if (++count > treshold)
			Logger.get().println("Thread has " + count + " replies.");
	}

	@Override
	public void onPostsParsingDone() {
		Logger.get().println("Thread with " + count + " posts received.");
	}
}
