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

	/**
	 * Returns the position of the element in the array that contains key, or
	 * -1. The array has to be lowercase
	 * 
	 * @param array
	 * @param key
	 * @return
	 */
	public static int containsAlike(String[] array, String key) {
		if (array == null)
			return -1;

		key = key.toLowerCase();

		for (int i = 0; i < array.length; ++i)
			if ((key.contains(array[i])))
				return i;
		return -1;
	}

	/**
	 * Returns the position of the element in the array that equals key, or -1.
	 * 
	 * @param array
	 * @param key
	 * @return
	 */
	public static int contains(String[] array, String key) {
		if (array == null)
			return -1;

		for (int i = 0; i < array.length; ++i)
			if (key.equals(array[i]))
				return i;
		return -1;
	}
}
