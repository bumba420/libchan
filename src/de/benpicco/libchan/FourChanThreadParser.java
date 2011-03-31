package de.benpicco.libchan;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class FourChanThreadParser implements ThreadParser {

	private Post parsePost(String s) {
		String imgUrl = null;
		String thumbnail = null;
		String filename = null;
		String mail = ""; // TODO: parse
		String title = "";
		String user = "";
		String date = "";
		String message = "";
		int id = 0;
		List<Image> images = new LinkedList<Image>();

		for (String str : s.split("<span ")) {
			try {
				if (str.contains("form"))
					continue;
				if (str.contains("File"))
					imgUrl = str.split("<a href=\"")[1].split("\"")[0];
				if (str.contains("postername")) {
					user = str.split(">")[1].split("<")[0];
					String[] spans = str.split("</span>");
					if (spans[1].trim().length() > 0) // </span></span> issue
						date = str.split("</span>")[1];
					else
						date = str.split("</span>")[2];
				}
				if (str.contains("norep")) {
					String sid = str.split("id=\"norep")[1].split("\">")[0];
					id = Integer.parseInt(sid);
				}
				if (str.contains("replytitle"))
					title = str.split(">")[1].split("<")[0];
				else if (str.contains("title"))
					filename = str.split(">")[1].split("<")[0];

				if (str.contains("img src"))
					thumbnail = str.split("img src=")[1].split(" border")[0];
				if (str.contains("blockquote")) {
					message = str.split("blockquote>")[1].split("</blockquote")[0];
					message = message.replace("<br />", "\n");
				}
			} catch (Exception e) {
				System.err.print(e);
			}
		}

		if (imgUrl != null)
			images.add(new Image(thumbnail, imgUrl, filename));

		return new Post(id, date.trim(), (title.length() == 0 ? null
				: title.trim()), user.trim(), (mail.length() == 0 ? null
				: mail.trim()), message.trim(), images);
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
