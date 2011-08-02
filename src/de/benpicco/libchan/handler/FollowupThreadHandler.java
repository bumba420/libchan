package de.benpicco.libchan.handler;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.ImageBoardParser;
import de.benpicco.libchan.interfaces.NewThreadReceiver;
import de.benpicco.libchan.interfaces.PostProcessor;
import de.benpicco.libchan.util.Logger;

public class FollowupThreadHandler implements PostProcessor {
	private final String			followUpTag;
	private final NewThreadReceiver	handler;
	private final ImageBoardParser	parser;		// for composing the URL
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
	public FollowupThreadHandler(ImageBoardParser parser, String followUpTag, NewThreadReceiver handler) {
		this.followUpTag = followUpTag.toUpperCase();
		this.handler = handler;
		this.parser = parser;
		followUps = new LinkedList<Integer>();
	}

	@Override
	public void onAddPost(Post post) {
		if (followUpTag != null && post.message.toUpperCase().contains(followUpTag)) {
			Matcher match = Pattern.compile(">>([0-9]+)").matcher(post.message);
			if (match.find()) {
				String newThreadId = match.group(1);
				if (newThreadId != null && newThreadId.trim().length() > 0) {
					int newId = Integer.parseInt(newThreadId);
					if (!followUps.contains(newId)) {
						followUps.add(newId);
						Logger.get().println("Detected follow-up thread: " + newThreadId);
						handler.addThread(parser.composeUrl(newId));
					} else
						Logger.get().println("follow-up thread " + newId + " has already been detected.");
				}
			}
		}
	}

	@Override
	public void onPostsParsingDone() {
	}

	@Override
	public void onPostModified(Post oldPost, Post newPost) {
	}
}
