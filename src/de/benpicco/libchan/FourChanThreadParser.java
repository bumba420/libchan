package de.benpicco.libchan;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

public class FourChanThreadParser implements ThreadParser {

	private Post parsePost(String s) {
		String imgUrl = null;
		String thumbnail = null;
		String filename = null;
		String mail = "";
		String title = "";
		String user = "";
		String date = "";
		String message = "";
		int id = 0;
		List<Image> images = new LinkedList<Image>();

		for (String str : s.split("<span ")) {
			try {
				if (str.contains("postername\">")) {
					mail = StringUtils.substringBetween(str, "<a href=\"mailto:", "\"");
					if (mail != null) {
						mail = mail.trim();
						user = StringUtils.substringBetween(str, "class=\"linkmail\">", "</a>").trim();
					} else
						user = StringUtils.substringBetween(str, ">", "<").trim();
					date = StringUtils.substringAfter(str, "</span> ").trim();
				} else if (str.contains("id=\"norep")) {
					id = Integer.parseInt(StringUtils.substringBetween(str, "id=\"norep", "\""));
				} else if (str.contains("id=\"nothread")) {
					id = Integer.parseInt(StringUtils.substringBetween(str, "id=\"nothread", "\""));
				} else if (str.contains("replytitle\">") || str.contains("filetitle\">")) {
					title = StringUtils.substringBetween(str, ">", "<");
					if (title != null)
						title = title.trim();
					if (title.length() == 0)
						title = null;
				} else if (str.contains("title=")) {
					filename = StringUtils.substringBetween(str, "\"", "\"");
					thumbnail = StringUtils.substringBetween(str, "<img src=", " ");
					imgUrl = StringUtils.substringBetween(str, "<a href=\"", "\"");
				}
				if (str.contains("<blockquote>")) {
					message = StringUtils.substringBetween(str, "<blockquote>", "</blockquote>").trim();
					message = message.replace("<br />", "\n").replaceAll("\\<.*?>", "");
					message = StringEscapeUtils.unescapeHtml4(message);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (imgUrl != null)
			images.add(new Image(thumbnail, imgUrl, filename));

		return new Post(id, date, title, user, mail, message, images);
	}

	public void parseThread(InputStream responseStream, PostReceiver receiver) {

		StringBuilder builder = new StringBuilder();
		int read = 0;
		try {
			do {
				byte[] buffer = new byte[2048];
				read = responseStream.read(buffer);
				builder.append(new String(buffer));
			} while (read > 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String data = builder.toString();

		String[] splitted = data.split("<hr>");
		if (splitted.length < 3)
			return;
		data = splitted[3];
		splitted = data.split("<table>");

		for (String s : splitted)
			receiver.addPost(parsePost(s));

		receiver.parsingDone();
	}

	public void getThreads(InputStream responseStream, PostReceiver receiver) {
		StringBuilder builder = new StringBuilder();
		int read = 0;
		try {
			do {
				byte[] buffer = new byte[2048];
				read = responseStream.read(buffer);
				builder.append(new String(buffer));
			} while (read > 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String data = builder.toString();
		for (String s : data.split("[<a href")) {
			String url = s.split("\"")[1].split("\"")[0];
			receiver.addThread(new Thread(null, url, 0));
		}
	}
}
