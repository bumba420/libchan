package de.benpicco.libchan;

import java.io.IOException;
import java.net.MalformedURLException;

import de.benpicco.clichan.StdLogger;
import de.benpicco.libchan.clichan.ArchiveOptions;
import de.benpicco.libchan.clichan.ChanManager;
import de.benpicco.libchan.clichan.ThreadArchiver;
import de.benpicco.libchan.imageboards.Board;
import de.benpicco.libchan.imageboards.GenericImageBoardParser;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.BoardHandler;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.util.Logger;
import de.benpicco.libchan.util.NotImplementedException;

/*
 * This is just a sandbox for testing stuff, please ignore
 */
public class DebugMain {

	@SuppressWarnings("unused")
	private static void archiveThread(String url) {
		ArchiveOptions options = new ArchiveOptions();
		options.chanConfig = "chans/";
		options.htmlTemplate = "template/";
		options.target = "/tmp/libChan/";
		// options.saveHtml = true;
		// options.saveImages = true;
		options.threadFolders = true;
		// options.interval = 30000;
		options.delete = true;
		options.followUpTag = "new thread";
		options.vocaroo = new String[0];

		ThreadArchiver archiver = new ThreadArchiver(options);
		archiver.addThread(url);
		archiver.run();
	}

	public static void main(final String[] args) throws MalformedURLException, IOException, InterruptedException,
			NotImplementedException {
		Logger.add(new StdLogger());

		String url = "http://www.4chan.org";

		// archiveThread(url);
		// ChanSpecification spec = new ChanSpecification("template/", true);

		ChanManager mngr = new ChanManager("chans/", "template/");
		GenericImageBoardParser parser = mngr.getParser(url);
		if (parser == null)
			parser = mngr.guessParser(url);

		if (parser == null) {
			System.err.println("No parser found");
			System.exit(-1);
		}

		SimplePostReceiver rec = new SimplePostReceiver();
		parser.setPostHandler(rec);
		parser.setBoardHandler(rec);

		// System.out.println("MaxFiles: " + parser.getMaxFiles());

		// try {
		// // uploadDir("/tmp/kc", parser);
		// parser.deletePost(961, "debugpasswd");
		// } catch (NotImplementedException e) {
		// Logger.get().error(e.getMessage());
		// }
		// parser.getPosts();
		// parser.getThreads();
		parser.getBoards();

		// while (true) {
		// parser.getPosts();
		// java.lang.Thread.sleep(5000);
		// }
	}
}

class SimplePostReceiver implements PostHandler, BoardHandler {
	int	postCount	= 0;
	int	threadCount	= 0;
	int	boardCount	= 0;

	@Override
	public void onAddPost(final Post post) {
		postCount++;
		if (post.isFirstPost()) {
			threadCount++;
			Logger.get().println("[" + post.id + "]");
		}
		// System.out.println(post);
	}

	@Override
	public void onPostsParsingDone() {
		System.out.println(postCount + " posts received.");
	}

	@Override
	public void onAddBoard(Board board) {
		boardCount++;
		System.out.println(board);
	}

	@Override
	public void onBoardParsingDone() {
		System.out.println(boardCount + " boards received.");
	}
}
