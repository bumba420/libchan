package de.benpicco.libchan.handler;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.PostProcessor;
import de.benpicco.libchan.util.FileUtil;
import de.benpicco.libchan.util.Logger;

public class UserNotifyHandler implements PostProcessor {

	private final String[]	names;

	public UserNotifyHandler(List<String> names) {
		this.names = new String[names.size()];
		for (int i = 0; i < this.names.length; ++i)
			this.names[i] = names.get(i).toLowerCase();
	}

	@Override
	public void onAddPost(Post post) {
		for (int i = 0; i < names.length; ++i)
			if (post.user.toLowerCase().contains(names[i])) {
				Logger.get().println("New post from " + post.user);

				String tempfile = "";
				if (post.images.size() > 0) {
					String url = post.images.get(0).thumbnailUrl;
					try {
						tempfile = FileUtil.prepareDir(System.getProperty("java.io.tmpdir") + File.separator
								+ "libChan")
								+ StringUtils.substringAfterLast(url, "/");
						FileUtil.downloadFile(url, tempfile);
					} catch (IOException e) {
						Logger.get().error("Failed fetching thumbnail: " + e);
					}
				}

				String[] cmd = null;
				String[] cmd_linux = { "notify-send", "-i", tempfile, post.user, post.message };
				String[] cmd_osx = { "growlnotify", "--image", tempfile, "-t", post.user, "-m", post.message };
				String[] cmd_win = { "growlnotify", "/i:", tempfile, "/t:", post.user, post.message };

				String uname = System.getProperty("os.name").toLowerCase();
				if (uname.contains("windows"))
					cmd = cmd_win;
				else if (uname.contains("linux"))
					cmd = cmd_linux;
				else if (uname.contains("mac"))
					cmd = cmd_osx;
				else {
					Logger.get().println(uname + " notification not supported");
					return;
				}

				try {
					Runtime.getRuntime().exec(cmd);
				} catch (IOException e) {
					Logger.get().error("can't display notification");
				}

				break;
			}
	}

	@Override
	public void onPostsParsingDone() {
	}

	@Override
	public void onPostModified(Post oldPost, Post newPost) {
	}
}
