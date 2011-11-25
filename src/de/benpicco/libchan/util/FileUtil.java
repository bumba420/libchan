package de.benpicco.libchan.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;

import org.apache.commons.lang3.StringUtils;

import de.benpicco.libchan.clichan.GlobalOptions;

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

	public static URI encodeUrl(String url) throws URISyntaxException {
		final String protocol = StringUtils.substringBefore(url, "://");
		final String host = StringUtils.substringBetween(url, protocol + "://", "/");
		final String path = StringUtils.substringBefore(url.substring((protocol + "://" + host).length()), "?");
		final int pos = (protocol + "://" + host + path + "?").length();
		final String param = pos < url.length() ? url.substring(pos) : null;

		return new URI(protocol, host, path, param);
	}

	public static void downloadFile(String url, String filename, int tries) {
		URI uri;
		try {
			uri = encodeUrl(url);
		} catch (URISyntaxException e) {
			Logger.get().error("Invalid URL: " + url);
			return;
		}
		File file = new File(filename);
		while (--tries >= 0)
			try {
				if (file.exists()) {
					Logger.get().println(filename + " already exists, skipping.");
					break;
				}
				downloadFile(uri.toURL(), filename);
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

	private static void downloadFile(URL url, String target) throws MalformedURLException, IOException {
		byte[] buffer = new byte[2048];

		URLConnection connection = url.openConnection();
		if (connection instanceof HttpURLConnection)
			connection.setRequestProperty("User-Agent", GlobalOptions.useragent);

		BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
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
	 * @return name of the new folder (with closing separator)
	 */
	public static String prepareDir(String target) {
		String dir = target.endsWith(File.separator) ? target : target + File.separator;
		new File(dir).mkdir();
		return dir;
	}

	public static long pipe(InputStream is, OutputStream os, ProgressCallback callback) throws IOException {
		long total = 0;
		byte[] buffer = new byte[1024];
		int bytes = 0;

		while (true) {
			bytes = is.read(buffer);
			if (bytes <= 0)
				break;
			os.write(buffer, 0, bytes);
			total += bytes;

			if (callback != null)
				callback.written(total);
		}

		return total;
	}

	public static String getMimeType(File file) {
		return URLConnection.getFileNameMap().getContentTypeFor(file.getName());
	}
}
