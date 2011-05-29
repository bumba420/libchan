package de.benpicco.libchan.clichan;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import de.benpicco.libchan.imageboards.ChanSpecification;
import de.benpicco.libchan.imageboards.GenericImageBoardParser;
import de.benpicco.libchan.imageboards.Imageboard;
import de.benpicco.libchan.util.Logger;

public class ChanManager {

	private List<ChanSpecification>	chans;

	public ChanManager(String configDirectory) {
		chans = new LinkedList<ChanSpecification>();

		File cfgdir = new File(configDirectory);
		if (cfgdir.exists()) {
			for (String file : cfgdir.list())
				if (file.endsWith(".chan"))
					chans.add(new ChanSpecification(configDirectory + file));
		} else
			Logger.get().error(configDirectory + " does not contain any imageboard specifications");

	}

	public synchronized GenericImageBoardParser getParser(String url) {
		GenericImageBoardParser ret = null;
		for (ChanSpecification chan : chans) {
			ret = chan.getImageBoardParser(url);
			if (ret != null)
				break;
		}
		return ret;
	}

	public List<Imageboard> getSupported() {
		LinkedList<Imageboard> boards = new LinkedList<Imageboard>();
		for (ChanSpecification chan : chans)
			boards.addAll(chan.getSupported());
		return boards;
	}
}
