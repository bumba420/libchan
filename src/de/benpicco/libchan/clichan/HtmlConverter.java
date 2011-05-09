package de.benpicco.libchan.clichan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import de.benpicco.libchan.imageboards.Image;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.util.Logger;

public class HtmlConverter {
	final String	imgTemplate;
	final String	postTemplate;
	final String	threadHeader;

	private static String fileToString(String file) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(new File(file)));
		StringBuilder out = new StringBuilder();

		String read = null;
		while (true) {
			read = in.readLine();
			if (read != null)
				out.append(read + "\n");
			else
				break;
		}

		in.close();
		return out.toString();
	}

	public HtmlConverter(String templatedir) {
		String img = null;
		String post = null;
		String thread = null;

		try {
			img = fileToString(templatedir + "image.html");
			post = fileToString(templatedir + "post.html");
			thread = fileToString(templatedir + "thread-header.html");
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

	public String postToHtml(Post post) {
		final String user = post.mail == null ? post.user : "<a href=\"mailto:" + post.mail + "\">" + post.user
				+ "</a>";
		final String message = getLinkifiedText(StringEscapeUtils.escapeHtml4(post.message)
				.replaceAll("&gt;&gt;([0-9]*)", "<a href=\"#$1\">&gt;&gt;$1</a>").replace("\n", "<br>"));

		String images = "";
		for (Image img : post.images)
			images += imgTemplate.replace("$IMGURL", img.url).replace("$THUMBNAIL", img.thumbnailUrl)
					.replace("$FILENAME", img.filename);

		return postTemplate.replace("$ID", post.id + "").replace("$TITLE", post.title != null ? post.title : "")
				.replace("$USER", user).replace("$DATE", post.date).replace("$IMAGES", images)
				.replace("$MESSAGE", message);
	}

	public String getHeader(Post opening) {
		return threadHeader.replace("$TITLE", opening.title == null ? "Thread " + opening.id : opening.title);
	}
}
