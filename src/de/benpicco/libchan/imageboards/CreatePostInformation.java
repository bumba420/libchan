package de.benpicco.libchan.imageboards;

import java.util.ArrayList;
import java.util.List;

public class CreatePostInformation implements Cloneable {
	protected int			maxFiles			= 1;

	protected String		postUrl;
	protected String		deleteUrl;

	protected List<String>	postParameter		= new ArrayList<String>();
	protected List<String>	postFileParameter	= new ArrayList<String>();
	protected List<String>	delParameter		= new ArrayList<String>();

	public CreatePostInformation clone() {
		CreatePostInformation cpi = new CreatePostInformation();
		cpi.maxFiles = maxFiles;
		cpi.postUrl = postUrl;
		cpi.deleteUrl = deleteUrl;

		cpi.postParameter = new ArrayList<String>(postParameter);
		cpi.delParameter = new ArrayList<String>(delParameter);
		cpi.postFileParameter = new ArrayList<String>(postFileParameter);

		return cpi;
	}
}
