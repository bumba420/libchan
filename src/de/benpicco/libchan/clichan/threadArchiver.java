package de.benpicco.libchan.clichan;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedList;
import java.util.List;

import de.benpicco.libchan.Image;
import de.benpicco.libchan.IImageBoardParser;
import de.benpicco.libchan.Post;
import de.benpicco.libchan.IPostReceiver;
import de.benpicco.libchan.Thread;
import de.benpicco.libchan.imageboards.FourChanParser;

public class threadArchiver {
	public static void main(String[] args) {
		String thread = "";
		String target = ".";

		switch (args.length) {
		case 0:
			System.out.println("no thread specified");
			return;
		case 2:
			target = args[1];
		default:
			thread = args[0];
		}

		InputStream in = null;
		try {
			in = new BufferedInputStream(new URL(thread).openStream());
		} catch (IOException e) {
			System.err.println("Invalid URL " + thread);
			return;
		}

		target = target + File.separator + thread.substring(thread.lastIndexOf('/') + 1);

		System.out.println("Saving pictures from " + thread + " to " + target);

		IImageBoardParser parser = new FourChanParser();
		parser.parseThread(in, new PostArchiver(target));
	}
}

class PostArchiver implements IPostReceiver {

	List<Post>		posts;
	final String	targetDir;

	public PostArchiver(String targetDir) {
		posts = new LinkedList<Post>();
		this.targetDir = targetDir.endsWith(File.separator) ? targetDir : targetDir + File.separator;

		new File(targetDir).mkdir();
	}

	@Override
	public void addPost(final Post post) {
		posts.add(post);

		final String dir = targetDir + post.user + File.separator;

		if (post.images.size() > 0)
			new File(dir).mkdir();

		(new java.lang.Thread(new Runnable() {

			@Override
			public void run() {
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
						System.out.println(dir + img.filename + " saved.");
					} catch (Exception e) {
						System.out.println(post);
						e.printStackTrace();
					}
				}
			}
		})).start();

	}

	@Override
	public void parsingDone() {
		System.out.println("Thread with " + posts.size() + " posts received.");
	}

	@Override
	public void addThread(Thread thread) {
		// TODO: implement
	}
}