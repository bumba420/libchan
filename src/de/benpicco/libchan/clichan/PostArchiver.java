package de.benpicco.libchan.clichan;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.interfaces.PostProcessor;
import de.benpicco.libchan.util.Logger;

/**
 * Contains all information and handler about a thread.
 */
public class PostArchiver implements PostHandler {
	List<PostProcessor>	handler;
	private int			lastId		= 0;

	int					threadId	= 0;
	int					count		= 0;

	// only needed if we check for deleted posts
	private List<Post>	postList	= null;
	private int			postNum		= 0;

	public PostArchiver(boolean delete) {
		handler = new ArrayList<PostProcessor>();
		if (delete)
			postList = new LinkedList<Post>();
	}

	public void addHandler(PostProcessor processor) {
		handler.add(processor);
	}

	/**
	 * private helper function, does no sanity checking
	 * 
	 * @param oldPost
	 */
	private void removePost(int postNum) {
		Post oldPost = postList.remove(postNum);
		for (PostProcessor h : handler)
			h.onPostModified(oldPost, null);
		Logger.get().println("Post " + oldPost.id + " deleted");
		Logger.get().println(oldPost.toString());
	}

	@Override
	public void onAddPost(final Post post) {
		if (post.isFirstPost()) {
			threadId = post.id;
			count = 0;
		}
		count++;

		if (post.id > lastId) { // new posts
			lastId = post.id;
			if (postNum > 0) { // last post got deleted, new post was done
								// afterwards
				for (; postNum < postList.size(); postNum++)
					removePost(postNum);
				postNum++;
			}

			if (postList != null)
				postList.add(post);

			for (PostHandler h : handler)
				h.onAddPost(post);
		} else if (postList != null) { // see which post got deleted
			Post oldPost = postList.get(postNum);
			while (oldPost.id < post.id) {
				removePost(postNum);
				oldPost = postList.get(postNum);
			}
			if (!Post.equals(oldPost, post))
				for (PostProcessor h : handler)
					h.onPostModified(oldPost, post);
			postNum++;
		}
	}

	@Override
	public void onPostsParsingDone() {
		if (postNum > 0) {
			for (; postNum < postList.size(); postNum++)
				// deleted posts that are the last posts
				removePost(postNum);

			postNum = 0;
		}
		for (PostHandler h : handler)
			h.onPostsParsingDone();
	}

	public int getThreadId() {
		return threadId;
	}

	public int getPostCount() {
		return count;
	}
}
