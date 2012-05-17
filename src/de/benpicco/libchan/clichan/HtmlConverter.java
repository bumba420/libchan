package de.benpicco.libchan.clichan;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import de.benpicco.libchan.imageboards.Image;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.imageboards.Tags;
import de.benpicco.libchan.util.FileUtil;
import de.benpicco.libchan.util.Logger;

public class HtmlConverter {
	final String	imgTemplate;
	final String	postTemplate;
	final String	threadHeader;

	public HtmlConverter(String templatedir) {
		String img = null;
		String post = null;
		String thread = null;

		try {
			img = FileUtil.fileToString(templatedir + "image.html");
			post = FileUtil.fileToString(templatedir + "post.html");
			thread = FileUtil.fileToString(templatedir + "thread-header.html");
		} catch (IOException e) {
			if (img == null)
				Logger.get().error("Can not read " + templatedir + "image.html");
			else if (post == null)
				Logger.get().error("Can not read " + templatedir + "post.html");
			else
				Logger.get().error("Can not read " + templatedir + "thread-header.html");
		}

		imgTemplate = img;
		postTemplate = post;
		threadHeader = thread;
	}

	public boolean isInizialised() {
		return imgTemplate != null && postTemplate != null && threadHeader != null;
	}

	/**
	 * taken from http://stackoverflow.com/questions/5386682
	 * 
	 * @param text
	 * @return
	 */
	private static String getLinkifiedText(String text) {
		try {
			Pattern patt = Pattern
					.compile("(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:\'\".,<>???“”‘’]))");
			Matcher matcher = patt.matcher(text);

			while (matcher.find())
				if (matcher.group(1).startsWith("http")) // also catches https
					text = matcher.replaceAll("<a href=\"$1\">$1</a>");
				else
					text = matcher.replaceAll("<a href=\"http://$1\">$1</a>");

		} catch (Exception e) {
		}
		return text;
	}

	private String n(String s) {
		return s == null ? "null" : s;
	}

	public String postToHtml(Post post) {
		final String user = post.mail == null ? post.user : "<a href=\"mailto:" + post.mail + "\">" + post.user
				+ "</a>";
		final String message = getLinkifiedText(StringEscapeUtils.escapeHtml4(post.message)
				.replaceAll("&gt;&gt;([0-9]+)", "<a href=\"#$1\" onClick=\"replyhl('$1');\">&gt;&gt;$1</a>")
				.replaceAll("(?m)^&gt;(.*)", "\n<span class=\"quote\">&gt;$1</span>").replace("\n", "<br>"));

		String images = "";
		for (Image img : post.images)
			images += imgTemplate.replace("$" + Tags.POST_IMGURL + "$", FileUtil.urlencode(img.url))
					.replace("$" + Tags.POST_THUMBNAIL + "$", StringEscapeUtils.escapeHtml4(img.thumbnailUrl))
					.replace("$" + Tags.POST_FILENAME + "$", StringEscapeUtils.escapeHtml4(img.filename));

		return postTemplate
				.replace("$" + Tags.POST_ID + "$", post.id + "")
				.replace("$" + Tags.POST_TITLE + "$", post.title != null ? post.title : "")
				.replace("$" + Tags.POST_COUNTRY + "$",
						post.countryball != null ? "<img src=\"" + post.countryball + "\">" : "")
				.replace("$" + Tags.POST_USER + "$", n(user))
				.replace("$" + Tags.POST_TRIP + "$", post.tripcode != null ? post.tripcode : "")
				.replace("$" + Tags.POST_DATE + "$", n(post.date)).replace("$IMAGES$", images)
				.replace("$" + Tags.POST_MESSAGE + "$", n(message));
	}

	public String getHeader(Post opening) {
		return threadHeader.replace("$" + Tags.POST_TITLE + "$", opening.title == null ? "Thread " + opening.id
				: opening.title);
	}
}
