package de.benpicco.libchan;

import de.benpicco.libchan.imageboards.Post;

public class Thread {
	private int				replies;
	private final String	url;

	public Thread(Post opening, String url, int replies) {
		this.url = url;
		this.replies = replies;
	}

	public int getReplies() {
		return replies;
	}

	public String getUrl() {
		return url;
	}
}
