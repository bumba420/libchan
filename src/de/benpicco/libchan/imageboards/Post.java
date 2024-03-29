package de.benpicco.libchan.imageboards;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.benpicco.libchan.util.FileUtil;
import de.benpicco.libchan.util.Misc;

public class Post implements Cloneable {
	public int					id;
	public String				title;
	public String				user;
	public String				uid;
	public String				tripcode;
	public String				mail;
	public String				message;
	public String				date;
	public final List<Image>	images;
	public String				countryball;
	public int					op;
	public String				threadUrl;		// let's only have this at the
												// OP post - is this a good
												// idea?

	public Post() {
		this.images = new LinkedList<Image>();
	}

	public String toString() {
		return "ID: " + id + "\n" + "Date: " + date + "\n" + (title != null ? "Title: " + title + "\n" : "") + "User: "
				+ (countryball != null ? " [" + countryball + "]" : "") + user + (uid != null ? " (" + uid + ")" : "")
				+ (tripcode != null ? " (" + tripcode + ")" : "") + "\n" + (mail != null ? "Mail: " + mail + "\n" : "")
				+ (images.size() > 0 ? "Images: " + images.toString() + "\n" : "") + message + "\n";
	}

	public void addImage(Image image) {
		images.add(image);
	}

	protected void cleanup() {

		message = message.replaceAll("(<br>|<br />|<p>|</p>)", "\n").replaceAll("\\<.*?>", "").trim();
		message = Misc.unescapeHtml(message);

		Iterator<Image> iter = images.iterator();
		while (iter.hasNext()) {
			final Image img = iter.next();
			img.cleanup();
			if (img.url == null)
				iter.remove();
		}

		mail = StringUtils.substringBetween(user, "<a href=\"mailto:", "\"");
		if (user != null)
			user = Misc.unescapeHtml(user.replaceAll("\\<.*?>", ""));
		else
			user = " ";

		if (tripcode != null) // there might be some leftover html from mail…
			tripcode = tripcode.replaceAll("\\<.*?>", "");

		if (date != null)
			date = Misc.unescapeHtml(date).trim();

		if (title != null)
			title = Misc.unescapeHtml(title).trim();

		if (title != null && title.length() == 0)
			title = null;

		if (uid != null && uid.length() == 0)
			uid = null;
	}

	public String getDir() {
		String ret = FileUtil.filterInvalidChars(user + (uid != null ? " " + uid : "")
				+ (tripcode != null ? tripcode : ""), " ");
		if (ret.length() == 0)
			ret = "(undefined)";
		return ret;
	}

	public boolean equals(Object o) {
		return o instanceof Post && equals(this, (Post) o);
	}

	public boolean isFirstPost() {
		return op == id;
	}

	private static boolean saveEquals(Object a, Object b) {
		if (a == null && b == null)
			return true;
		if (a == null || b == null)
			return false;
		return a.equals(b);
	}

	public static boolean equals(Post p1, Post p2) {
		if (p1.images.size() != p2.images.size())
			return false;

		for (int i = 0; i < p1.images.size(); ++i)
			if (!Image.equals(p1.images.get(i), p2.images.get(i)))
				return false;

		return p1.id == p2.id && saveEquals(p1.title, p2.title) && saveEquals(p1.user, p2.user)
				&& saveEquals(p1.tripcode, p2.tripcode) && saveEquals(p1.mail, p2.mail)
				&& saveEquals(p1.message, p2.message) && saveEquals(p1.date, p2.date)
				&& saveEquals(p1.countryball, p2.countryball) && saveEquals(p1.uid, p2.uid) && p1.op == p2.op;
	}

	public static Post localisePost(Post post, String thumbs) {
		if (post.images.size() == 0)
			return post;

		Post newPost = new Post();
		newPost.id = post.id;
		newPost.title = post.title;
		newPost.user = post.user;
		newPost.mail = post.mail;
		newPost.message = post.message;
		newPost.date = post.date;
		newPost.countryball = post.countryball;
		newPost.tripcode = post.tripcode;
		newPost.uid = post.uid;

		for (Image img : post.images) {
			Image newImg = new Image();
			newImg.filename = img.filename;
			newImg.url = post.getDir() + File.separator + img.filename;
			newImg.thumbnailUrl = thumbs + StringUtils.substringAfterLast(img.thumbnailUrl, "/");
			newPost.addImage(newImg);
		}

		return newPost;
	}
}
