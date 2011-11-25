var oldHighlight;

function getStyle(id, attribute) {
	return document.defaultView.getComputedStyle(document.getElementById(id), null).getPropertyValue(attribute)
}

function replyhl(id) {
	document.getElementById(id).style.background = getStyle("posthl", "background-color");
	if (oldHighlight && oldHighlight != id)
		document.getElementById(oldHighlight).style.background = getStyle("post", "background-color");
	oldHighlight=id;
}

