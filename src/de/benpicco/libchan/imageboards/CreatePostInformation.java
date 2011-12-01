package de.benpicco.libchan.imageboards;

public class CreatePostInformation implements Cloneable {
	protected int		maxFiles	= 1;

	protected String	postUrl;
	protected String	deleteUrl;

	protected String	nameParam;
	protected String	mailParam;
	protected String	titleParam;
	protected String	messageParam;
	protected String	passwordParam;

	protected String	fileParam;

	protected String	deleteParam;
	protected String	deleteParamVal;

	protected String	boardParam;
	protected String	replyToParam;

	protected String	paramCaptchaId;
	protected String	paramCaptchaSolution;

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
		cpi.paramCaptchaId = paramCaptchaId;
		cpi.paramCaptchaSolution = paramCaptchaSolution;
		cpi.passwordParam = passwordParam;
		cpi.deleteUrl = deleteUrl;
		cpi.deleteParam = deleteParam;
		cpi.deleteParamVal = deleteParamVal;
		return cpi;
	}
}
