package de.benpicco.libchan.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;

import org.apache.commons.lang3.StringUtils;

public class FileUtil {
	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists())
			destFile.createNewFile();

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	public static void copyDirectory(File srcDir, File dstDir) throws IOException {
		if (srcDir.isDirectory()) {
			if (!dstDir.exists())
				dstDir.mkdir();

			for (String child : srcDir.list())
				copyDirectory(new File(srcDir, child), new File(dstDir, child));
		} else {
			copyFile(srcDir, dstDir);
		}
	}

	public static String getJarLocation() {
		String[] path = System.getProperty("java.class.path").split(File.pathSeparator);
		return (path[0].contains(File.separator) ? StringUtils.substringBeforeLast(path[0], File.separator) : ".")
				+ File.separator;

	}

	public static void downloadFile(String url, String filename, int tries) {
		File file = new File(filename);
		while (--tries >= 0)
			try {
				if (file.exists()) {
					Logger.get().println(filename + " already exists, skipping.");
					break;
				}
				downloadFile(url, filename);
				Logger.get().println("Saved " + url + " as " + filename);

				break;
			} catch (Exception e) {
				file.delete();
				if (tries <= 0)
					Logger.get().error("Failed to save " + url + " as " + filename + " (" + e + ")");
				else
					try {
						Thread.sleep(250);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
			}
	}

	public static void downloadFile(String url, String target) throws MalformedURLException, IOException {
		byte[] buffer = new byte[2048];
		BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
		OutputStream out = new FileOutputStream(target);
		int count = 0;
		do {
			count = in.read(buffer);
			if (count > 0)
				out.write(buffer, 0, count);
		} while (count > 0);
		out.close();
		in.close();
	}

	public static String filterInvalidChars(String filename, String replacement) {
		String regex;
		if (System.getProperty("os.name").toLowerCase().contains("win"))
			regex = "[\\?\\:\\\\/\\*\"\\<\\>\\|]";
		else
			regex = "\\/";
		return filename.replaceAll(regex, replacement);
	}

	/**
	 * Reads file and returns its content as a String.
	 * 
	 * @param file
	 *            the file to read
	 * @return the content of the file
	 * @throws IOException
	 */
	public static String fileToString(String file) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(new File(file)));
		StringBuilder out = new StringBuilder();

		String read = null;
		while (true) {
			read = in.readLine();
			if (read != null)
				out.append(read + "\n");
			else
				break;
		}

		in.close();
		return out.toString();
	}

	/**
	 * Creates a folder with the given name and appends a separator char if
	 * necessary
	 * 
	 * @param target
	 *            new folder
	 * @return name of the nwe folder (with closing separator)
	 */
	public static String prepareDir(String target) {
		String dir = target.endsWith(File.separator) ? target : target + File.separator;
		new File(dir).mkdir();
		return dir;
	}
}
