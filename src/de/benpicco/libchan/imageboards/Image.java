package de.benpicco.libchan.imageboards;

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

			if (thumbnailUrl == null)
				thumbnailUrl = url;
		}
	}
}
