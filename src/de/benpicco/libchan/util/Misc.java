package de.benpicco.libchan.util;

import org.apache.commons.lang3.StringEscapeUtils;

public class Misc {
	/**
	 * works around an issue in StringEscapeUtils that leads to a crash when
	 * there is a & in the html followed by an invalid escape sequence
	 * 
	 * @param input
	 * @return
	 */
	public static String unescapeHtml(String input) {
		try {
			return StringEscapeUtils.unescapeHtml4(input);
		} catch (Exception e) {
			return input;
		}
	}

	/**
	 * Returns a comma separated String of the items in an array.
	 * 
	 * @param names
	 * @return
	 */
	public static String printNames(String[] names) {
		String printNames = "";
		for (int i = 0; i < names.length; ++i)
			printNames += (i > 0 ? ", " : "") + names[i];
		return printNames;
	}
}
