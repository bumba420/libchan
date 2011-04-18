package de.benpicco.libchan.imageboards;

import java.io.IOException;
import java.util.List;

import de.benpicco.libchan.PostHandler;
import de.benpicco.libchan.ThreadHandler;
import de.benpicco.libchan.streamparser.StreamParser;
import de.benpicco.libchan.util.Tuple;

public class AsyncImageBoardParser extends GenericImageBoardParser {

	public AsyncImageBoardParser(String baseUrl, List<Tags> postStarter, List<Tags> postEnder, List<Tags> imageEnder,
			StreamParser parser, String imgPrefix, String thumbPrefix, String countryPrefix,
			Tuple<String, String> threadURL) {
		super(baseUrl, postStarter, postEnder, imageEnder, parser, imgPrefix, thumbPrefix, countryPrefix, threadURL);
	}

	@Override
	public void getPosts(final String url, final PostHandler rec) {
		if (url == null)
			return;

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					AsyncImageBoardParser.super.getPosts(url, rec);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void getThreads(final String url, final ThreadHandler rec) throws IOException {
		if (url == null)
			return;

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					AsyncImageBoardParser.super.getThreads(url, rec);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
}