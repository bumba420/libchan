package de.benpicco.libchan;

import java.util.LinkedList;
import java.util.List;

public class Post {
	public final int id;
	public final String title;
	public final String user;
	public final String mail;
	public final String message;
	public final String date;
	public final List<Image> images;

	public Post(int id, String date, String title, String user, String mail,
			String message, List<Image> images) {
		this.id = id;
		this.title = title;
		this.user = user;
		this.mail = mail;
		this.message = message;
		this.date = date;
		this.images = images != null ? images : new LinkedList<Image>();
	}

	public String toString() {
		return "ID: "
				+ id
				+ "\n"
				+ "Date: "
				+ date
				+ "\n"
				+ (title != null ? "Title: " + title + "\n" : "")
				+ "User: "
				+ user
				+ "\n"
				+ (mail != null ? "Mail: " + mail + "\n" : "")
				+ (images.size() > 0 ? "Images: " + images.toString() + "\n"
						: "") + message + "\n";
	}
}
