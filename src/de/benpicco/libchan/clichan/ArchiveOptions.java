package de.benpicco.libchan.clichan;

import java.util.List;

public class ArchiveOptions {
	/**
	 * refresh interval in s, no refresh when <= 0
	 */
	public int			interval;

	/**
	 * If an entire board should be archived, this sets the delay between two
	 * refreshes of the main page.
	 */
	public int			boardInterval;

	/**
	 * save images
	 */
	public boolean		saveImages;

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
	public String		chanConfig;

	/**
	 * html template directory
	 */
	public String		htmlTemplate;

	/**
	 * watch for deleted posts and delete the image
	 */
	public boolean		delete;

	/**
	 * do not create a folder for every board when monitoring an entire board
	 */
	public boolean		noBoardFolders;

	/**
	 * download vocaroo recordings for the given users, if this is empty, all
	 * vocaroos will be downloaded, if this is null, no vocaroos will be
	 * downloaded.
	 */
	public String[]		vocaroo;

	/**
	 * when warning about the bump limit should be shown
	 */

	public int			autosage;

	/**
	 * create a notification when a new user starts posting in a thread
	 */
	public boolean		onJoinMsg;
}
