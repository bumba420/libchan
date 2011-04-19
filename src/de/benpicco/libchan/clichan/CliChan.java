package de.benpicco.libchan.clichan;

import java.io.File;
import java.util.ArrayList;

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
		String[] namesToSearch = null;
		ArrayList<String> namesToWach = null;
		boolean html = false;
		boolean threadFolders = true;

		final Options cliOptions = new Options();
		cliOptions.addOption("u", "url", true, "url to process");
		cliOptions.addOption("o", "output", true, "target directory");
		cliOptions.addOption("i", "interval", true, "thread refresh interval");
		cliOptions.addOption("c", "config", true, "chan configuration directory");
		cliOptions.addOption("v", "version", false, "show version");
		cliOptions.addOption("tag", true, "follow-up threads tag");
		cliOptions.addOption("html", false, "also archive thread as html");
		cliOptions.addOption("nothreadfolders", false, "Do not create a folder for every thread");

		Option o = new Option("f", "find", true, "Searches the imageborad for users");
		o.setArgs(Integer.MAX_VALUE);
		cliOptions.addOption(o);
		o = new Option("n", "notify", true, "show a notification when the following users post");
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
				namesToSearch = commandLine.getOptionValues('f');
			if (commandLine.hasOption('n')) {
				String[] names = commandLine.getOptionValues('n');
				namesToWach = new ArrayList<String>(names.length);
				for (String name : names)
					namesToWach.add(name.toLowerCase());
			}
			if (commandLine.hasOption('v')) {
				System.out.println("cliChan using libChan");
				System.exit(0);
			}
			html = commandLine.hasOption("html");
			threadFolders = !commandLine.hasOption("nothreadfolders");

		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (url == null) {
			System.err.println("No url specified");
			new HelpFormatter().printHelp("You have to at least specify a URL using the -u option.", cliOptions);
			return;
		}

		if (!chancfg.endsWith(File.separator))
			chancfg += File.separator;

		if (namesToSearch != null) {
			ChanCrawler.lookFor(namesToSearch, url, 0, 15, chancfg);
			return;
		}

		ThreadArchiver archiver = new ThreadArchiver(url, out, threadFolders, chancfg, interval, followUpTag, html,
				namesToWach);
		archiver.saveThread();
	}
}
