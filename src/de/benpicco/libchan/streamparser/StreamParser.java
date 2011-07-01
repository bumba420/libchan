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
	private int						bytesRead;
	private boolean					parsing;
	private BackgroundDownloader	reader;

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

	private void reset() {
		for (ParseItem item : tags)
			item.reset();
	}

	public synchronized void parseStream(InputStream stream, IParseDataReceiver receiver) throws IOException {
		reset();
		reader = new BackgroundDownloader(512, new InputStreamReader(stream));

		int read = 0;
		bytesRead = 0;
		parsing = true;

		while (read >= 0 && parsing) {
			Chunk c = reader.get();
			if (c == null)
				break;
			char[] buffer = c.data;
			read = c.len;

			for (int i = 0; i < read; ++i) {
				++bytesRead;
				for (ParseItem pi : tags)
					if (pi.match(buffer[i]))
						for (int j = 0; j < pi.tags.length; ++j)
							if (parsing)
								receiver.parsedString(pi.tags[j], pi.items[j]);
			}
		}
		parsing = false;
	}

	public void addTag(String pattern) {
		tags.add(new ParseItem(pattern));
	}

	/**
	 * @return the current position in the stream in bytes
	 */
	public int getPos() {
		return bytesRead;
	}

	/**
	 * forces the parsing to stop before the end of the stream is reached
	 */
	public void halt() {
		parsing = false;
		if (reader != null)
			reader.stop();
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
		Matcher match = Pattern.compile("\\$([A-Z_]*)\\$").matcher(pattern);
		StringBuilder sb = new StringBuilder();
		int lastMatch = 0;

		ArrayList<Tags> tags = new ArrayList<Tags>();

		while (match.find()) {
			if (match.group(1).length() == 0)
				tags.add(Tags.NULL);
			else
				tags.add(Tags.valueOf(match.group(1)));
			sb.append(pattern.substring(lastMatch, match.start()));
			sb.append((char) 0);
			lastMatch = match.end();
		}
		sb.append(pattern.substring(lastMatch));

		this.pattern = sb.toString().toCharArray();
		this.tags = tags.toArray(new Tags[tags.size()]);
		items = new String[this.tags.length];
	}

	public boolean match(char c) {
		if (pattern.length == 0)
			return false;

		if (pattern[count] == c)
			++count;
		else if (pattern[count] == 0) {
			if (itemBuilder != null && count > lastItem) {
				items[item] = itemBuilder.substring(0, itemBuilder.length() - (count - lastItem));
				item++;
			}
			if (count > lastItem)
				itemBuilder = new StringBuilder();

			lastItem = ++count;
			if (pattern[count] == c)
				++count;
		} else {
			count = lastItem;
		}

		if (itemBuilder != null)
			itemBuilder.append(c);

		if (count >= pattern.length) {
			items[item] = itemBuilder.substring(0, itemBuilder.length() - (count - lastItem));

			reset();
			return true;
		}
		return false;
	}

	void reset() {
		count = 0;
		lastItem = 0;
		item = 0;
		itemBuilder = null;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		int j = 0;
		for (int i = 0; i < pattern.length; ++i)
			if (pattern[i] > 0)
				sb.append(pattern[i]);
			else
				sb.append(tags[j++]);
		return sb.toString();
	}
}
