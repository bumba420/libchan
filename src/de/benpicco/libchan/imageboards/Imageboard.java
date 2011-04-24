package de.benpicco.libchan.imageboards;

public class Imageboard {
	public String	name;
	public String	baseurl;
	public String	description;
	public String	icon;

	public String toString() {
		return name + " - " + description + "\n\t" + baseurl;
	}
}
