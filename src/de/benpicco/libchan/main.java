package de.benpicco.libchan;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import de.benpicco.libchan.imageboards.ChanSpecification;
import de.benpicco.libchan.imageboards.Post;

public class main {
	public static void main(final String[] args) {
		// List<String> names = new ArrayList<String>();
		// names.add("Pokechu");
		// ChanCrawler.lookFor(names, "http://boards.4chan.org/soc/");

		IImageBoardParser parser = (new ChanSpecification("chans/krautchan.chan")).getImageBoardParser();

		try {
			InputStream in = new BufferedInputStream(
					new URL("http://krautchan.net/int/thread-4209173.html").openStream());
			parser.getMessages(in, new SimplePostReceiver(parser));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// try {
		// IImageBoardParser parser = new KrautchanParser();
		//
		// // InputStream in = new FileInputStream("/tmp/3035546");
		// InputStream in = new BufferedInputStream(new
		// URL("http://krautchan.net/b/0.html").openStream());
		// parser.parseThread(in, new SimplePostReceiver(parser));
		//
		// // InputStream in = new BufferedInputStream(new
		// // URL("http://boards.4chan.org/soc/0").openStream());
		// // parser.getThreads(in, new SimplePostReceiver(parser));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}
}

class SimplePostReceiver implements IPostReceiver {

	List<Post>			posts;
	IImageBoardParser	parser;

	public SimplePostReceiver(IImageBoardParser parser) {
		posts = new LinkedList<Post>();
		this.parser = parser;
	}

	@Override
	public void addPost(final Post post) {
		posts.add(post);
		System.out.println(post);
	}

	@Override
	public void parsingDone() {
		System.out.println("Thread with " + posts.size() + " posts received.");
	}

	@Override
	public void addThread(Thread thread) {
		System.out.println(thread.getUrl());
	}
}
