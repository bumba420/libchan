package de.benpicco.libchan;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedList;
import java.util.List;

public class main {
	public static void main(final String[] args) {
		String thread = "http://boards.4chan.org/soc/res/2977802";

		try {
			ThreadParser parser = new FourChanThreadParser();

			InputStream in = new FileInputStream("/tmp/thread");
			// InputStream in = new BufferedInputStream(new
			// URL(thread).openStream());
			parser.parseThread(in, new SimplePostReceiver(parser));

			// parser.getThreads(in, new SimplePostReceiver(parser));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class SimplePostReceiver implements PostReceiver {

	List<Post>		posts;
	ThreadParser	parser;

	public SimplePostReceiver(ThreadParser parser) {
		posts = new LinkedList<Post>();
		this.parser = parser;
	}

	@Override
	public void addPost(Post post) {
		posts.add(post);
		System.out.println(post);

		if (false) {

			String dir = "/tmp/4chan/" + post.user + "/";

			if (post.images.size() > 0)
				new File(dir).mkdir();

			for (Image img : post.images) {
				try {
					if ((new File(dir + img.filename)).exists()) {
						System.out.println(dir + img.filename + " already exists, skipping.");
						continue;
					}
					System.out.println("Saving " + img.url + " as " + dir + img.filename);
					URL image = new URL(img.url);
					ReadableByteChannel rbc = Channels.newChannel(new BufferedInputStream(image.openStream()));
					FileOutputStream fos = new FileOutputStream(dir + img.filename);
					fos.getChannel().transferFrom(rbc, 0, 1 << 24);
				} catch (Exception e) {
					System.out.println(post);
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void parsingDone() {
		System.out.println("Thread with " + posts.size() + " posts received.");
	}

	@Override
	public void addThread(Thread thread) {
		System.out.println("------------------");
		System.out.println("    NEW THREAD    ");
		System.out.println("------------------\n");

		System.out.println(thread.getUrl());

		// try {
		// parser.parseThread(new URL(thread.getUrl()).openStream(), this);
		// } catch (MalformedURLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}
}
