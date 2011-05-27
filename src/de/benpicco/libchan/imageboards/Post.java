package de.benpicco.libchan.imageboards;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import de.benpicco.libchan.util.FileUtil;

public class Post implements Cloneable {
	public int					id;
	public boolean				isFirstPost;
	public String				title;
	public String				user;
	public String				tripcode;
	public String				mail;
	public String				message;
	public String				date;
	public final List<Image>	images;
	public String				countryball;
	public int					op;
	public int					bytePos;

	public Post() {
		this.images = new LinkedList<Image>();
	}

	public String toString() {
		return "ID: " + id + "\n" + "Date: " + date + "\n" + (title != null ? "Title: " + title + "\n" : "") + "User: "
				+ user + (tripcode != null ? " (" + tripcode + ")" : "") + "\n"
				+ (mail != null ? "Mail: " + mail + "\n" : "")
				+ (images.size() > 0 ? "Images: " + images.toString() + "\n" : "") + message + "\n";
	}

	public void addImage(Image image) {
		images.add(image);
	}

	protected void cleanup() {

		message = message.replaceAll("(<br>|<br />|<p>|</p>)", "\n").replaceAll("\\<.*?>", "").trim();
		message = StringEscapeUtils.unescapeHtml4(message);

		Iterator<Image> iter = images.iterator();
		while (iter.hasNext())
			if (iter.next().url == null)
				iter.remove();

		mail = StringUtils.substringBetween(user, "<a href=\"mailto:", "\"");
		if (user != null)
			user = StringEscapeUtils.unescapeHtml4(user.replaceAll("\\<.*?>", ""));

		title = StringEscapeUtils.unescapeHtml4(title);

		if (title != null && title.length() == 0)
			title = null;
	}

	public String getDir() {
		String ret = FileUtil.filterInvalidChars(user + (tripcode != null ? tripcode : ""), " ");
		if (ret.length() == 0)
			ret = "(undefined)";
		return ret;
	}
}
