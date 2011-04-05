package de.benpicco.libchan;


public interface IThreadReceiver {
	public void onAddThread(Thread thread);

	public void onThreadsParsingDone();
}
