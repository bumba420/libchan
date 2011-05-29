package de.benpicco.libchan.handler;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.benpicco.libchan.imageboards.Post;
import de.benpicco.libchan.interfaces.PostHandler;
import de.benpicco.libchan.util.FileUtil;

public class StatisticsHandler implements PostHandler {

	final HashMap<Integer, Stats>	usermapping;
	final ArrayList<Stats>			users;
	final String					baseDir;
	final boolean					threadFolder;

	private String					targetDir;

	private int						threadId	= 0;
	private FileWriter				graph		= null;

	public StatisticsHandler(String target, boolean threadFolder) {
		this.threadFolder = threadFolder;
		usermapping = new HashMap<Integer, Stats>();
		users = new ArrayList<Stats>();
		baseDir = FileUtil.prepareDir(target);
	}

	@Override
	public void onAddPost(Post post) {
		if (post.isFirstPost) {
			threadId = post.id;
			if (threadFolder)
				targetDir = FileUtil.prepareDir(baseDir + threadId);
			else
				targetDir = baseDir;

			try {
				graph = new FileWriter(targetDir + threadId + ".gv");
				graph.write("digraph " + threadId + " {\n");
				graph.write("\tnode [color=lightblue2, style=filled];\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		boolean found = false;
		for (Stats s : users)
			if (s.addPost(post, graph)) {
				found = true;
				break;
			}
		if (!found)
			users.add(new Stats(post, usermapping, graph));
	}

	@Override
	public void onPostsParsingDone() {
		try {
			FileWriter writer = new FileWriter(targetDir + threadId + ".csv");
			writer.write(Stats.getHeader());
			for (Stats s : users)
				writer.write(s.toString());
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (graph != null) {
			graph.append("}");
			graph.close();
		}
	}
}

class Stats {
	final String							user;
	private int								posts				= 0;
	private int								images				= 0;
	private int								posts_with_images	= 0;
	private int								text				= 0;
	private int								responses			= 0;
	private int								responded			= 0;

	private final Pattern					responsePattern;
	private final HashMap<Integer, Stats>	usermapping;

	public Stats(Post post, HashMap<Integer, Stats> usermapping, FileWriter graph) {
		this.user = post.user;
		this.usermapping = usermapping;
		responsePattern = Pattern.compile(">>([0-9]+)");
		addPost(post, graph);
	}

	public boolean addPost(Post post, FileWriter graph) {
		if (!post.user.equals(user))
			return false;

		usermapping.put(post.id, this);
		posts++;
		images += post.images.size();
		if (post.images.size() > 0)
			posts_with_images++;
		text += post.message.length();

		Matcher matcher = responsePattern.matcher(post.message);
		int lastId = 0; // do not count the same response more than once
		while (matcher.find()) {
			int id = 0;
			try {
				id = Integer.parseInt(matcher.group(1));
			} catch (Exception e) {
				continue;
			}
			if (lastId == id)
				continue;
			lastId = id;
			responded++;

			Stats target = usermapping.get(id);
			if (target != null) {
				try {
					graph.append("\t\"" + post.user + "\"" + " -> \"" + target.user + "\";\n");
					graph.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				target.responed();
			}
		}

		return true;
	}

	void responed() {
		responses++;
	}

	public static String getHeader() {
		return "User\tPosts\twith Images\tImages\tavg. Text\tresponded\tresponses\n";
	}

	public String toString() {
		return user + '\t' + posts + '\t' + posts_with_images + '\t' + images + '\t' + (text / posts) + '\t'
				+ responded + '\t' + responses + '\n';
	}
}
