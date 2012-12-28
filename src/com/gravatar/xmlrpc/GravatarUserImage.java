package com.gravatar.xmlrpc;

public class GravatarUserImage {
	private String id;
	private int rating;
	private String url;

	public GravatarUserImage(String id, int rating, String url) {
		this.id = id;
		this.rating = rating;
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public String getUrl(int size) {
		return url + "?size=" + size;
	}

	public int getRating() {
		return rating;
	}

	public String getId() {
		return id;
	}
}
