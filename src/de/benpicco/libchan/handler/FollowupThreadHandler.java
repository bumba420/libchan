package de.benpicco.libchan.handler;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.PostHandler;

public class FollowupThreadHandler implements PostHandler {
	private final String			followUpTag;
	private final NewThreadReceiver	handler;
	// We don't want to create multiple threads on multiple mentions of the same
	// follow-up thread
	private final List<Integer>		followUps;

	/**
	 * This class checks posts for the occurrence of a follow-up thread, which
	 * is indicated by a follow-up tag, e.g.
	 * <p>
	 * NEW THREAD<br>
	 * >>new_thread_id<br>
	 * NEW THREAD
	 * </p>
	 * If such new thread is detected, the followThread() method of
	 * NewThreadHandler object is called.
	 * 
	 * @param followUpTag
	 * @param handler
	 */
	public FollowupThreadHandler(String followUpTag, NewThreadReceiver handler) {
		this.followUpTag = followUpTag;
		this.handler = handler;
		followUps = new LinkedList<Integer>();
	}

	@Override
	public void onAddPost(Post post) {
		if (followUpTag != null) {
			String newThread = StringUtils.substringBetween(post.message.toUpperCase(), followUpTag);
			if (newThread != null) {
				final String newThreadId = StringUtils.substringBetween(newThread, ">>", "\n");
				if (newThreadId != null && newThreadId.trim().length() > 0) {
					int newId = Integer.parseInt(newThreadId);
					if (!followUps.contains(newId)) {
						followUps.add(newId);
						System.out.println("Detected follow-up thread: " + newThreadId);
						handler.saveThread(newId);
					} else
						System.out.println("follow-up thread " + newId + " has already been detected.");
				}
			}
		}
	}

	@Override
	public void onPostsParsingDone() {
	}
}
