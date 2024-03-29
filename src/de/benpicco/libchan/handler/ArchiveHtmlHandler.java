package de.benpicco.libchan.handler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang3.StringUtils;

import de.benpicco.libchan.clichan.HtmlConverter;
import de.benpicco.libchan.imageboards.Image;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.PostProcessor;
import de.benpicco.libchan.util.FileUtil;
import de.benpicco.libchan.util.Logger;
import de.benpicco.libchan.util.ThreadPool;

public class ArchiveHtmlHandler implements PostProcessor {

	private final String	baseDir;
	private final boolean	threadFolder;

	private final String	thumbs		= ".thumbs" + File.separator;

	private String			targetDir;
	private Writer			writer		= null;
	private final String	templateDir;
	private HtmlConverter	converter	= null;
	private int				threadId	= 0;

	private void initialise() {
		if (threadFolder)
			targetDir = FileUtil.prepareDir(baseDir + threadId);
		else
			targetDir = baseDir;

		new File(targetDir + thumbs).mkdir();
		converter = new HtmlConverter(templateDir);
		if (!converter.isInizialised())
			converter = null;
		try {
			FileUtil.copyDirectory(new File(templateDir + "static"), new File(targetDir));
		} catch (IOException e) {
			Logger.get().error("Error copying html template: " + e.getMessage());
		}
	}

	public ArchiveHtmlHandler(String target, String templateDir, boolean threadFolder) {
		baseDir = FileUtil.prepareDir(target);
		this.threadFolder = threadFolder;
		this.templateDir = templateDir;
	}

	@Override
	public void onAddPost(Post post) {
		if (post.isFirstPost()) {
			threadId = post.id;
			initialise();
		}
		Post localPost = Post.localisePost(post, thumbs);

		if (writer == null)
			try {
				writer = new FileWriter(targetDir + threadId + ".html", !post.isFirstPost());
			} catch (IOException e) {
				e.printStackTrace();
			}

		try {
			if (post.isFirstPost())
				writer.write(converter.getHeader(localPost));
			writer.append(converter.postToHtml(localPost));
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (Image img : post.images)
			ThreadPool.addDownload(img.thumbnailUrl,
					targetDir + thumbs + StringUtils.substringAfterLast(img.thumbnailUrl, "/"));
	}

	@Override
	public void onPostsParsingDone() {
		if (writer != null)
			try {
				writer.close();
				writer = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public void onPostModified(Post oldPost, Post newPost) {
		// TODO Auto-generated method stub

	}
}
