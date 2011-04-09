package de.benpicco.libchan.clichan;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import de.benpicco.libchan.imageboards.Image;
import de.benpicco.libchan.imageboards.Post;

public class HtmlConverter {
	final String	imgTemplate;
	final String	postTemplate;
	final String	threadHeader;

	private static String fileToString(String file) throws IOException {
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(file))));
		StringBuilder out = new StringBuilder();

		while (in.available() > 0)
			out.append(in.readLine() + "\n");

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
				System.err.println("Can not read " + templatedir + "image.html");
			else if (post == null)
				System.err.println("Can not read " + templatedir + "post.html");
			else
				System.err.println("Can not read " + templatedir + "thread-header.html");
		}

		imgTemplate = img;
		postTemplate = post;
		threadHeader = thread;
	}

	public String postToHtml(Post post) {
		final String user = post.mail == null ? post.user : "<a href=\"" + post.mail + "\">" + post.user + "</a>";
		final String message = post.message.replaceAll(">>([0-9]*)", "<a href=\"#$1\">&gt;&gt;$1</a>").replace("\n",
				"<br>");

		String images = "";
		for (Image img : post.images)
			images += imgTemplate.replace("$IMGURL", img.url).replace("$THUMBNAIL", img.thumbnailUrl)
					.replace("$FILENAME", img.filename);

		return postTemplate.replace("$ID", post.id + "").replace("$USER", user).replace("$DATE", post.date)
				.replace("$IMAGES", images).replace("$MESSAGE", message);
	}

	public DataOutputStream threadToHtml(Post opening, String dir) throws IOException {
		DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(dir + opening.id + ".html")));

		out.writeUTF(threadHeader.replace("$TITLE", opening.title == null ? "Thread " + opening.id : opening.title));

		return out;
	}
}
