package de.benpicco.libchan.handler;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.util.FileUtil;
import de.benpicco.libchan.util.Logger;

public class UserNotifyHandler implements PostHandler {

	private final List<String>	names;

	public UserNotifyHandler(List<String> names) {
		this.names = names;
	}

	@Override
	public void onAddPost(Post post) {
		if (names.contains(post.user.toLowerCase())) {
			Logger.get().println("New post from " + post.user);

			String tempfile = "";
			if (post.images.size() > 0) {
				String url = post.images.get(0).thumbnailUrl;
				try {
					tempfile = FileUtil.prepareDir(System.getProperty("java.io.tmpdir") + File.separator + "libChan")
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
		}
	}

	@Override
	public void onPostsParsingDone() {
	}

}
