package de.benpicco.libchan.clichan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.ImageBoardParser;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.util.FileUtil;
import de.benpicco.libchan.util.Logger;

// nasty code duplication ahead :/
// quick & dirty version, this should be merged with ThreadArchiver
public class BoardArchiver implements Runnable, PostHandler {

	private final ThreadArchiver				archiver;
	private final ArrayList<ImageBoardParser>	boards;
	private final ArchiveOptions				options;
	private ChanManager							manager;

	public BoardArchiver(ArchiveOptions options) {
		this.options = options;
		archiver = new ThreadArchiver(options);
		boards = new ArrayList<ImageBoardParser>();
		manager = new ChanManager(options.chanConfig, options.htmlTemplate);
	}

	// Assumption: addBoard() is not called while running, if this is ever
	// required, see ThreadArchiver for how to handle that
	public void addBoard(String url) {
		ImageBoardParser p = manager.getParser(url);
		if (p == null)
			p = manager.guessParser(url);
		if (p == null)
			Logger.get().error("No parser found for " + url);

		boards.add(p);
	}

	@Override
	public void run() {
		do {
			for (ImageBoardParser parser : boards) {
				Logger.get().println("Refreshing " + parser.getUrl());
				try {
					parser.getPosts();
				} catch (IOException e) {
					Logger.get().error("Can't refresh " + parser.getUrl());
				}
			}

			if (options.boardInterval > 0)
				try {
					Thread.sleep(options.boardInterval);
				} catch (InterruptedException e) {
				}
			else
				return;
		} while (boards.size() > 0);
	}

	@Override
	public void onAddPost(Post post) {
		if (!post.isFirstPost())
			return;

		if (!options.noBoardFolders)
			for (ImageBoardParser parser : boards)
				if (post.threadUrl.startsWith(parser.getUrl())) {
					String board = parser.getUrl();
					board = board.endsWith("/") ? board.substring(0, board.length() - 1) : board;
					board = File.separator + StringUtils.substringAfterLast(board, "/");
					archiver.addThread(post.threadUrl, FileUtil.prepareDir(options.target + board));
					return;
				}
		archiver.addThread(post.threadUrl);
	}

	@Override
	public void onPostsParsingDone() {
		if (archiver.threads() > 0 && !archiver.isRunnig())
			new Thread(archiver).start();
	}
}
