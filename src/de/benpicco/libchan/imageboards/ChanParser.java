package de.benpicco.libchan.imageboards;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import de.benpicco.libchan.streamparser.IParseDataReceiver;
import de.benpicco.libchan.streamparser.StreamParser;

public class ChanParser implements IParseDataReceiver {
	static enum Tags {
		POST_ID, POST_USER, POST_TITLE, POST_THUMBNAIL, POST_IMGURL, POST_FILENAME, POST_DATE, POST_MESSAGE, BOARD_URL, END_POST
	}

	public ChanParser(String file) {
		StreamParser parser = new StreamParser(this);
		for (Tags t : Tags.values())
			parser.addTag(t.ordinal(), t.toString(), "\n");

		try {
			parser.parseStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			System.err.println("File " + file + "does not exist.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void parsedString(int id, String data) {
		if (id > Tags.values().length)
			return;

		Pattern p = Pattern.compile("(?<!\\\\)\"(.*?)(?<!\\\\)\"");
		Matcher m = p.matcher(data);
		String first = null;
		String second = null;
		if (m.find())
			first = StringEscapeUtils.unescapeJava(m.group().substring(1, m.group().length() - 1));
		if (m.find())
			second = StringEscapeUtils.unescapeJava(m.group().substring(1, m.group().length() - 1));

		switch (Tags.values()[id]) {
		case BOARD_URL:
			System.out.println("Support for " + second);
			break;
		case END_POST:
			Tags endPost = Tags.valueOf(data.trim());
			System.out.println(endPost + " ends a message");
			break;
		default:

			if (first == null || second == null) {
				System.err.println("Malformed configuration for " + Tags.values()[id].name());
				return;
			}
			System.out.println(id + ": [" + first + ", " + second + "]");
		}
	}
}
