package de.benpicco.libchan.handler;

import de.benpicco.libchan.clichan.PostArchiver;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.PostProcessor;
import de.benpicco.libchan.util.Logger;

public class PostCountHandler implements PostProcessor {
	final int			treshold;
	final PostArchiver	archiver;

	public PostCountHandler(PostArchiver archiver, int treshold) {
		this.treshold = treshold;
		this.archiver = archiver;
	}

	@Override
	public void onAddPost(Post post) {
		if (archiver.getPostCount() > treshold)
			Logger.get().println("Thread " + archiver.getThreadId() + " has " + archiver.getPostCount() + " replies.");
	}

	@Override
	public void onPostsParsingDone() {
		Logger.get().println(
				"Thread " + archiver.getThreadId() + " with " + archiver.getPostCount() + " posts received.");
	}

	@Override
	public void onPostModified(Post oldPost, Post newPost) {
	}
}
