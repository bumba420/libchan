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

		bytesRead = 0;
		parsing = true;
		Chunk c;

		while ((c = reader.get()) != null && parsing) {
			for (int i = 0; i < c.len; ++i) {
				++bytesRead;
				for (ParseItem pi : tags)
					if (pi.match(c.data[i]))
						for (int j = 0; j < pi.tags.length && parsing; ++j)
							if (pi.tags[j] != Tags.NULL)
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
	private int			pos			= 0;
	private int			pos2		= -1;
	private int			item		= 0;
	private ItemBuilder	itemBuilder	= null;
	private int			lastItem	= 0;
	private int			lastItem2	= 0;
	private int			prevItemPos	= 0;

	final String[]		items;
	final char[]		pattern;
	final Tags[]		tags;

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

	private boolean checkMatch(char c) {
		if (pattern[pos] == 0) {
			if (pos > lastItem) { // we reached a new matcher/the end
				if (itemBuilder != null)
					items[item++] = itemBuilder.substring(0, itemBuilder.length() - (pos - lastItem));
				itemBuilder = new ItemBuilder(tags[item] == Tags.NULL);
			}

			// start looking for the beginning of the same pattern again
			lastItem2 = 0;
			prevItemPos = 0;
			pos2 = lastItem2;

			lastItem = ++pos;
		}

		if (pattern[pos] == c)
			++pos;
		else
			pos = lastItem;

		if (itemBuilder != null)
			itemBuilder.append(c);

		if (pos >= pattern.length) { // pattern finished
			items[item] = itemBuilder.substring(0, itemBuilder.length() - (pos - lastItem));

			reset();
			return true;
		}
		return false;
	}

	private void checkLaterMatch(char c) {
		if (pos2 < 0)
			return;

		if (pattern[pos2] == 0) {
			itemBuilder = null;
			pos = pos2;
			lastItem = 0; // has to be smaller than pos
			item = prevItemPos;
		}

		if (pattern[pos2] == c)
			++pos2;
		else
			pos2 = lastItem2;
	}

	/**
	 * @param c
	 *            Character to match
	 * @return true when the entire pattern is complete, results can be found in
	 *         tags and items members.
	 */
	public boolean match(char c) {
		if (pattern.length == 0)
			return false;

		checkLaterMatch(c);
		return checkMatch(c);
	}

	void reset() {
		pos = 0;
		pos2 = -1;
		lastItem = 0;
		item = 0;
		itemBuilder = null;
		prevItemPos = 0;
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

/**
 * Simple wrapper around StringBuilder, discards all chars if a NULL dummy item
 * is parsed.
 */
class ItemBuilder {
	private final StringBuilder	sb;

	public ItemBuilder(boolean noop) {
		if (noop)
			sb = null;
		else
			sb = new StringBuilder();
	}

	public void append(char c) {
		if (sb != null)
			sb.append(c);
	}

	public int length() {
		if (sb != null)
			return sb.length();
		return 0;
	}

	public String substring(int i, int j) {
		if (sb != null)
			return sb.substring(i, j);
		return null;
	}

}
