package de.benpicco.libchan.imageboards;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import de.benpicco.libchan.IImageBoardParser;
import de.benpicco.libchan.streamparser.IParseDataReceiver;
import de.benpicco.libchan.streamparser.StreamParser;

public class ChanSpecification implements IParseDataReceiver {

	private List<Tags>		threadStarter	= new ArrayList<Tags>();
	private List<Tags>		postStarter		= new ArrayList<Tags>();
	private List<Tags>		postEnder		= new ArrayList<Tags>();
	private List<Tags>		imageEnder		= new ArrayList<Tags>();
	private List<String>	supported		= new ArrayList<String>();
	private StreamParser	parser			= new StreamParser();
	private String			thumbPrefix		= "";
	private String			imgPrefix		= "";

	public ChanSpecification(String file) {
		StreamParser configParser = new StreamParser();
		for (Tags t : Tags.values())
			configParser.addTag(t, t.toString(), "\n");
		try {
			configParser.parseStream(new FileInputStream(file), this);
		} catch (FileNotFoundException e) {
			System.err.println("File " + file + "does not exist.");
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
			supported.add(first);
			break;
		case START_THREAD:
			threadStarter.add(Tags.valueOf(data.trim()));
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
		default:
			if (first == null || second == null) {
				System.err.println("Malformed configuration for " + tag);
				return;
			}
			parser.addTag(tag, first, second);
		}
	}

	public IImageBoardParser getImageBoardParser() {
		String url = supported.get(0); // XXX
		return new GenericImageBoardParser(url, threadStarter, postStarter, postEnder, imageEnder, parser, imgPrefix,
				thumbPrefix);
	}
}