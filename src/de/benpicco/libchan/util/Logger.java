package de.benpicco.libchan.util;

import java.util.ArrayList;

public class Logger implements LoggerBackend {
	private static Logger				logger	= null;
	private ArrayList<LoggerBackend>	backends;

	Logger() {
		backends = new ArrayList<LoggerBackend>();
	}

	public static void add(LoggerBackend backend) {
		if (logger == null)
			logger = new Logger();
		logger.backends.add(backend);
	}

	public static LoggerBackend get() {
		if (logger == null)
			logger = new Logger();
		return logger;
	}

	@Override
	public void error(String msg) {
		for (LoggerBackend backend : backends)
			backend.error(msg);
	}

	@Override
	public void println(String msg) {
		for (LoggerBackend backend : backends)
			backend.println(msg);
	}

}
