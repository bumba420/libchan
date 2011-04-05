package de.benpicco.libchan;

import java.io.IOException;

import de.benpicco.libchan.clichan.ChanManager;
import de.benpicco.libchan.imageboards.Post;

public class ThreadWatcher implements IPostReceiver, Runnable {
	private final IImageBoardParser	parser;
	private final String			url;
	private final int				interval;
	private final IPostReceiver		receiver;

	int								lastId	= 0;

	/**
	 * 
	 * @param url
	 *            The thread to monitor
	 * @param interval
	 *            time in s between refreshing the thread
	 * @param receiver
	 *            Object to push new posts to
	 */
	public ThreadWatcher(String url, int interval, IPostReceiver receiver) {
		this.url = url;
		this.interval = interval * 1000;
		this.receiver = receiver;

		parser = new ChanManager("chans/").getParser(url);
	}

	@Override
	public void onAddPost(Post post) {
		if (post.id > lastId) {
			lastId = post.id;
			receiver.onAddPost(post);
		}
	}

	@Override
	public void onPostsParsingDone() {

	}

	@Override
	public void run() {
		try {
			parser.getPosts(url, this);
			java.lang.Thread.sleep(interval);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		run();
	}
}
