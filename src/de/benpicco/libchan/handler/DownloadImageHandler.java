package de.benpicco.libchan.handler;

import java.io.File;

import de.benpicco.libchan.imageboards.Image;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.util.FileUtil;

public class DownloadImageHandler implements PostHandler {
	final String	targetDir;

	public DownloadImageHandler(String target) {
		targetDir = target.endsWith(File.separator) ? target : target + File.separator;
		new File(targetDir).mkdir();
	}

	@Override
	public void onAddPost(final Post post) {

		final String dir = targetDir + post.getDir() + File.separator;

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
