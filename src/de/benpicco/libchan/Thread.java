package de.benpicco.libchan;

import de.benpicco.libchan.imageboards.Post;

public class Thread {
	private int				replies;
	private final String	url;
	private final Post		opening;

	public Thread(Post opening, String url, int replies) {
		this.url = url;
		this.replies = replies;
		this.opening = opening;
	}

	public int getReplies() {
		return replies;
	}

	public String getUrl() {
		return url;
	}

	public String getTitle() {
		return opening.title;
	}

	public int getId() {
		return opening.id;
	}

	public Post getOpening() {
		return opening;
	}
}
