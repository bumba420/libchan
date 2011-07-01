package de.benpicco.libchan.streamparser;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.benpicco.libchan.util.Logger;

/**
 * Downloads an InputStream in a Background thread (should only be used when the
 * stream has a end, e.g. when downloading a website) so the download is not
 * blocked by operations performed on the downloaded bytes
 */
public class BackgroundDownloader {
	final Reader						in;
	final int							buffsize;
	private final BlockingQueue<Chunk>	fifo;
	private boolean						finished	= false;
	private IOException					exception	= null;
	private Worker						worker		= null;
	private boolean						nothread	= true;	// TODO: figure out why this slows down the crawler

	public BackgroundDownloader(int buffsize, Reader in) {
		this.in = in;
		this.buffsize = buffsize;
		fifo = new LinkedBlockingQueue<Chunk>();
	}

	public Chunk get() throws IOException {
		if (nothread)
			return read();

		if (worker == null)
			new Thread(worker = new Worker()).start();

		if (exception != null)
			throw exception;
		if (finished && fifo.isEmpty())
			return null;
		try {
			return fifo.take();
		} catch (InterruptedException e) {
			Logger.get().error("Cought InterruptedException " + e.getMessage());
			return fifo.poll();
		}
	}

	private Chunk read() throws IOException {
		char[] buffer = new char[buffsize];
		int read = in.read(buffer);
		if (read > 0)
			return new Chunk(buffer, read);
		else
			return null;
	}

	class Worker implements Runnable {

		private boolean	running	= true;

		@Override
		public void run() {
			Chunk c;
			try {
				while ((c = read()) != null && running)
					fifo.add(c);
			} catch (IOException e) {
				exception = e;
			}
			finished = true;
		}

		public void stop() {
			running = false;
		}
	}

	public void stop() {
		if (worker != null)
			worker.stop();
	}
}

class Chunk {
	public final int	len;
	public final char[]	data;

	public Chunk(char[] data, int len) {
		this.data = data;
		this.len = len;
	}
}
