package de.benpicco.libchan.imageboards;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

public class Post {
	public int					id;
	public boolean				isFirstPost;
	public String				title;
	public String				user;
	public String				mail;
	public String				message;
	public String				date;
	public final List<Image>	images;
	public String				url;

	public Post() {
		this.images = new LinkedList<Image>();
	}

	public String toString() {
		return "ID: " + id + "\n" + "Date: " + date + "\n" + (title != null ? "Title: " + title + "\n" : "") + "User: "
				+ user + "\n" + (mail != null ? "Mail: " + mail + "\n" : "")
				+ (images.size() > 0 ? "Images: " + images.toString() + "\n" : "") + message + "\n";
	}

	protected void addImage(Image image) {
		images.add(image);
	}

	protected void cleanup() {

		message = message.replace("<br />", "\n").replaceAll("\\<.*?>", "").trim();
		message = StringEscapeUtils.unescapeHtml4(message);

		mail = StringUtils.substringBetween(user, "<a href=\"mailto:", "\"");
		user = StringEscapeUtils.unescapeHtml4(user.replaceAll("\\<.*?>", ""));
		title = StringEscapeUtils.unescapeHtml4(title);

		if (title.length() == 0)
			title = null;
	}
}
