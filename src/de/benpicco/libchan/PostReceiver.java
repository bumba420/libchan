package de.benpicco.libchan;

public interface PostReceiver {
	public void addPost(Post post);

	public void addThread(Thread thread);

	public void parsingDone();
}
