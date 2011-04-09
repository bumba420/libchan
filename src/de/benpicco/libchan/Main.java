package de.benpicco.libchan;

import java.io.DataOutputStream;
import java.io.IOException;

import de.benpicco.libchan.clichan.ChanManager;
import de.benpicco.libchan.clichan.HtmlConverter;
import de.benpicco.libchan.imageboards.Post;

public class Main {
	public static void main(final String[] args) {

		String url = "http://boards.4chan.org/soc/res/3239694";

		// new ThreadWatcher(url, 5, new SimplePostReceiver()).run();

		IImageBoardParser parser = new ChanManager("chans/").getParser(url);

		if (parser == null) {
			System.err.println("No parser found");
			return;
		}

		try {
			parser.getPosts(url, new SimplePostReceiver());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}

class SimplePostReceiver implements IPostReceiver, IThreadReceiver {

	DataOutputStream	out	= null;
	final HtmlConverter	converter;

	public SimplePostReceiver() {
		converter = new HtmlConverter("template/");
	}

	@Override
	public void onAddPost(final Post post) {
		System.out.println(post);
		if (out == null)
			try {
				out = converter.threadToHtml(post, "");
			} catch (IOException e) {
				e.printStackTrace();
			}

		try {
			out.writeUTF(converter.postToHtml(post));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPostsParsingDone() {
		System.out.println("Thread parsing done.");
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onAddThread(Thread thread) {
		System.out.println(thread.getUrl());

	}

	@Override
	public void onThreadsParsingDone() {
	}
}
