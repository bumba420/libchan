package de.benpicco.libchan.clichan;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import de.benpicco.libchan.IImageBoardParser;
import de.benpicco.libchan.imageboards.ChanSpecification;

public class ChanManager {

	private List<ChanSpecification>	chans;

	public ChanManager(String configDirectory) {
		chans = new LinkedList<ChanSpecification>();

		for (String file : new File(configDirectory).list())
			if (file.endsWith(".chan"))
				chans.add(new ChanSpecification(configDirectory + file));
	}

	public IImageBoardParser getParser(String url) {
		IImageBoardParser ret = null;
		for (ChanSpecification chan : chans) {
			ret = chan.getImageBoardParser(url);
			if (ret != null)
				break;
		}
		return ret;
	}
}