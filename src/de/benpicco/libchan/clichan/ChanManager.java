package de.benpicco.libchan.clichan;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.benpicco.libchan.imageboards.ChanSpecification;
import de.benpicco.libchan.imageboards.GenericImageBoardParser;
import de.benpicco.libchan.imageboards.Image;
import de.benpicco.libchan.imageboards.Imageboard;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.util.FileUtil;
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

	public synchronized GenericImageBoardParser guessParser(String url) {
		PostCounter pc = new PostCounter();
		for (ChanSpecification chan : chans) {
			Logger.get().println("Trying " + chan.name() + "…");
			GenericImageBoardParser parser = chan.forceGetImageBoardParser(url);
			try {
				pc.reset();
				parser.setPostHandler(pc);
				parser.getPosts();
				if (pc.matches()) {
					Logger.get().println("Success!");
					return parser;
				}
			} catch (Exception e) {
				continue;
			}
		}
		return null;
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

class PostCounter implements PostHandler {
	int	postCout	= 0;
	int	fileCount	= 0;

	public void reset() {
		postCout = 0;
	}

	public boolean matches() {
		if (postCout > 0)
			Logger.get().println("Recognized " + postCout + " posts.");

		return fileCount > 0;
	}

	@Override
	public void onAddPost(Post post) {
		postCout++;
		for (Image img : post.images) {
			try {
				String devzero = "/dev/null";

				// TODO: test on windows
				if (System.getProperty("os.name").toLowerCase().contains("windows"))
					devzero = "nul:nul";

				FileUtil.downloadFile(img.thumbnailUrl, devzero);
				fileCount++;
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void onPostsParsingDone() {
	}

}
