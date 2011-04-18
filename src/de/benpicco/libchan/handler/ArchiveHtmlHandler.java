package de.benpicco.libchan.handler;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang3.StringUtils;

import de.benpicco.libchan.clichan.HtmlConverter;
import de.benpicco.libchan.imageboards.Image;
import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.util.FileUtil;

public class ArchiveHtmlHandler implements PostHandler {

	private final String	targetDir;
	final String			thumbs		= ".thumbs" + File.separator;
	Writer					writer		= null;
	String					templateDir	= FileUtil.getJarLocation() + "template" + File.separator;
	HtmlConverter			converter	= null;

	public ArchiveHtmlHandler(String target) {
		targetDir = target.endsWith(File.separator) ? target : target + File.separator;
		new File(targetDir).mkdir();

		new File(targetDir + thumbs).mkdir();
		converter = new HtmlConverter(templateDir);
		if (!converter.isInizialised())
			converter = null;
		try {
			FileUtil.copyFile(new File(templateDir + "style.css"), new File(targetDir + "style.css"));
		} catch (IOException e) {
			System.err.println("Unable to copy " + templateDir + "style.css");
		}
	}

	@Override
	public void onAddPost(Post post) {
		Post localPost = localisePost(post);
		if (writer == null)
			try {
				writer = converter.threadToHtml(localPost, targetDir);
			} catch (IOException e) {
				e.printStackTrace();
			}

		try {
			writer.write(converter.postToHtml(localPost));
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (Image img : post.images)
			FileUtil.downloadFile(img.thumbnailUrl,
					targetDir + thumbs + StringUtils.substringAfterLast(img.thumbnailUrl, "/"), 3);
	}

	@Override
	public void onPostsParsingDone() {
		if (writer != null)
			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private Post localisePost(Post post) {
		if (post.images.size() == 0)
			return post;

		Post newPost = new Post();
		newPost.id = post.id;
		newPost.title = post.title;
		newPost.user = post.user;
		newPost.mail = post.mail;
		newPost.message = post.message;
		newPost.date = post.date;
		newPost.countryball = post.countryball;

		for (Image img : post.images) {
			Image newImg = new Image();
			newImg.filename = img.filename;
			newImg.url = post.user + File.separator + img.filename;
			newImg.thumbnailUrl = thumbs + StringUtils.substringAfterLast(img.thumbnailUrl, "/");
			newPost.addImage(newImg);
		}

		return newPost;
	}

}
