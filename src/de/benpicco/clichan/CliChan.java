package de.benpicco.clichan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import de.benpicco.libchan.clichan.CreatePostUtil;
import de.benpicco.libchan.clichan.GlobalOptions;
import de.benpicco.libchan.clichan.ThreadArchiver;
import de.benpicco.libchan.imageboards.Imageboard;
import de.benpicco.libchan.util.FileUtil;
import de.benpicco.libchan.util.Logger;
import de.benpicco.libchan.util.NotImplementedException;
import de.benpicco.libchan.util.ThreadPool;

public class CliChan {

	public static void main(String[] args) {
		ArchiveOptions options = new ArchiveOptions();
		String[] urls = null;
		boolean quick = false;
		options.target = ".";
		options.followUpTag = "NEW THREAD";
		options.interval = -1;
		options.chanConfig = FileUtil.getJarLocation() + "chans" + File.separator;
		options.htmlTemplate = FileUtil.getJarLocation() + "template" + File.separator;
		String[] namesToSearch = null;
		options.saveHtml = false;
		options.saveImages = true;
		options.threadFolders = true;
		options.recordStats = false;
		options.autosage = 500;

		List<File> uploadFiles = null;

		Logger.add(new StdLogger());

		final Options cliOptions = new Options();
		cliOptions.addOption("o", "output", true, "target directory");
		cliOptions.addOption("i", "interval", true, "thread refresh interval");
		cliOptions.addOption("c", "config", true, "chan configuration directory");
		cliOptions.addOption("v", "version", false, "show version");
		cliOptions.addOption("debug", false, "enable debug output");
		cliOptions.addOption("archiveAll", true, "Archive all threads on a board, refresh interval for the main page");
		cliOptions.addOption("d", "deleteDeleted", false, "watch for deleted pots and remove deleted images");
		cliOptions.addOption("tag", true, "follow-up threads tag");
		cliOptions.addOption("html", false, "archive thread as html");
		cliOptions.addOption("nothreadfolders", false, "Do not create a folder for every thread");
		cliOptions.addOption("nopics", false, "Do not download images from the thread");
		cliOptions.addOption("stats", false, "record poster statistics");
		cliOptions.addOption("list", false, "list all supported imageboards");
		cliOptions.addOption("nofollow", false, "Do not try to find a follow-up thread");
		cliOptions.addOption("t", "threads", true, "maximum number of parallel downloads");
		cliOptions.addOption("autosage", true, "postcount for bump limit warning");
		cliOptions.addOption("joinMsg", false, "Display a notification when a new user starts posting in a thread.");
		cliOptions.addOption("quick", false,
				"(option for --find) only search on what's visible on e.g. page 1-10, not every thread");
		cliOptions.addOption("keepFilenames", false,
				"keep the original filenames and do not append the post id to ensure that they are unique");
		cliOptions.addOption("useragent", true, "HTTP Client String");

		Option o = new Option("p", "post", false, "Create a new post, you may specify files or a directory to upload");
		o.setArgs(Integer.MAX_VALUE);
		cliOptions.addOption(o);

		o = new Option("f", "find", true,
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

		o = new Option(
				"vocaroo",
				"Download vocaroo links. If you add a list of usernames as a parameter, only recordings by these users will be downloaded.");
		o.setArgs(Integer.MAX_VALUE);
		o.setOptionalArg(true);
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
				options.chanConfig = commandLine.getOptionValue('c');
			if (commandLine.hasOption('f'))
				namesToSearch = commandLine.getOptionValues('f');
			if (commandLine.hasOption('n')) {
				String[] names = commandLine.getOptionValues('n');
				options.names = new ArrayList<String>(names.length);
				for (String name : names)
					options.names.add(name.toLowerCase());
			}
			if (commandLine.hasOption("tag"))
				options.followUpTag = commandLine.getOptionValue("tag");
			if (commandLine.hasOption("nofollow"))
				options.followUpTag = null;
			if (commandLine.hasOption("vocaroo"))
				options.vocaroo = commandLine.getOptionValues("vocaroo") == null ? new String[0] : commandLine
						.getOptionValues("vocaroo");
			if (commandLine.hasOption('t'))
				ThreadPool.setPoolSize(Integer.parseInt(commandLine.getOptionValue('t')));
			if (commandLine.hasOption("autosage"))
				options.autosage = Integer.parseInt(commandLine.getOptionValue("autosage"));
			if (commandLine.hasOption("useragent"))
				GlobalOptions.useragent = commandLine.getOptionValue("useragent");
			if (commandLine.hasOption("keepFilenames"))
				GlobalOptions.useUniqueFilenames = false;
			if (commandLine.hasOption("debug"))
				GlobalOptions.debug = true;
			quick = commandLine.hasOption("quick");

			if (commandLine.hasOption('v')) {
				System.out.println("cliChan using libChan " + ThreadArchiver.VERSION
						+ "\nhttp://libchan.googlecode.com/");
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
			options.onJoinMsg = commandLine.hasOption("joinMsg");

			if (commandLine.hasOption("list")) {
				ChanManager manager = new ChanManager(options.chanConfig, options.htmlTemplate);
				for (Imageboard board : manager.getSupported())
					System.out.println(board);
				return;
			}

			if (commandLine.hasOption('p')) {
				String[] params = commandLine.getOptionValues('p');
				uploadFiles = new ArrayList<File>();
				for (String param : params) {
					File f = new File(param);
					if (f.isDirectory()) {
						for (File file : f.listFiles())
							if (file.isFile())
								uploadFiles.add(file);
					} else
						uploadFiles.add(f);
				}
			}

		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
			return;
		}

		if (urls == null) {
			System.err.println("No url specified");
			new HelpFormatter().printHelp("You have to specify at least one URL using the -u option.", cliOptions);
			return;
		}

		ThreadArchiver archiver = null;
		BoardArchiver boardArchiver = null;

		if (namesToSearch == null && uploadFiles == null)
			archiver = new ThreadArchiver(options);
		else if (uploadFiles != null) {
			CreatePostUtil cpu = new CreatePostUtil(urls[0], options);
			CliDialog d = new CliDialog();

			try {
				cpu.uploadFiles(d.ask("name"), d.ask("subject"), d.ask("mail"), d.ask("password", "deletepasswd"),
						d.ask("message"), Integer.parseInt(d.ask("delay between two posts", "30")), uploadFiles);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotImplementedException e) {
				Logger.get().error("No support for creating posts was added for this imageboard.");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return;
		}

		if (options.boardInterval > 0) {
			if (options.interval <= 0)
				options.interval = options.boardInterval * 3;
			if (urls.length > 1)
				options.noBoardFolders = false;
			else
				options.noBoardFolders = true;
			boardArchiver = new BoardArchiver(options);
		}

		for (String url : urls) {
			int anchor = url.indexOf('#');
			if (anchor > 0)
				url = url.substring(0, anchor);

			if (namesToSearch != null) // crawler mode
				ChanCrawler.lookFor(namesToSearch, url, quick, 0, 20, options.chanConfig);
			else if (boardArchiver != null) // archive an entire board
				boardArchiver.addBoard(url);
			else
				// archive a single thread
				archiver.addThread(url);
		}

		if (boardArchiver != null)
			boardArchiver.run();
		else if (archiver != null)
			archiver.run();
	}
}
