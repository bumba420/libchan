package de.benpicco.libchan;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.benpicco.libchan.clichan.ChanManager;
import de.benpicco.libchan.imageboards.Post;

public class main {
	public static void main(final String[] args) {
		// List<String> names = new ArrayList<String>();
		// names.add("Pokechu");
		// ChanCrawler.lookFor(names, "http://boards.4chan.org/soc/");

		String url = "http://boards.4chan.org/soc/res/3167929";

		// new ThreadWatcher(url, 5, new SimplePostReceiver()).run();

		IImageBoardParser parser = new ChanManager("chans/").getParser(url);

		try {
			parser.getPosts(url, new SimplePostReceiver());
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

class SimplePostReceiver implements IPostReceiver, IThreadReceiver {

	List<Post>	posts;

	public SimplePostReceiver() {
		posts = new LinkedList<Post>();
	}

	@Override
	public void onAddPost(final Post post) {
		posts.add(post);
		System.out.println(post);
	}

	@Override
	public void onPostsParsingDone() {
		System.out.println("Thread with " + posts.size() + " posts received.");
	}

	@Override
	public void onAddThread(Thread thread) {
		System.out.println(thread.getUrl());

	}

	@Override
	public void onThreadsParsingDone() {
	}
}
