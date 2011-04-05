package de.benpicco.libchan.imageboards;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import de.benpicco.libchan.IImageBoardParser;
import de.benpicco.libchan.IPostReceiver;
import de.benpicco.libchan.IThreadReceiver;
import de.benpicco.libchan.imageboards.Message.Type;

public class AsyncImageBoardParser implements IImageBoardParser {

	private final IImageBoardParser			parser;
	private final BlockingQueue<Message>	messages;

	/**
	 * Wraps an IImageBoardParser to enable non-blocking get-Methods
	 */
	public AsyncImageBoardParser(final IImageBoardParser parser) {
		this.parser = parser;
		messages = new LinkedBlockingDeque<Message>();

		if (parser == null)
			return;

		new Thread(new Runnable() {

			@Override
			public void run() {

				Message message = messages.poll();
				if (message == null)
					return;

				try {
					if (message.type == Type.POST)
						parser.getPosts(message.url, (IPostReceiver) message.receiver);
					else if (message.type == Type.THREAD)
						parser.getThreads(message.url, (IThreadReceiver) message.receiver);

				} catch (IOException e) {
					System.err.println(e);
					try {
						System.out.println("Retry…");
						messages.put(message);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}

				run();
			}
		}).start();
	}

	protected void finalize() {
		messages.add(null);
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

	@Override
	public String composeUrl(String url, Post post) {
		return parser.composeUrl(url, post);
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
}
