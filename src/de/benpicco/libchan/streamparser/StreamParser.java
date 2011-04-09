package de.benpicco.libchan.streamparser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.benpicco.libchan.imageboards.Tags;

public class StreamParser implements Cloneable {
	private final List<ParseItem>	tags;

	public StreamParser() {
		tags = new ArrayList<ParseItem>();
	}

	private StreamParser(List<ParseItem> tags) {
		this.tags = tags;
	}

	public StreamParser clone() {
		List<ParseItem> cpyTags = new ArrayList<ParseItem>(tags.size());
		for (ParseItem t : tags)
			cpyTags.add(new ParseItem(t.tag, new String(t.start), new String(t.end)));
		return new StreamParser(cpyTags);
	}

	public void parseStream(InputStream stream, IParseDataReceiver receiver) throws IOException {
		InputStreamReader reader = new InputStreamReader(stream);

		char[] buffer = new char[512];
		int read = 0;

		ParseItem match = null;
		StringBuilder builder = new StringBuilder();
		while (read >= 0) {
			read = reader.read(buffer);

			for (int i = 0; i < read; ++i)
				if (match != null) {
					builder.append(buffer[i]);
					if (match.match(buffer[i])) {
						builder.delete(builder.length() - match.trailing(), builder.length());

						receiver.parsedString(match.tag, builder.toString());

						builder = new StringBuilder();
						match = null;
					}
				} else {
					for (ParseItem pi : tags)
						if (pi.match(buffer[i])) {
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
		this(tag, start.toCharArray(), end.toCharArray());
	}

	private ParseItem(Tags tag, char[] start, char[] end) {
		this.start = start;
		this.end = end;
		this.tag = tag;
	}

	public boolean match(char c) {
		char[] tag = open ? end : start;

		if (tag.length > 0 && tag[count] == c)
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
