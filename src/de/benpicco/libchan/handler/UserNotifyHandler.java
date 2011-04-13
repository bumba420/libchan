package de.benpicco.libchan.handler;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.benpicco.libchan.PostHandler;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.util.FileUtil;

public class UserNotifyHandler implements PostHandler {

	private final List<String>	names;

	public UserNotifyHandler(List<String> names) {
		this.names = names;
	}

	@Override
	public void onAddPost(Post post) {
		if (names.contains(post.user.toLowerCase())) {
			System.out.println("New post from " + post.user);

			String tempfile = "";
			if (post.images.size() > 0) {
				String url = post.images.get(0).thumbnailUrl;
				try {
					tempfile = System.getProperty("java.io.tmpdir") + File.separator
							+ StringUtils.substringAfterLast(url, "/");
					FileUtil.downloadFile(url, tempfile);
				} catch (IOException e1) {
					System.err.println("Failed fetching thumbnail: " + e1);
				}
			}

			String[] cmd = { "notify-send", "-i", tempfile, post.user, post.message };

			try {
				Runtime.getRuntime().exec(cmd);
			} catch (IOException e) {
				System.err.println("can't display notification");
			}
		}
	}

	@Override
	public void onPostsParsingDone() {
	}

}
