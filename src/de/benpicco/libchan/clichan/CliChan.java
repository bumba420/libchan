package de.benpicco.libchan.clichan;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.benpicco.libchan.util.FileUtil;

public class CliChan {
	public static void main(String[] args) {
		String url = null;
		String out = ".";
		String followUpTag = "NEW THREAD";
		int interval = -1;
		String chancfg = FileUtil.getJarLocation() + "chans" + File.separator;
		String[] names = null;
		boolean html = false;

		final Options cliOptions = new Options();
		cliOptions.addOption("u", "url", true, "url to process");
		cliOptions.addOption("o", "output", true, "target directory");
		cliOptions.addOption("i", "interval", true, "thread refresh interval");
		cliOptions.addOption("c", "config", true, "chan configuration directory");
		cliOptions.addOption("v", "version", false, "show version");
		cliOptions.addOption("tag", true, "follow-up threads tag");
		cliOptions.addOption("html", false, "also archive thread as html");

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
			if (commandLine.hasOption('v')) {
				System.out.println("cliChan using libChan");
				System.exit(0);
			}
			html = commandLine.hasOption("html");

		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (url == null) {
			System.err.println("No url specified");
			new HelpFormatter().printHelp("use -t to specify either a thread to archive or a board to crawl",
					cliOptions);
			return;
		}

		if (!chancfg.endsWith(File.separator))
			chancfg += File.separator;

		if (names != null) {
			ChanCrawler.lookFor(names, url, 0, 15, chancfg);
			return;
		}

		ThreadArchiver archiver = new ThreadArchiver(url, out, chancfg, interval, followUpTag, html);
		archiver.saveThread();
	}
}
