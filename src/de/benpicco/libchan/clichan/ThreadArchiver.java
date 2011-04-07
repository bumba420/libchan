package de.benpicco.libchan.clichan;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import de.benpicco.libchan.IPostReceiver;
import de.benpicco.libchan.imageboards.AsyncImageBoardParser;
import de.benpicco.libchan.imageboards.Image;
import de.benpicco.libchan.imageboards.Post;

public class ThreadArchiver {
	final String	target;
	final String	chancfg;
	final int		interval;
	final String	oldThread;
	final String	followUpTag;

	public ThreadArchiver(String thread, String target, String config, int interval, String followUpTag) {
		this.followUpTag = followUpTag.toUpperCase();
		this.oldThread = thread;
		this.target = target;
		this.chancfg = config;
		this.interval = interval;
	}

	void archiveThread(int id) {

		AsyncImageBoardParser parser = new ChanManager(chancfg).getParser(oldThread);
		String thread = oldThread;
		if (id > 0)
			thread = parser.composeUrl(oldThread, id);

		System.out.println("Saving pictures from " + thread + " to " + target);

		String t = target + File.separator + thread.substring(thread.lastIndexOf('/') + 1);

		if (parser == null)
			System.err.println("URL scheme not supported by any parser");
		else
			try {
				if (interval >= 0)
					(new ThreadWatcher(thread, interval, new PostArchiver(t), parser)).run();
				else {
					parser.getPosts(thread, new PostArchiver(t));
					parser.dispose();
				}
			} catch (IOException e) {
				System.out.println("Unable to parse " + thread + ", " + e);
			}
	}

	class PostArchiver implements IPostReceiver {

		int				count				= 0;
		final String	targetDir;
		boolean			hasFollowUpThread	= false;

		public PostArchiver(String targetDir) {
			this.targetDir = targetDir.endsWith(File.separator) ? targetDir : targetDir + File.separator;

			new File(targetDir).mkdir();
		}

		@Override
		public void onAddPost(final Post post) {
			if (++count > 500)
				System.out.println("Thread has " + count + " replies.");

			final String dir = targetDir + post.user + File.separator;

			if (post.images.size() > 0)
				new File(dir).mkdir();

			(new java.lang.Thread(new Runnable() {

				@Override
				public void run() {
					for (Image img : post.images) {
						img.filename = img.filename.replace(File.separatorChar, ' ');
						File file = new File(dir + img.filename);
						int tries = 5;
						while (--tries > 0)
							try {
								if (file.exists()) {
									System.out.println(dir + img.filename + " already exists, skipping.");
									break;
								}
								downloadFile(img.url, dir + img.filename);
								System.out.println("Saved " + img.url + " as " + dir + img.filename);

								break;
							} catch (Exception e) {
								file.delete();
								System.err.println("Failed to save " + img.filename + " as " + dir + img.filename
										+ " (" + e + ")");
								// System.out.println("------------------\n" +
								// post
								// + "\n-------------------");
								if (tries > 0)
									System.out.println("retrying…");
								else
									System.err.println("…giving up");
							}
					}
				}
			})).start();

			if (!hasFollowUpThread) {
				String newThread = StringUtils.substringBetween(post.message.toUpperCase(), followUpTag);
				if (newThread != null) {
					String newThreadId = StringUtils.substringBetween(newThread, ">>", "\n");
					if (newThreadId != null && newThreadId.trim().length() > 0) {
						System.out.println("Detected follow-up thread: " + newThreadId);
						archiveThread(Integer.parseInt(newThreadId));
						hasFollowUpThread = true;
					}
				}
			}
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
}