package de.benpicco.libchan;

/**
 * This class is used to hold the url of an image as the url for it's thumbnail.
 */
public class Image {
	public final String	thumbnailUrl;
	public final String	url;
	public final String	filename;

	public Image(String thumbnail, String imgUrl, String filename) {
		this.thumbnailUrl = thumbnail;
		this.url = imgUrl;
		this.filename = filename;
	}

	public String toString() {
		return filename + " (" + thumbnailUrl + ", " + url + ")\n";
	}
}
