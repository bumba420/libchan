package de.benpicco.libchan.handler;

import java.io.File;

import de.benpicco.libchan.imageboards.Image;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.util.FileUtil;

public class DownloadImageHandler implements PostHandler {
	final String	targetDir;
	final boolean	threadFolder;

	String			tf	= "";

	public DownloadImageHandler(String target, boolean threadFolder) {
		this.threadFolder = threadFolder;
		targetDir = FileUtil.prepareDir(target);
	}

	@Override
	public void onAddPost(final Post post) {
		if (post.isFirstPost && threadFolder) {
			tf = post.id + File.separator;
			new File(targetDir + tf).mkdir();
		}

		final String dir = targetDir + tf + post.getDir() + File.separator;

		if (post.images.size() > 0)
			new File(dir).mkdir();

		(new java.lang.Thread(new Runnable() {

			@Override
			public void run() {
				for (Image img : post.images) {
					String filename = dir + img.filename.replace(File.separatorChar, ' ');
					FileUtil.downloadFile(img.url, filename, 5);
				}
			}
		})).start();
	}

	@Override
	public void onPostsParsingDone() {
	}
}
