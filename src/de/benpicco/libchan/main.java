package de.benpicco.libchan;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import de.benpicco.libchan.imageboards.FourChanParser;

public class main {
	public static void main(final String[] args) {
		try {
			IImageBoardParser parser = new FourChanParser();

			InputStream in = new FileInputStream("/tmp/3035546");
			// InputStream in = new BufferedInputStream(new
			// URL("http://boards.4chan.org/soc/res/3035546").openStream());
			parser.parseThread(in, new SimplePostReceiver(parser));

			// InputStream in = new BufferedInputStream(new
			// URL("http://boards.4chan.org/soc/0").openStream());
			// parser.getThreads(in, new SimplePostReceiver(parser));
		} catch (IOException e) {
			e.printStackTrace();
		}
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
