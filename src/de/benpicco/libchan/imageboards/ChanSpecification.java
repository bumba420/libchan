package de.benpicco.libchan.imageboards;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

	private List<Tags>				postStarter		= new ArrayList<Tags>();
	private List<Tags>				postEnder		= new ArrayList<Tags>();
	private List<Tags>				imageEnder		= new ArrayList<Tags>();
	private List<Imageboard>		supported		= new ArrayList<Imageboard>();
	private StreamParser			parser			= new StreamParser();
	private StreamParser			boardParser		= new StreamParser();
	private String					thumbPrefix		= "";
	private String					imgPrefix		= "";
	private String					countryPrefix	= "";
	private Tuple<String, String>	threadURL		= new Tuple<String, String>("", "");
	private Imageboard				board			= new Imageboard();
	private String					threadMark		= "";

	private final String			file;

	public ChanSpecification(String file) {
		this.file = file;

		try {
			readConfig(file);
		} catch (FileNotFoundException e) {
			System.err.println("File " + file + " does not exist.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		supported.add(board);
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
		Pattern p = Pattern.compile("(?<!\\\\)\"(.*?)(?<!\\\\)\"");
		Matcher m = p.matcher(data);
		String value = null;
		if (m.find())
			value = StringEscapeUtils.unescapeJava(m.group().substring(1, m.group().length() - 1));
		else if (data != null)
			value = data.trim();

		// System.out.println(key + " - " + value);

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
			threadURL.first = value;
			break;
		case URL_POSTFIX:
			threadURL.second = value;
			break;
		case START_POST:
			postStarter.add(Tags.valueOf(value));
			break;
		case END_POST:
			postEnder.add(Tags.valueOf(value));
			break;
		case END_IMAGE:
			imageEnder.add(Tags.valueOf(value));
			break;
		case THUMBNAIL_PREFIX:
			thumbPrefix = value;
			break;
		case IMAGE_PREFIX:
			imgPrefix = value;
			break;
		case COUNTRY_PREFIX:
			countryPrefix = value;
			break;
		case BOARD:
			boardParser.addTag(value);
			break;
		case POST:
			parser.addTag(value);
			break;
		case POST_THREAD_MARK:
			threadMark = value;
			break;
		default:
			if (value == null) {
				System.err.println("Syntax error in " + file + ": " + key + " does have invalid value " + data);
				return;
			} else
				System.out.println("Warning: ununsed option " + key);
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
	 *            either the url or the name of the imageboard website
	 * @return
	 */
	public GenericImageBoardParser getImageBoardParser(String key) {
		for (Imageboard chan : supported)
			if (key.startsWith(chan.baseurl) || key.equals(chan.name))
				return new GenericImageBoardParser(chan.baseurl, postStarter, postEnder, imageEnder, parser.clone(),
						boardParser.clone(), threadMark, imgPrefix, thumbPrefix, countryPrefix, threadURL);
		return null;
	}
}