package de.benpicco.libchan;

public class Thread {
	private int replies;
	private final String url;
	private Post opening;

	public Thread(Post opening, String url, int replies) {
		this.opening = opening;
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
