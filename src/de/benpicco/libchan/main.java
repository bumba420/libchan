package de.benpicco.libchan;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import de.benpicco.libchan.imageboards.KrautchanParser;

public class main {
	public static void main(final String[] args) {
		try {
			ImageBoardParser parser = new KrautchanParser();
			InputStream in = new BufferedInputStream(new URL("http://krautchan.net/b/thread-2828069.html").openStream());
			parser.parseThread(in, new SimplePostReceiver(parser));

			// InputStream in = new BufferedInputStream(new
			// URL("http://boards.4chan.org/soc/0").openStream());
			// parser.getThreads(in, new SimplePostReceiver(parser));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class SimplePostReceiver implements PostReceiver {

	List<Post>			posts;
	ImageBoardParser	parser;

	public SimplePostReceiver(ImageBoardParser parser) {
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
