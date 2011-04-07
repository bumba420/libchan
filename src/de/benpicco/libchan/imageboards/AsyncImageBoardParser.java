package de.benpicco.libchan.imageboards;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import de.benpicco.libchan.IPostReceiver;
import de.benpicco.libchan.IThreadReceiver;
import de.benpicco.libchan.imageboards.Message.Type;
import de.benpicco.libchan.streamparser.StreamParser;
import de.benpicco.libchan.util.Tuple;

public class AsyncImageBoardParser extends GenericImageBoardParser {

	private final BlockingQueue<Message>	messages;

	/**
	 * Wraps an IImageBoardParser to enable non-blocking get-Methods
	 */
	public AsyncImageBoardParser(String baseUrl, List<Tags> postStarter, List<Tags> postEnder, List<Tags> imageEnder,
			StreamParser parser, String imgPrefix, String thumbPrefix, String countryPrefix,
			Tuple<String, String> threadURL) {
		super(baseUrl, postStarter, postEnder, imageEnder, parser, imgPrefix, thumbPrefix, countryPrefix, threadURL);

		messages = new LinkedBlockingDeque<Message>();

		new Thread(new Runnable() {

			@Override
			public void run() {

				while (true) {

					Message message = null;
					try {
						message = messages.take();
					} catch (InterruptedException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					if (message.url == null)
						return;

					// System.out.println(message);

					try {
						if (message.type == Type.POST)
							AsyncImageBoardParser.super.getPosts(message.url, (IPostReceiver) message.receiver);
						else if (message.type == Type.THREAD)
							AsyncImageBoardParser.super.getThreads(message.url, (IThreadReceiver) message.receiver);

					} catch (FileNotFoundException e) {
						System.err.println(message.url + " does not esist");
						return; // XXX propagate error
					} catch (IOException e) {
						System.err.println(e);
						try {
							System.out.println("Retry…");
							messages.put(message); // XXX infinite loop
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}

				}
			}
		}).start();
	}

	public void dispose() {
		messages.add(new Message(null, null, null));
	}

	@Override
	public void getPosts(String url, IPostReceiver rec) throws IOException {
		try {
			messages.put(new Message(url, rec, Type.POST));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void getThreads(String url, IThreadReceiver rec) throws IOException {
		try {
			messages.put(new Message(url, rec, Type.THREAD));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class Message {
	public final String	url;
	public final Object	receiver;
	public final Type	type;

	enum Type {
		POST, THREAD
	};

	public Message(String url, Object receiver, Type type) {
		this.url = url;
		this.receiver = receiver;
		this.type = type;
	}

	public String toString() {
		return type + ": " + url;
	}
}
