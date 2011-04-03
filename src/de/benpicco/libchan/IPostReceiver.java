package de.benpicco.libchan;

import de.benpicco.libchan.imageboards.Post;

public interface IPostReceiver {
	public void addPost(Post post);

	public void addThread(Thread thread);

	public void parsingDone();
}
