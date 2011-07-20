package de.benpicco.libchan.util;

public interface LoggerBackend {
	public void println(String msg);

	public void error(String msg);
}
