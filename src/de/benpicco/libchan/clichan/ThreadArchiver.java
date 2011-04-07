package de.benpicco.libchan.clichan;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

import de.benpicco.libchan.IPostReceiver;
import de.benpicco.libchan.imageboards.AsyncImageBoardParser;
import de.benpicco.libchan.imageboards.Image;
import de.benpicco.libchan.imageboards.Post;

public class ThreadArchiver {
	String	target		= ".";
	String	chancfg		= "chans/";
	int		interval	= -1;
	String	oldThread	= null;

	public ThreadArchiver() {

	}

	public static void main(String[] args) {
		ThreadArchiver archiver = new ThreadArchiver();

		String thread = "http://boards.4chan.org/soc/res/3186014";

		final Options cliOptions = new Options();
		cliOptions.addOption("t", "thread", true, "Thread to process");
		cliOptions.addOption("o", "output", true, "target directory");
		cliOptions.addOption("i", "interval", true, "thread refresh interval");
		cliOptions.addOption("c", "config", true, "chan configuration directory");

		final CommandLineParser cliParser = new GnuParser();
		try {
			CommandLine commandLine = cliParser.parse(cliOptions, args);
			if (commandLine.hasOption('t'))
				thread = commandLine.getOptionValue('t');
			if (commandLine.hasOption('o'))
				archiver.target = commandLine.getOptionValue('o');
			if (commandLine.hasOption('i'))
				archiver.interval = Integer.parseInt(commandLine.getOptionValue('i'));
			if (commandLine.hasOption('c'))
				archiver.chancfg = commandLine.getOptionValue('c');
		} catch (ParseException e1) {
			e1.printStackTrace();
		}

		if (thread == null) {
			new HelpFormatter().printHelp("No Thread specified", cliOptions);
			return;
		}

		archiver.oldThread = thread;

		archiver.archiveThread(0);
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
						int tries = 5;
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
				String newThread = StringUtils.substringBetween(post.message, "NEW THREAD");
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