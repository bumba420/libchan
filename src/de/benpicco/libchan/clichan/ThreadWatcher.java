package de.benpicco.libchan.clichan;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.ImageBoardParser;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.util.Logger;

public class ThreadWatcher implements PostHandler, Runnable {
	private final ImageBoardParser	parser;
	private final int				interval;
	private final PostHandler		receiver;
	private final String			url;
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
	public ThreadWatcher(String url, int interval, PostHandler receiver, ImageBoardParser parser) {
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
		receiver.onPostsParsingDone();
	}

	@Override
	public void run() {
		boolean running = true;
		while (running) {
			try {
				parser.getPosts(url, this);
				java.lang.Thread.sleep(interval);
				Logger.get().println("Refreshing " + url);
			} catch (FileNotFoundException e) {
				Logger.get().println("Thread " + url + " does not exist.");
				running = false;
			} catch (IOException e) {
				Logger.get().error("Error downloading " + url + ": " + e.getMessage());
				break;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
