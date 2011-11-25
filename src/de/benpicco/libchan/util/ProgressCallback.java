package de.benpicco.libchan.util;

public interface ProgressCallback {
	public void setFileSize(long size);

	public void written(long bytes);
}
