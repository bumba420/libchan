package de.benpicco.libchan.imageboards;

import java.io.File;

/**
 * This class is used to hold the url of an image as the url for it's thumbnail.
 */
public class Image {
	public String	thumbnailUrl;
	public String	url;
	public String	filename;

	public String toString() {
		return filename + " (" + thumbnailUrl + ", " + url + ")\n";
	}

	protected void cleanup() {
		if (url != null) {
			if (filename == null)
				filename = url.substring(url.lastIndexOf('/') + 1);

			// clear malicious filenames
			filename = filename.replace(File.separatorChar, ' ');

			if (thumbnailUrl == null)
				thumbnailUrl = url;
		}
	}

	public boolean isReady() {
		return thumbnailUrl != null && url != null && filename != null;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Image && equals(this, (Image) o);
	}

	public static boolean equals(Image img1, Image img2) {
		return img1.thumbnailUrl.equals(img2.thumbnailUrl) && img1.url.equals(img2.url)
				&& img1.filename.equals(img2.filename);
	}
}
