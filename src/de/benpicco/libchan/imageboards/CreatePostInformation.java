package de.benpicco.libchan.imageboards;

public class CreatePostInformation implements Cloneable {
	protected int		maxFiles	= 1;

	protected String	postUrl;

	protected String	nameParam;
	protected String	mailParam;
	protected String	titleParam;
	protected String	messageParam;

	protected String	fileParam;

	protected String	boardParam;
	protected String	replyToParam;

	public CreatePostInformation clone() {
		CreatePostInformation cpi = new CreatePostInformation();
		cpi.maxFiles = maxFiles;
		cpi.postUrl = postUrl;
		cpi.nameParam = nameParam;
		cpi.mailParam = mailParam;
		cpi.titleParam = titleParam;
		cpi.messageParam = messageParam;
		cpi.fileParam = fileParam;
		cpi.boardParam = boardParam;
		cpi.replyToParam = replyToParam;
		return cpi;
	}
}
