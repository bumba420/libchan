package de.benpicco.libchan.streamparser;

import de.benpicco.libchan.imageboards.Tags;

public interface IParseDataReceiver {
	public void parsedString(Tags tag, String data);
}
