package de.benpicco.libchan.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import de.benpicco.libchan.clichan.GlobalOptions;
import de.benpicco.libchan.clichan.ThreadArchiver;

/**
 * This class helps to send POST HTTP requests with various form data, including
 * files.
 */
public class ClientHttpRequest {
	private final OutputStream		os;
	private final HttpURLConnection	httpConnection;
	private final boolean			urlencode;

	private boolean					firstparam	= true;

	private void write(String s) throws IOException {
		os.write(s.getBytes());
	}

	// yay, pointless free text
	final static String	boundary	= "--" + ClientHttpRequest.class.getName() + ThreadArchiver.VERSION
											+ "FormBoundary";

	private void boundary() throws IOException {
		write("--");
		write(boundary);
	}

	private HttpURLConnection openConnection(String url, boolean urlencode) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("User-Agent", GlobalOptions.useragent);
		connection.setDoOutput(true);
		if (urlencode)
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		else
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

		return connection;
	}

	/**
	 * Creates a new POST HTTP request on a freshly for the url
	 * 
	 * @param url
	 *            The Request url
	 * @param urlencode
	 *            use either application/x-www-form-urlencoded or
	 *            multipart/form-data (recommended for file transfers)
	 * @throws IOException
	 */
	public ClientHttpRequest(String url, boolean urlencode) throws IOException {
		httpConnection = openConnection(url, urlencode);
		os = httpConnection.getOutputStream();
		this.urlencode = urlencode;
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

		// application/x-www-form-urlencoded
		if (urlencode) {
			write(firstparam ? "" : "&");
			firstparam = false;
			write(URLEncoder.encode(name, "UTF-8"));
			write("=");
			write(URLEncoder.encode(value, "UTF-8"));
			return;
		}

		// multipart/form-data

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

		// application/x-www-form-urlencoded
		if (urlencode) {
			write(firstparam ? "?" : "&");
			firstparam = false;
			write(URLEncoder.encode(name, "UTF-8"));
			write(URLEncoder.encode("=", "UTF-8"));
			write(URLEncoder.encode(FileUtil.getFileContent(file), "UTF-8"));
			return;
		}

		// multipart/form-data

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
	public InputStream post() throws IOException {
		if (!urlencode) {
			boundary();
			write("--\r\n");
		}
		os.close();

		return httpConnection.getInputStream();
	}
}
