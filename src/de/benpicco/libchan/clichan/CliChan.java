package de.benpicco.libchan.clichan;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CliChan {
	public static void main(String[] args) {
		String url = null;
		String out = ".";
		String followUpTag = "NEW THREAD";
		int interval = -1;
		String chancfg = new PathHelper().jarLocation + "chans/";
		String[] names = null;

		final Options cliOptions = new Options();
		cliOptions.addOption("u", "url", true, "url to process");
		cliOptions.addOption("o", "output", true, "target directory");
		cliOptions.addOption("i", "interval", true, "thread refresh interval");
		cliOptions.addOption("c", "config", true, "chan configuration directory");
		cliOptions.addOption("tag", true, "follow-up threads tag");

		Option o = new Option("f", "find", true, "Searches the imageborad for users");
		o.setArgs(Integer.MAX_VALUE);
		cliOptions.addOption(o);

		final CommandLineParser cliParser = new GnuParser();
		try {
			CommandLine commandLine = cliParser.parse(cliOptions, args);
			if (commandLine.hasOption('u'))
				url = commandLine.getOptionValue('u');
			if (commandLine.hasOption('o'))
				out = commandLine.getOptionValue('o');
			if (commandLine.hasOption('i'))
				interval = Integer.parseInt(commandLine.getOptionValue('i'));
			if (commandLine.hasOption('c'))
				chancfg = commandLine.getOptionValue('c');
			if (commandLine.hasOption('f'))
				names = commandLine.getOptionValues('f');

		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (url == null) {
			System.err.println("No url specified");
			new HelpFormatter().printHelp("use -t to specify either a thread to archive or a board to crawl",
					cliOptions);
			return;
		}

		if (names != null) {
			ChanCrawler.lookFor(names, url, 0, 15, chancfg);
			return;
		}

		ThreadArchiver archiver = new ThreadArchiver(url, out, chancfg, interval, followUpTag, false);
		archiver.archiveThread(0);
	}
}

class PathHelper {
	public final String	jarLocation;

	public PathHelper() {
		String tmp = ThreadArchiver.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		jarLocation = tmp.substring(0, tmp.lastIndexOf(File.separatorChar) + 1);
	}
}
