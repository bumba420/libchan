package de.benpicco.clichan;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.benpicco.libchan.util.LoggerBackend;

/**
 * Writes log messages to stdout/stderr.
 */
public class StdLogger implements LoggerBackend {
	private final SimpleDateFormat	format	= new SimpleDateFormat("[HH:mm:ss] ");

	private String time() {
		return format.format(Calendar.getInstance().getTime());
	}

	@Override
	public void print(String msg) {
		System.out.print(msg);
	}

	@Override
	public void error(String msg) {
		System.err.println(time() + msg);
	}

	@Override
	public void println(String msg) {
		System.out.println(time() + msg);
	}
}
