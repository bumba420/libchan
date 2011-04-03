package de.benpicco.libchan.streamparser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.benpicco.libchan.imageboards.Tags;

public class StreamParser {
	private final List<ParseItem>	tags;

	public StreamParser() {
		tags = new ArrayList<ParseItem>();
	}

	public void parseStream(InputStream stream, IParseDataReceiver receiver) throws IOException {
		int character = 0;
		ParseItem match = null;
		StringBuilder builder = new StringBuilder();
		while (character >= 0) {
			character = stream.read();

			if (character < 0)
				break;

			if (match != null) {
				builder.append((char) character);
				if (match.match((char) character)) {
					builder.delete(builder.length() - match.trailing(), builder.length());

					receiver.parsedString(match.tag, builder.toString());

					builder = new StringBuilder();
					match = null;
				}
			} else {
				for (ParseItem pi : tags)
					if (pi.match((char) character)) {
						match = pi;
						break;
					}
			}
		}
	}

	public void addTag(Tags tag, String start, String end) {
		tags.add(new ParseItem(tag, start, end));
	}
}

class ParseItem {
	int					count	= 0;
	boolean				open	= false;

	public final Tags	tag;
	final char[]		start;
	final char[]		end;

	public ParseItem(Tags tag, String start, String end) {
		this.start = start.toCharArray();
		this.end = end.toCharArray();
		this.tag = tag;
	}

	public boolean match(char c) {
		char[] tag = open ? end : start;

		if (tag[count] == c)
			++count;
		else
			count = 0;

		if (count == tag.length) {
			count = 0;
			open = !open;
			return true;
		}
		return false;
	}

	public int trailing() {
		return end.length;
	}
}
