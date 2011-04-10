package de.benpicco.libchan.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
}

class PathHelper {
	public final String	jarLocation;

	public PathHelper() {
		String tmp = ThreadArchiver.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		jarLocation = tmp.substring(0, tmp.lastIndexOf(File.separatorChar) + 1);
	}
}
