package de.benpicco.libchan.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import de.benpicco.libchan.clichan.GlobalOptions;
import de.benpicco.libchan.clichan.ThreadArchiver;

/**
 * This class helps to send POST HTTP requests with various form data, including
 * files.
 */
public class ClientHttpRequest {
	private final OutputStream	os;

	private void write(String s) throws IOException {
		os.write(s.getBytes());
	}

	// yay, pointless free text
	final static String	boundary	= "--" + ThreadArchiver.VERSION + "FormBoundary";

	private void boundary() throws IOException {
		write("--");
		write(boundary);
	}

	/**
	 * Creates a new multipart POST HTTP request on a freshly opened
	 * URLConnection
	 * 
	 * @param connection
	 *            an already open URL connection
	 * @throws IOException
	 */
	public ClientHttpRequest(HttpURLConnection connection) throws IOException {
		connection.setRequestMethod("POST");
		connection.setRequestProperty("User-Agent", GlobalOptions.useragent);
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		os = connection.getOutputStream();
	}

	/**
	 * adds a string parameter to the request
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws IOException
	 */
	public void setParameter(String name, String value) throws IOException {
		if (value == null)
			value = "";

		Logger.get().println(name + "=\"" + value + "\"");

		boundary();
		write("\r\n");
		write("Content-Disposition: form-data; ");
		write("name=\"" + name + "\";");

		write("\r\n\r\n");

		write(value);

		write("\r\n");
	}

	/**
	 * adds a file parameter to the request
	 * 
	 * @param name
	 *            parameter name
	 * @param file
	 *            the file to upload
	 * @param callback
	 *            a callback routine that is called for each chunk of the file
	 *            that is uploaded, makes it possible to draw nice progress bars
	 * @throws IOException
	 */
	public void setParameter(String name, File file, ProgressCallback callback) throws IOException {
		if (file == null)
			return;

		Logger.get().println(name + "=\"" + file.getAbsolutePath() + "\"");

		boundary();
		write("\r\n");
		write("Content-Disposition: form-data; ");
		write("name=\"" + name + "\"; ");

		write("filename=\"" + file.getName() + "\"\r\n");
		write("Content-Type: " + FileUtil.getMimeType(file));

		write("\r\n\r\n");

		if (callback != null)
			callback.setFileSize(file.length());

		Logger.get().println(
				"Uploading " + file.getName() + " (" + FileUtil.getMimeType(file) + ", " + file.length() + " bytes)");
		Logger.get().println("wrote " + FileUtil.pipe(new FileInputStream(file), os, callback) + " bytes");

		write("\r\n");
	}

	/**
	 * finishes the requests to the server, call it after all the files and
	 * parameters were added.
	 * 
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public void post() throws IOException {
		boundary();
		write("--\r\n");
		os.close();
	}
}
