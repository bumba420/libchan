package de.benpicco.libchan.imageboards;

import java.util.ArrayList;
import java.util.List;

import de.benpicco.libchan.streamparser.StreamParser;
import de.benpicco.libchan.util.Tuple;

public class ParserOptions {
	List<Tags>				postStarter		= new ArrayList<Tags>();
	List<Tags>				postEnder		= new ArrayList<Tags>();
	List<Tags>				imageEnder		= new ArrayList<Tags>();
	StreamParser			parser			= new StreamParser();
	StreamParser			boardParser		= new StreamParser();
	String					thumbPrefix		= "";
	String					imgPrefix		= "";
	String					countryPrefix	= "";
	Tuple<String, String>	threadURL		= new Tuple<String, String>("", "");
	String					threadMark		= "";
	String					boardIndex		= "";
	CreatePostInformation	cpi				= null;

	public ParserOptions() {
	}

	public ParserOptions(ParserOptions o) {
		postEnder = o.postEnder;
		postStarter = o.postStarter;
		imageEnder = o.imageEnder;
		parser = o.parser.clone();
		boardParser = o.boardParser.clone();
		thumbPrefix = o.thumbPrefix;
		imgPrefix = o.imgPrefix;
		countryPrefix = o.countryPrefix;
		threadURL = o.threadURL;
		threadMark = o.threadMark;
		boardIndex = o.boardIndex;
		if (o.cpi != null)
			cpi = o.cpi.clone();
	}
}
