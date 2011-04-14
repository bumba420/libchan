package de.benpicco.libchan.imageboards;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import de.benpicco.libchan.streamparser.IParseDataReceiver;
import de.benpicco.libchan.streamparser.StreamParser;
import de.benpicco.libchan.util.Tuple;

public class ChanSpecification implements IParseDataReceiver {

	private List<Tags>					postStarter		= new ArrayList<Tags>();
	private List<Tags>					postEnder		= new ArrayList<Tags>();
	private List<Tags>					imageEnder		= new ArrayList<Tags>();
	private List<Tuple<String, String>>	supported		= new ArrayList<Tuple<String, String>>();
	private StreamParser				parser			= new StreamParser();
	private String						thumbPrefix		= "";
	private String						imgPrefix		= "";
	private String						countryPrefix	= "";
	private Tuple<String, String>		threadURL		= null;

	private final String				file;

	public ChanSpecification(String file) {
		this.file = file;

		StreamParser configParser = new StreamParser();
		for (Tags t : Tags.values())
			configParser.addTag(t, t.toString(), "\n");
		try {
			configParser.parseStream(new FileInputStream(file), this);
		} catch (FileNotFoundException e) {
			System.err.println("File " + file + " does not exist.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void parsedString(Tags tag, String data) {
		Pattern p = Pattern.compile("(?<!\\\\)\"(.*?)(?<!\\\\)\"");
		Matcher m = p.matcher(data);
		String first = null;
		String second = null;
		if (m.find())
			first = StringEscapeUtils.unescapeJava(m.group().substring(1, m.group().length() - 1));
		if (m.find())
			second = StringEscapeUtils.unescapeJava(m.group().substring(1, m.group().length() - 1));

		switch (tag) {
		case BOARD_URL:
			supported.add(new Tuple<String, String>(first, second));
			break;
		case URL_SCHEME:
			threadURL = new Tuple<String, String>(first, second);
			break;
		case START_POST:
			postStarter.add(Tags.valueOf(data.trim()));
			break;
		case END_POST:
			postEnder.add(Tags.valueOf(data.trim()));
			break;
		case END_IMAGE:
			imageEnder.add(Tags.valueOf(data.trim()));
			break;
		case THUMBNAIL_PREFIX:
			thumbPrefix = first;
			break;
		case IMAGE_PREFIX:
			imgPrefix = first;
			break;
		case COUNTRY_PREFIX:
			countryPrefix = first;
			break;
		default:
			if (first == null || second == null) {
				System.err.println("Malformed configuration for " + tag + " in " + file);
				return;
			}
			parser.addTag(tag, first, second);
		}
	}

	public List<Tuple<String, String>> getSupported() {
		return supported;
	}

	/**
	 * A ChanSpecification can be valid for many websites using the same
	 * software. This function generates a parser for a specific site.
	 * 
	 * @param key
	 *            either the url or the name of the imageboard website
	 * @return
	 */
	public AsyncImageBoardParser getImageBoardParser(String key) {
		for (Tuple<String, String> chan : supported)
			if (key.startsWith(chan.first) || key.equals(chan.second))
				return new AsyncImageBoardParser(chan.first, postStarter, postEnder, imageEnder, parser.clone(),
						imgPrefix, thumbPrefix, countryPrefix, threadURL);
		return null;
	}
}