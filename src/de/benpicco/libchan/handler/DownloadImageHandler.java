package de.benpicco.libchan.handler;

import java.io.File;

import de.benpicco.libchan.imageboards.Image;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.PostProcessor;
import de.benpicco.libchan.util.FileUtil;
import de.benpicco.libchan.util.Logger;

public class DownloadImageHandler implements PostProcessor {
	final String	targetDir;
	final boolean	threadFolder;

	String			tf	= "";

	public DownloadImageHandler(String target, boolean threadFolder) {
		this.threadFolder = threadFolder;
		targetDir = FileUtil.prepareDir(target);
	}

	private String folder(Post post) {
		return targetDir + tf + post.getDir() + File.separator;
	}

	@Override
	public void onAddPost(final Post post) {
		if (post.isFirstPost() && threadFolder) {
			tf = post.id + File.separator;
			new File(targetDir + tf).mkdir();
		}

		final String dir = folder(post);

		if (post.images.size() > 0)
			new File(dir).mkdir();

		(new java.lang.Thread(new Runnable() {

			@Override
			public void run() {
				for (Image img : post.images)
					FileUtil.downloadFile(img.url, dir + img.filename, 5);
			}
		})).start();
	}

	@Override
	public void onPostsParsingDone() {
	}

	@Override
	public void onPostModified(Post oldPost, Post newPost) {
		// we move deleted files to a separate directory
		final String dir = folder(oldPost);
		for (Image img : oldPost.images) {
			if (newPost == null || newPost.images == null || !newPost.images.contains(img)) {
				Logger.get().println("File " + img.filename + " deleted");
				String delDir = FileUtil.prepareDir(dir + "deleted");
				new File(dir + img.filename).renameTo(new File(delDir + img.filename));
			}

		}
	}
}
