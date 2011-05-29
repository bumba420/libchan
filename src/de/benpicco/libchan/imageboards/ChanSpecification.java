package de.benpicco.libchan.imageboards;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.benpicco.libchan.streamparser.IParseDataReceiver;
import de.benpicco.libchan.util.Logger;

public class ChanSpecification implements IParseDataReceiver {
	private List<Imageboard>	supported	= new ArrayList<Imageboard>();
	private Imageboard			board		= new Imageboard();
	private ParserOptions		o			= new ParserOptions();

	private final String		file;

	public ChanSpecification(String file) {
		this.file = file;
		supported.add(board);

		try {
			readConfig(file);
		} catch (FileNotFoundException e) {
			Logger.get().error("File " + file + " does not exist.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readConfig(String file) throws IOException {
		Pattern p = Pattern.compile("([A-Z_]+)(.+)");
		BufferedReader reader = new BufferedReader(new FileReader(file));

		String line = reader.readLine();
		while (line != null) {
			if (!line.startsWith("#")) {
				Matcher matcher = p.matcher(line);
				if (matcher.matches()) {
					Tags key = Tags.valueOf(matcher.group(1));
					String value = matcher.group().substring(matcher.end(1)).trim();
					parsedString(key, value);
				}
			}
			line = reader.readLine();
		}

	}

	@Override
	public void parsedString(Tags key, String data) {
		String value = null;
		if (data != null)
			value = data.trim();

		try {
			switch (key) {
			case SITE_NAME:
				if (board.name != null) {
					board = new Imageboard();
					supported.add(board);
				}
				board.name = value;
				break;
			case SITE_DESC:
				if (board.description != null) {
					board = new Imageboard();
					supported.add(board);
				}
				board.description = value;
				break;
			case SITE_URL:
				if (board.baseurl != null) {
					board = new Imageboard();
					supported.add(board);
				}
				board.baseurl = value;
				break;
			case URL_PREFIX:
				o.threadURL.first = value;
				break;
			case URL_POSTFIX:
				o.threadURL.second = value;
				break;
			case START_POST:
				o.postStarter.add(Tags.valueOf(value));
				break;
			case END_POST:
				o.postEnder.add(Tags.valueOf(value));
				break;
			case END_IMAGE:
				o.imageEnder.add(Tags.valueOf(value));
				break;
			case THUMBNAIL_PREFIX:
				o.thumbPrefix = value;
				break;
			case IMAGE_PREFIX:
				o.imgPrefix = value;
				break;
			case COUNTRY_PREFIX:
				o.countryPrefix = value;
				break;
			case BOARD:
				o.boardParser.addTag(value);
				break;
			case BOARD_INDEX:
				o.boardIndex = value;
				break;
			case POST:
				o.parser.addTag(value);
				break;
			case POST_THREAD_MARK:
				o.threadMark = value;
				break;
			default:
				if (value == null) {
					Logger.get().error("Syntax error in " + file + ": " + key + " does have invalid value " + data);
					return;
				} else
					Logger.get().println("Warning: ununsed option " + key);
			}
		} catch (IllegalArgumentException e) {
			Logger.get().error("Invalid key " + key + " in " + file);
		}
	}

	public List<Imageboard> getSupported() {
		return supported;
	}

	/**
	 * A ChanSpecification can be valid for many websites using the same
	 * software. This function generates a parser for a specific site.
	 * 
	 * @param key
	 *            either the url of the site to be parsed
	 * @return
	 */
	public GenericImageBoardParser getImageBoardParser(String url) {
		for (Imageboard chan : supported)
			if (url.startsWith(chan.baseurl))
				return new GenericImageBoardParser(url, chan.baseurl, new ParserOptions(o));
		return null;
	}
}