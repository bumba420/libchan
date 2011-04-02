package de.benpicco.libchan;

public interface IPostReceiver {
	public void addPost(Post post);

	public void addThread(Thread thread);

	public void parsingDone();
}
