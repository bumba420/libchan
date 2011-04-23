package de.benpicco.libchan.clichan;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.IImageBoardParser;
import de.benpicco.libchan.interfaces.PostHandler;

public class ThreadWatcher implements PostHandler, Runnable {
	private final IImageBoardParser	parser;
	private final String			url;
	private final int				interval;
	private final PostHandler		receiver;

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
	public ThreadWatcher(String url, int interval, PostHandler receiver, IImageBoardParser parser) {
		this.url = url;
		this.interval = interval * 1000;
		this.receiver = receiver;
		this.parser = parser;
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
		boolean running = true;
		while (running) {
			try {
				parser.getPosts(url, this);
				java.lang.Thread.sleep(interval);
				System.out.println("Refreshing thread…");
			} catch (FileNotFoundException e) {
				System.out.println("Thread " + url + " does not exist.");
				running = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
