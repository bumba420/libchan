package de.benpicco.libchan.clichan;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import de.benpicco.libchan.IImageBoardParser;
import de.benpicco.libchan.IPostReceiver;
import de.benpicco.libchan.imageboards.Image;
import de.benpicco.libchan.imageboards.Post;

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

		target = target + File.separator + thread.substring(thread.lastIndexOf('/') + 1);

		System.out.println("Saving pictures from " + thread + " to " + target);

		IImageBoardParser parser = new ChanManager("chans/").getParser(thread);
		if (parser == null)
			System.err.println("URL scheme not supported by any parser");
		else
			try {
				parser.getPosts(thread, new PostArchiver(target));
			} catch (IOException e) {
				System.out.println("Unable to parse " + thread + ", " + e);
			}
	}
}

class PostArchiver implements IPostReceiver {

	int				count	= 0;
	final String	targetDir;

	public PostArchiver(String targetDir) {
		this.targetDir = targetDir.endsWith(File.separator) ? targetDir : targetDir + File.separator;

		new File(targetDir).mkdir();
	}

	@Override
	public void onAddPost(final Post post) {
		++count;

		final String dir = targetDir + post.user + File.separator;

		if (post.images.size() > 0)
			new File(dir).mkdir();

		(new java.lang.Thread(new Runnable() {

			@Override
			public void run() {
				for (Image img : post.images) {
					img.filename = img.filename.replace(File.separatorChar, ' ');
					int tries = 5;
					while (tries-- > 0)
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
							break;
						} catch (Exception e) {
							System.err.println("Failed to save " + img.filename + " as " + dir + img.filename);
							System.out.println("------------------\n" + post + "\n-------------------");
							if (tries > 0)
								System.out.println("retrying…");
							else
								System.out.println("…giving up");
						}
				}
			}
		})).start();

	}

	@Override
	public void onPostsParsingDone() {
		System.out.println("Thread with " + count + " posts received.");
	}
}