package de.benpicco.libchan;

import java.io.IOException;
import java.net.MalformedURLException;

import de.benpicco.clichan.StdLogger;
import de.benpicco.libchan.clichan.ArchiveOptions;
import de.benpicco.libchan.clichan.ChanManager;
import de.benpicco.libchan.clichan.ThreadArchiver;
import de.benpicco.libchan.imageboards.Board;
import de.benpicco.libchan.imageboards.ChanSpecification;
import de.benpicco.libchan.imageboards.GenericImageBoardParser;
import de.benpicco.libchan.imageboards.Image;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.imageboards.Thread;
import de.benpicco.libchan.interfaces.BoardHandler;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.interfaces.ThreadHandler;
import de.benpicco.libchan.util.Logger;

public class DebugMain {

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

	private static void addFile(Post post, String file) {
		Image image = new Image();
		image.filename = file;
		post.addImage(image);
	}

	private static Post getTestPost() {
		Post post = new Post();
		post.user = "TestBernd";
		post.mail = "sage";
		post.message = "Die Manule dienen nur zu Testzwecken, eigentlich foll fieß wie ich hier Tierversuche durchführe!";
		post.op = 3757648;

		addFile(post, "/tmp/kc/manul_2_serious.jpg");
		addFile(post, "/tmp/kc/manul_2_kitten_running.jpg");

		return post;
	}

	public static void main(final String[] args) throws MalformedURLException, IOException, InterruptedException {
		Logger.add(new StdLogger());

		// String url = "http://desuchan.net/a/";
		String url = "http://krautchan.net/b/thread-3757648.html";
		// String url = "http://boards.4chan.org/soc/res/8434479";
		// String url = "http://2ch.so/p/";
		// String url = "http://operatorchan.org/k/";

		// archiveThread(url);
		ChanSpecification spec = new ChanSpecification("template/", true);

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
		parser.setThreadHandler(rec);
		parser.setBoardHandler(rec);

		parser.createPost(getTestPost());
		// parser.getPosts();
		// parser.getThreads();
		// parser.getBoards();

		// while (true) {
		// parser.getPosts();
		// java.lang.Thread.sleep(5000);
		// }
	}
}

class SimplePostReceiver implements PostHandler, ThreadHandler, BoardHandler {
	int	postCount	= 0;
	int	threadCount	= 0;
	int	boardCount	= 0;

	@Override
	public void onAddPost(final Post post) {
		postCount++;
		System.out.println(post);
	}

	@Override
	public void onPostsParsingDone() {
		System.out.println(postCount + " posts received.");
	}

	@Override
	public void onAddThread(Thread thread) {
		threadCount++;
		System.out.println(thread.getUrl());
	}

	@Override
	public void onThreadsParsingDone() {
		System.out.println(threadCount + " threads received.");
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
