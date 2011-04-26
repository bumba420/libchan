package de.benpicco.libchan.util;

public class Logger implements LoggerBackend {
	private static Logger	logger	= null;
	private LoggerBackend	backend;

	Logger(LoggerBackend backend) {
		this.backend = backend;
	}

	public static void initialise(LoggerBackend backend) {
		logger = new Logger(backend);
	}

	public static LoggerBackend get() {
		if (logger == null)
			logger = new Logger(null);
		return logger;
	}

	@Override
	public void print(String msg) {
		if (backend != null)
			backend.print(msg);
	}

	@Override
	public void error(String msg) {
		if (backend != null)
			backend.print(msg);
	}

	@Override
	public void println(String msg) {
		print(msg + "\n");
	}

}
