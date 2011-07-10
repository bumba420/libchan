package de.benpicco.libchan.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ThreadPool {
	private static int									threadCount	= 0;
	private static int									poolSize	= 20;
	private static BlockingQueue<Tuple<String, String>>	queue		= new LinkedBlockingDeque<Tuple<String, String>>();

	public static void addDownload(String url, String dest) {
		queue.add(new Tuple<String, String>(url, dest));
		governThread(1);
	}

	public static void setPoolSize(int threads) {
		poolSize = threads;
	}

	protected static synchronized void governThread(int i) {
		if (i > 0)
			for (int j = 0; j <= i && threadCount <= poolSize; ++j, ++threadCount)
				new workerThread(queue).start();
		else
			threadCount -= i;
		if (threadCount < 0)
			threadCount = 0;
	}
}

class workerThread extends Thread {
	private final BlockingQueue<Tuple<String, String>>	queue;

	public workerThread(BlockingQueue<Tuple<String, String>> queue) {
		this.queue = queue;
	}

	@Override
	public void run() {
		Tuple<String, String> job;
		while ((job = queue.poll()) != null)
			FileUtil.downloadFile(job.first, job.second, 5);
		ThreadPool.governThread(-1);
	}

}