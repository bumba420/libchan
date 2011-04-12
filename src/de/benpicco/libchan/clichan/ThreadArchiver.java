package de.benpicco.libchan.clichan;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import de.benpicco.libchan.IPostReceiver;
import de.benpicco.libchan.imageboards.AsyncImageBoardParser;
import de.benpicco.libchan.imageboards.Image;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.util.FileUtil;

public class ThreadArchiver {
	final String	target;
	final String	chancfg;
	final int		interval;
	final String	oldThread;
	final String	followUpTag;
	final boolean	saveHtml;

	public ThreadArchiver(String thread, String target, String config, int interval, String followUpTag,
			boolean saveHtml) {
		this.followUpTag = followUpTag != null ? followUpTag.toUpperCase() : null;
		this.oldThread = thread;
		this.target = target;
		this.chancfg = config;
		this.interval = interval;
		this.saveHtml = saveHtml;
	}

	public void archiveThread() {
		archiveThread(-1);
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

		int				count		= 0;
		final String	targetDir;

		final String	thumbs		= ".thumbs" + File.separator;
		Writer			writer		= null;
		String			templateDir	= FileUtil.getJarLocation() + "template" + File.separator;
		HtmlConverter	converter	= null;

		public PostArchiver(String target) {
			targetDir = target.endsWith(File.separator) ? target : target + File.separator;

			new File(targetDir).mkdir();

			if (saveHtml) {
				new File(targetDir + thumbs).mkdir();
				converter = new HtmlConverter(templateDir);
				if (!converter.isInizialised())
					converter = null;
				try {
					FileUtil.copyFile(new File(templateDir + "style.css"), new File(targetDir + "style.css"));
				} catch (IOException e) {
					System.err.println("Unable to copy " + templateDir + "style.css");
				}
			}
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
						saveFile(img.url, dir + img.filename, 5);
						if (saveHtml)
							saveFile(img.thumbnailUrl,
									targetDir + thumbs + StringUtils.substringAfterLast(img.thumbnailUrl, "/"), 3);

					}
				}
			})).start();

			if (converter != null) {
				Post localPost = localisePost(post);
				if (writer == null)
					try {
						writer = converter.threadToHtml(localPost, targetDir);
					} catch (IOException e) {
						e.printStackTrace();
					}

				try {
					writer.write(converter.postToHtml(localPost));
					writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (followUpTag != null) {
				String newThread = StringUtils.substringBetween(post.message.toUpperCase(), followUpTag);
				if (newThread != null) {
					final String newThreadId = StringUtils.substringBetween(newThread, ">>", "\n");
					if (newThreadId != null && newThreadId.trim().length() > 0) {
						System.out.println("Detected follow-up thread: " + newThreadId);
						new Thread(new Runnable() {

							@Override
							public void run() {
								archiveThread(Integer.parseInt(newThreadId));
							}
						}).start();
					}
				}
			}
		}

		private Post localisePost(Post post) {
			if (post.images.size() == 0)
				return post;

			Post newPost = new Post();
			newPost.id = post.id;
			newPost.title = post.title;
			newPost.user = post.user;
			newPost.mail = post.mail;
			newPost.message = post.message;
			newPost.date = post.date;
			newPost.countryball = post.countryball;

			for (Image img : post.images) {
				Image newImg = new Image();
				newImg.filename = img.filename;
				newImg.url = post.user + File.separator + img.filename;
				newImg.thumbnailUrl = thumbs + StringUtils.substringAfterLast(img.thumbnailUrl, "/");
				newPost.addImage(newImg);
			}

			return newPost;
		}

		private void saveFile(String url, String filename, int tries) {
			File file = new File(filename);
			while (--tries > 0)
				try {
					if (file.exists()) {
						System.out.println(filename + " already exists, skipping.");
						break;
					}
					downloadFile(url, filename);
					System.out.println("Saved " + url + " as " + filename);

					break;
				} catch (Exception e) {
					file.delete();
					System.err.println("Failed to save " + url + " as " + filename + " (" + e + ")");
					if (tries > 0)
						System.out.println("retrying…");
					else
						System.err.println("…giving up");
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
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
}