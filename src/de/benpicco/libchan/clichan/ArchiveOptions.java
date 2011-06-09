package de.benpicco.libchan.clichan;

import java.util.List;

public class ArchiveOptions {
	/**
	 * refresh interval in s, no refresh when <= 0
	 */
	public int			interval;

	/**
	 * save images
	 */
	public boolean		archiveThread;

	/**
	 * where to save all the stuff
	 */
	public String		target;

	/**
	 * use separate folders for threads
	 */
	public boolean		threadFolders;

	/**
	 * save thread as html
	 */
	public boolean		saveHtml;

	/**
	 * names send a notification on
	 */
	public List<String>	names;

	/**
	 * whether to record posting stats or not
	 */
	public boolean		recordStats;
	/**
	 * the FollowUpTag, or null if archiving should not follow new threads
	 */
	public String		followUpTag;

	/**
	 * chan config dir
	 */
	public String		config;

	/**
	 * watch for deleted posts and delete the image
	 */
	public boolean		delete;
}
