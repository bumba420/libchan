package de.benpicco.clichan;

import de.benpicco.libchan.util.LoggerBackend;

/**
 * Writes log messages to stdout/stderr.
 */
public class StdLogger implements LoggerBackend {
	@Override
	public void print(String msg) {
		System.out.print(msg);
	}

	@Override
	public void error(String msg) {
		System.err.println(msg);
	}

	@Override
	public void println(String msg) {
		System.out.println(msg);
	}
}
