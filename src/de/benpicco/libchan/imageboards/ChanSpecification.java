package de.benpicco.libchan.imageboards;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import de.benpicco.libchan.streamparser.IParseDataReceiver;
import de.benpicco.libchan.util.FileUtil;
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
			readConfig(new LineReader(new FileReader(file)));
		} catch (FileNotFoundException e) {
			Logger.get().error("ChanSpecification File " + file + " does not exist.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String lastTag(String text, boolean first) {
		String lastTag = "";
		Matcher m = Pattern.compile("\\$([A-Z_]+)\\$").matcher(text);
		while (m.find()) {
			lastTag = m.group(1);
			if (first)
				break;
		}
		return lastTag;
	}

	public ChanSpecification(String templateDir, boolean local) {
		file = "LOCAL_HTML";
		supported.add(board);

		LinkedList<String> lines = new LinkedList<String>();

		lines.add(Tags.SITE_NAME + " local");
		lines.add(Tags.SITE_DESC + " local files");
		lines.add(Tags.SITE_URL + " file://");

		try {
			String image = FileUtil.fileToString(templateDir + "image.html").trim();
			lines.add(Tags.POST + " " + image);
			lines.add(Tags.END_IMAGE + " " + lastTag(image, false));

			String post = FileUtil.fileToString(templateDir + "post.html").trim();
			lines.add(Tags.POST + "  " + post);
			lines.add(Tags.START_POST + " " + lastTag(post, true));
			lines.add(Tags.END_POST + " " + lastTag(post, false));

			lines.add(Tags.URL_POSTFIX + " .html");

			readConfig(new LineReader(lines));
		} catch (IOException e) {
			Logger.get().error("Error parsing html template : " + e.getMessage());
		}
	}

	private void readConfig(LineReader reader) throws IOException {
		Pattern p = Pattern.compile("([A-Z_]+)(.+)");

		String line = reader.readLine();
		while (line != null) {
			if (!line.startsWith("#")) {
				Matcher matcher = p.matcher(line);
				if (matcher.matches()) {
					try {
						Tags key = Tags.valueOf(matcher.group(1));
						String value = matcher.group().substring(matcher.end(1)).trim();
						value = value.replace("\\n", "\n");
						value = value.replace("\\r", "\r");
						parsedString(key, value);
					} catch (IllegalArgumentException e) {
						Logger.get().error("Unknown Value '" + matcher.group(1) + "' in " + file);
					}
				}
			}
			line = reader.readLine();
		}
	}

	private void createCPI() {
		if (o.cpi == null)
			o.cpi = new CreatePostInformation();
	}

	@Override
	public void parsedString(Tags key, String data) {
		String value = null;
		if (data != null) {
			value = data.trim();
			if (value.startsWith("\"") && value.endsWith("\""))
				value = value.substring(1, value.length() - 1);
			value = StringEscapeUtils.unescapeJava(value);
		}

		try {
			switch (key) {
			case INCLUDE:
				try {
					String include = StringUtils.substringBeforeLast(file, File.separator) + File.separator + value;
					readConfig(new LineReader(new FileReader(include)));
				} catch (IOException e) {
					Logger.get().error("Error reading " + value + ": " + e.getMessage());
				}
				break;
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
			case NEW_POST_URL:
				createCPI();
				o.cpi.postUrl = data;
				break;
			case DEL_POST_URL:
				createCPI();
				o.cpi.deleteUrl = data;
				break;
			case NEW_POST_PARMS:
				createCPI();
				o.cpi.postParameter.add(data);
				break;
			case NEW_FILE_PARMS:
				createCPI();
				o.cpi.postFileParameter.add(data);
				break;
			case DEL_POST_PARMS:
				createCPI();
				o.cpi.delParameter.add(data);
				break;
			default:
				if (value == null) {
					Logger.get().error("Syntax error in " + file + ": " + key + " does have invalid value " + data);
					return;
				} else
					Logger.get().println("Warning: ununsed option " + key);
			}
		} catch (IllegalArgumentException e) {
			Logger.get().error("Error at key " + key + " in " + file + " (value: " + data + ")");
		}
	}

	public List<Imageboard> getSupported() {
		return supported;
	}

	public String name() {
		return file;
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
			if (chan.baseurl != null && url.startsWith(chan.baseurl))
				return new GenericImageBoardParser(url, chan.baseurl, new ParserOptions(o));
		return null;
	}

	/**
	 * will create an ImageBoardParser with the engine, no matter if it is
	 * supported by it or not.
	 * 
	 * @param url
	 * @return
	 */
	public GenericImageBoardParser forceGetImageBoardParser(String url) {
		final String http = "http://";
		final String baseUrl = http + StringUtils.substringBetween(url, http, "/");
		return new GenericImageBoardParser(url, baseUrl, new ParserOptions(o));
	}
}

class LineReader {
	final BufferedReader	reader;
	final Iterator<String>	iter;

	public LineReader(Reader reader) {
		this.reader = new BufferedReader(reader);
		this.iter = null;
	}

	public LineReader(List<String> lines) {
		this.reader = null;
		this.iter = lines.iterator();
	}

	public String readLine() throws IOException {
		if (reader != null)
			return reader.readLine();
		if (iter != null)
			return iter.hasNext() ? iter.next() : null;
		throw new IOException("No source specified.");
	}
}