package de.benpicco.libchan.streamparser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
			cpyTags.add(new ParseItem(t.tags, t.pattern));
		return new StreamParser(cpyTags);
	}

	public void parseStream(InputStream stream, IParseDataReceiver receiver) throws IOException {
		System.out.println("parsing stream");
		InputStreamReader reader = new InputStreamReader(stream);

		char[] buffer = new char[512];
		int read = 0;

		while (read >= 0) {
			read = reader.read(buffer);

			for (int i = 0; i < read; ++i)
				for (ParseItem pi : tags)
					if (pi.match(buffer[i])) {
						for (int j = 0; j < pi.tags.length; ++j)
							System.out.println(pi.tags[j] + " - " + pi.items[j]);
						// receiver.parsedString(pi.tags[j], pi.items[j]);
						break;
					}

		}
		System.out.println("Done parsing stream");
	}

	public void addTag(String pattern) {
		tags.add(new ParseItem(pattern));
	}
}

class ParseItem {
	private int				count		= 0;
	private int				item		= 0;
	private StringBuilder	itemBuilder	= null;
	private int				lastItem	= 0;

	final String[]			items;
	final char[]			pattern;
	final Tags[]			tags;

	ParseItem(Tags[] tags, char[] pattern) {
		this.pattern = pattern;
		this.tags = tags;
		items = new String[tags.length];
	}

	public ParseItem(String pattern) {
		Matcher match = Pattern.compile("\\$([A-Z_]+)\\$").matcher(pattern);
		StringBuilder sb = new StringBuilder();
		int lastMatch = 0;

		if (match.find()) {
			tags = new Tags[match.groupCount()];
			for (int i = 0; i < match.groupCount(); ++i) {
				tags[i] = Tags.valueOf(match.group(i + 1));
				sb.append(pattern.substring(lastMatch, match.start()));
				sb.append((char) 0);
				lastMatch = match.end();
			}
			sb.append(pattern.substring(lastMatch));
		} else
			tags = null;

		this.pattern = sb.toString().toCharArray();
		items = new String[tags.length];
	}

	public boolean match(char c) {
		if (pattern.length == 0)
			return false;

		if (pattern[count] == c)
			++count;
		else if (pattern[count] == 0) {
			// System.out.println("ok[" + count + "]: " + new String(pattern));
			if (itemBuilder != null && count > lastItem) {
				System.out.println("ok");
				items[item] = itemBuilder.substring(0, itemBuilder.length() - (count - lastItem));
				// System.out.println(tags[item] + " - " + items[item]);
				item++;
			} else
				itemBuilder = new StringBuilder();

			lastItem = ++count;
			if (pattern[count] == c)
				++count;
		} else {
			count = lastItem;
			// System.out.println("warp-around, setting back to " + count);
		}

		if (itemBuilder != null)
			itemBuilder.append(c);

		if (count >= pattern.length) {
			// System.out.println("deleteing " + (count - lastItem));
			items[item] = itemBuilder.substring(0, itemBuilder.length() - (count - lastItem));
			// for (int i = 0; i < items.length; ++i)
			// System.out.println(tags[i] + " - " + items[i]);

			count = 0;
			lastItem = 0;
			item = 0;
			itemBuilder = null;
			return true;
		}
		return false;
	}
}
