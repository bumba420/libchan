package de.benpicco.libchan.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.PostProcessor;
import de.benpicco.libchan.util.FileUtil;
import de.benpicco.libchan.util.Logger;
import de.benpicco.libchan.util.Misc;

public class UserNotifyHandler implements PostProcessor {

	private final String[]	names;
	final List<String>		namesInThread;

	public UserNotifyHandler(List<String> names, boolean onJoinMsg) {
		if (names != null) {
			this.names = new String[names.size()];
			for (int i = 0; i < this.names.length; ++i)
				this.names[i] = names.get(i).toLowerCase();
		} else
			this.names = null;

		if (onJoinMsg)
			namesInThread = new ArrayList<String>();
		else
			namesInThread = null;
	}

	private boolean sendNotification(String title, String message, String filename) {
		String[] cmd = null;
		String[] cmd_linux = { "notify-send", "-i", filename, title, message };
		String[] cmd_osx = { "growlnotify", "--image", filename, "-t", title, "-m", message };
		String[] cmd_win = { "growlnotify", "/i:", filename, "/t:", title, message };

		String uname = System.getProperty("os.name").toLowerCase();
		if (uname.contains("windows"))
			cmd = cmd_win;
		else if (uname.contains("linux"))
			cmd = cmd_linux;
		else if (uname.contains("mac"))
			cmd = cmd_osx;
		else {
			Logger.get().println(uname + " notification not supported");
			return false;
		}

		try {
			Runtime.getRuntime().exec(cmd);
			return true;
		} catch (IOException e) {
			Logger.get().error("can't display notification");
			return false;
		}
	}

	@Override
	public void onAddPost(Post post) {
		String title = null;
		if (namesInThread != null && !namesInThread.contains(post.user)) {
			namesInThread.add(post.user);
			title = post.user + " entered the thread";
			Logger.get().println(title);
		} else if (names != null && Misc.containsAlike(names, post.user.toLowerCase()) >= 0) {
			Logger.get().println("New post from " + post.user);
		}

		if (title != null) {
			String tempfile = "";
			if (post.images.size() > 0) {
				String url = post.images.get(0).thumbnailUrl;
				tempfile = FileUtil.prepareDir(System.getProperty("java.io.tmpdir") + File.separator + "libChan")
						+ StringUtils.substringAfterLast(url, "/");
				FileUtil.downloadFile(url, tempfile, 1);
			}

			sendNotification(title, post.message, tempfile);
		}
	}

	@Override
	public void onPostsParsingDone() {
	}

	@Override
	public void onPostModified(Post oldPost, Post newPost) {
	}
}
