package de.benpicco.libchan.clichan;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

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
			thread = "http://boards.4chan.org/soc/res/3143700"; // XXX
			break;
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
					File file = new File(dir + img.filename);
					int tries = 3;
					while (--tries > 0)
						try {
							if (file.exists()) {
								System.out.println(dir + img.filename + " already exists, skipping.");
								break;
							}
							System.out.println("Saving " + img.url + " as " + dir + img.filename);
							downloadFile(img.url, dir + img.filename);
							System.out.println(dir + img.filename + " saved.");

							break;
						} catch (Exception e) {
							file.delete();
							System.err.println("Failed to save " + img.filename + " as " + dir + img.filename + " ("
									+ e + ")");
							// System.out.println("------------------\n" + post
							// + "\n-------------------");
							if (tries > 0)
								System.out.println("retrying…");
							else
								System.err.println("…giving up");
						}
				}
			}
		})).start();
	}

	public void downloadFile(String url, String target) throws MalformedURLException, IOException {
		byte[] buffer = new byte[2048];
		BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
		OutputStream out = new FileOutputStream(target);
		int count = 0;
		do {
			count = in.read(buffer);
			if (count > 0)
				out.write(buffer, 0, count);
		} while (count > 0);
		out.close();
		in.close();
	}

	@Override
	public void onPostsParsingDone() {
		System.out.println("Thread with " + count + " posts received.");
	}
}