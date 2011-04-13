package de.benpicco.libchan.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;

import de.benpicco.libchan.clichan.ThreadArchiver;

public class FileUtil {
	private static String	jarLocation	= null;

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

	public static String getJarLocation() {
		if (jarLocation == null)
			jarLocation = new PathHelper().jarLocation;
		return jarLocation;
	}

	public static void downloadFile(String url, String filename, int tries) {
		File file = new File(filename);
		while (--tries > 0)
			try {
				if (file.exists()) {
					System.out.println(filename + " already exists, skipping.");
					break;
				}
				downloadFile(url, filename);
				System.out.println("Saved " + url + " as " + filename);

				break;
			} catch (Exception e) {
				file.delete();
				System.err.println("Failed to save " + url + " as " + filename + " (" + e + ")");
				if (tries > 0)
					System.out.println("retrying…");
				else
					System.err.println("…giving up on " + url + " (" + filename + ")");
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
}

class PathHelper {
	public final String	jarLocation;

	public PathHelper() {
		String tmp = ThreadArchiver.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		jarLocation = tmp.substring(0, tmp.lastIndexOf(File.separatorChar) + 1);
	}
}
