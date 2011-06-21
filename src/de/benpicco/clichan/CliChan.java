package de.benpicco.clichan;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.benpicco.libchan.clichan.ArchiveOptions;
import de.benpicco.libchan.clichan.BoardArchiver;
import de.benpicco.libchan.clichan.ChanCrawler;
import de.benpicco.libchan.clichan.ChanManager;
import de.benpicco.libchan.clichan.ThreadArchiver;
import de.benpicco.libchan.imageboards.Imageboard;
import de.benpicco.libchan.util.FileUtil;
import de.benpicco.libchan.util.Logger;

public class CliChan {
	public final static String	VERSION	= "0.3.1";

	public static void main(String[] args) {
		ArchiveOptions options = new ArchiveOptions();
		String[] urls = null;
		options.target = ".";
		options.followUpTag = "NEW THREAD";
		options.interval = -1;
		options.config = FileUtil.getJarLocation() + "chans" + File.separator;
		String[] namesToSearch = null;
		options.saveHtml = false;
		options.saveImages = true;
		options.threadFolders = true;
		options.recordStats = false;

		Logger.add(new StdLogger());

		final Options cliOptions = new Options();
		cliOptions.addOption("o", "output", true, "target directory");
		cliOptions.addOption("i", "interval", true, "thread refresh interval");
		cliOptions.addOption("c", "config", true, "chan configuration directory");
		cliOptions.addOption("v", "version", false, "show version");
		cliOptions.addOption("archiveAll", true, "Archive all threads on a board, refresh interval for the main page");
		cliOptions.addOption("d", "deleteDeleted", false, "watch for deleted pots and remove deleted images");
		cliOptions.addOption("tag", true, "follow-up threads tag");
		cliOptions.addOption("html", false, "archive thread as html");
		cliOptions.addOption("nothreadfolders", false, "Do not create a folder for every thread");
		cliOptions.addOption("noarchive", false, "Do not download images from the thread");
		cliOptions.addOption("stats", false, "record poster statistics");
		cliOptions.addOption("list", false, "list all supported imageboards");

		Option o = new Option("f", "find", true,
				"Searches the imageborad for users, paramaters are usernames, seperated by spaces (use \" for names containing spaces)");
		o.setArgs(Integer.MAX_VALUE);
		cliOptions.addOption(o);

		o = new Option("n", "notify", true,
				"show a notification for the following users (uses notify-send on Linux and growl on Windows and OS X (untested))");
		o.setArgs(Integer.MAX_VALUE);
		cliOptions.addOption(o);

		o = new Option("u", "url", true,
				"one or more urls to process, may be a thread or a board in when the --find option is used");
		o.setArgs(Integer.MAX_VALUE);
		cliOptions.addOption(o);

		final CommandLineParser cliParser = new GnuParser();
		try {
			CommandLine commandLine = cliParser.parse(cliOptions, args);
			if (commandLine.hasOption('u'))
				urls = commandLine.getOptionValues('u');
			if (commandLine.hasOption('o'))
				options.target = commandLine.getOptionValue('o');
			if (commandLine.hasOption('i'))
				options.interval = Integer.parseInt(commandLine.getOptionValue('i')) * 1000;
			if (commandLine.hasOption('c'))
				options.config = commandLine.getOptionValue('c');
			if (commandLine.hasOption('f'))
				namesToSearch = commandLine.getOptionValues('f');
			if (commandLine.hasOption('n')) {
				String[] names = commandLine.getOptionValues('n');
				options.names = new ArrayList<String>(names.length);
				for (String name : names)
					options.names.add(name.toLowerCase());
			}
			if (commandLine.hasOption('v')) {
				System.out.println("cliChan " + VERSION + " using libChan\nhttp://libchan.googlecode.com/");
				System.exit(0);
			}

			if (commandLine.hasOption("archiveAll")) {
				options.boardInterval = Integer.parseInt(commandLine.getOptionValue("archiveAll")) * 1000;
				if (options.interval <= 0)
					options.interval = options.boardInterval;
				options.followUpTag = null; // new threads will get added anyway
			}

			options.saveHtml = commandLine.hasOption("html");
			options.threadFolders = !commandLine.hasOption("nothreadfolders");
			options.saveImages = !commandLine.hasOption("noarchive");
			options.recordStats = commandLine.hasOption("stats");
			options.delete = commandLine.hasOption("d");

			if (commandLine.hasOption("list")) {
				ChanManager manager = new ChanManager(options.config);
				for (Imageboard board : manager.getSupported())
					System.out.println(board);
				return;
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (urls == null) {
			System.err.println("No url specified");
			new HelpFormatter().printHelp("You have to specify at least one URL using the -u option.", cliOptions);
			return;
		}

		ThreadArchiver archiver = null;
		BoardArchiver boardArchiver = null;

		if (namesToSearch == null)
			archiver = new ThreadArchiver(options);

		if (options.boardInterval > 0)
			boardArchiver = new BoardArchiver(options);

		for (String url : urls) {
			int anchor = url.indexOf('#');
			if (anchor > 0)
				url = url.substring(0, anchor);

			if (namesToSearch != null)
				ChanCrawler.lookFor(namesToSearch, url, 0, 15, options.config);
			else if (boardArchiver != null)
				boardArchiver.addBoard(url);
			else
				archiver.addThread(url);
		}

		if (boardArchiver != null)
			boardArchiver.run();
		else if (archiver != null)
			archiver.run();
	}
}
