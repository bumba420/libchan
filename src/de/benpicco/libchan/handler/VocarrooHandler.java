package de.benpicco.libchan.handler;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.PostProcessor;
import de.benpicco.libchan.util.FileUtil;
import de.benpicco.libchan.util.Logger;
import de.benpicco.libchan.util.Misc;
import de.benpicco.libchan.util.ThreadPool;

public class VocarrooHandler implements PostProcessor {

	final String				targetDir;
	final boolean				threadFolder;
	private final String[]		names;
	private final static String	voccUrl	= "http://vocaroo.com/download.php?server=0&media=";
	private final Pattern		voccReg	= Pattern.compile("http\\:\\/\\/vocaroo.com\\/\\?media=([a-zA-Z0-9_]+)");

	String						tf		= "";

	public VocarrooHandler(String target, boolean threadFolder, final String[] names) {
		String nameslist = names.length > 0 ? " from " + Misc.printNames(names) : "";
		Logger.get().println("Will save all vocaroos" + nameslist + ".");
		this.threadFolder = threadFolder;
		targetDir = FileUtil.prepareDir(target);
		this.names = names;
	}

	private String folder(Post post) {
		return FileUtil.prepareDir(targetDir + tf + post.getDir());
	}

	@Override
	public void onAddPost(final Post post) {
		if (post.isFirstPost() && threadFolder) {
			tf = post.id + File.separator;
			new File(targetDir + tf).mkdir();
		}

		if (names.length > 0 && !contains(names, post.user))
			return;

		final String dir = folder(post);
		final Matcher match = voccReg.matcher(post.message);
		while (match.find())
			ThreadPool.addDownload(voccUrl + match.group(1), dir + post.id + "_" + match.group(1) + ".wav");
	}

	@Override
	public void onPostsParsingDone() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPostModified(Post oldPost, Post newPost) {
		// TODO Auto-generated method stub

	}

	private static boolean contains(String[] array, String item) {
		for (String s : array)
			if (s.toLowerCase().equals(item.toLowerCase()))
				return true;
		return false;
	}
}
