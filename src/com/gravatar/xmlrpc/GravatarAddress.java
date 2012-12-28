package com.gravatar.xmlrpc;

public class GravatarAddress {
	private String email;
	private int rating;
	private String url;
	private String userImageId;

	public GravatarAddress(String email, int rating, String url, String userImageId) {
		this.email = email;
		this.rating = rating;
		this.url = url;
		this.userImageId = userImageId;
	}

	public String getEmail() {
		return email;
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

	public String getUserImageId() {
		return userImageId;
	}
}
