package de.benpicco.libchan;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import de.benpicco.libchan.streamparser.IParseDataReceiver;
import de.benpicco.libchan.streamparser.StreamParser;

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
						user = StringUtils.substringBetween(str, "class=\"linkmail\">", "</a>");
					} else
						user = StringUtils.substringBetween(str, ">", "<");
					if (user != null)
						user = user.trim();
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
				System.out.println("-----[cut here]-----");
				System.out.println(str);
				System.out.println("-----[/cut here]-----");
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
				if (read > 0) {
					ByteBuffer bb = ByteBuffer.wrap(buffer, 0, read);
					builder.append(Charset.defaultCharset().decode(bb));
				}
			} while (read > 0);
		} catch (IOException e) {
			System.err.println("Failed downloading thread:");
			e.printStackTrace();
			return;
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
		(new ThreadsParser()).getThreads(responseStream, receiver);
	}
}

class ThreadsParser implements IParseDataReceiver {

	final static int		THREAD_URL	= 0;
	private PostReceiver	receiver;

	public void getThreads(InputStream responseStream, PostReceiver receiver) {
		this.receiver = receiver;
		StreamParser parser = new StreamParser(this);

		parser.addTag(THREAD_URL, "[<a href=\"res/", "\"");

		try {
			parser.parseStream(responseStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void parsedString(int id, String data) {
		switch (id) {
		case THREAD_URL:
			receiver.addThread(new Thread(null, "res/" + data, 0));
			break;

		default:
			break;
		}

	}
}
