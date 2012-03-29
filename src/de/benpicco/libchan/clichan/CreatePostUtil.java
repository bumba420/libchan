package de.benpicco.libchan.clichan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.benpicco.libchan.imageboards.GenericImageBoardParser;
import de.benpicco.libchan.imageboards.Image;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.util.Logger;
import de.benpicco.libchan.util.NotImplementedException;

public class CreatePostUtil {
	private final GenericImageBoardParser	parser;
	private final ArchiveOptions			o;

	private void addFile(Post post, String file) {
		Image image = new Image();
		image.filename = file;
		post.addImage(image);
	}

	private Post getPost(String user, String title, String mail, String message, List<String> files) {
		Post post = new Post();
		post.user = user;
		post.mail = mail;
		post.message = message;
		post.title = title;

		if (files != null)
			for (String file : files)
				addFile(post, file);

		return post;
	}

	public void uploadFiles(String user, String title, String mail, String password, String message, int delay,
			List<File> files) throws IOException, NotImplementedException, InterruptedException {
		if (parser == null) {
			Logger.get().error("no suitable parser found.");
			return;
		}

		ArrayList<String> chunk = new ArrayList<String>(parser.getMaxFiles());
		Logger.get().println("Will upload " + files.size() + " files, " + parser.getMaxFiles() + " at a time.\n");

		if (files != null)
			for (File file : files) {
				if (chunk.size() == parser.getMaxFiles()) {
					Logger.get().println("Uploading " + chunk.size() + " files…");
					parser.createPost(getPost(user, title, mail, message, chunk), password);
					chunk.clear();
					Logger.get().println("Waiting for " + delay + "s…");
					java.lang.Thread.sleep(delay * 1000);
				}
				Logger.get().println("Adding " + file);
				chunk.add(file.toString());
			}
		Logger.get().println("Uploading " + chunk.size() + " files…");
		parser.createPost(getPost(user, title, mail, message, chunk), password);
	}

	public CreatePostUtil(String url, ArchiveOptions options) {
		o = options;
		parser = new ChanManager(o.chanConfig, o.htmlTemplate).getParser(url);
	}
}
