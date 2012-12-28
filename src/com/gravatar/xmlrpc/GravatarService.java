package com.gravatar.xmlrpc;

import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

import java.net.URL;
import java.security.MessageDigest;
import java.util.*;

/**
 * User: jeffrey
 * Date: 12/19/12
 * Time: 7:38 PM
 * <p/>
 * Implements some of the methods from https://en.gravatar.com/site/implement/xmlrpc/
 */
public class GravatarService {

	private static final String USER_AGENT = "gravatar-android";
	private static final String GRAVATAR_URL = "https://secure.gravatar.com/xmlrpc?user=";
	private String email;
	private String password;

	public GravatarService(String email, String password) {
		this.email = email;
		this.password = password;
	}

	private Object callService(String command, Map<String, Object> arguments) {
		try {
			URL url = new URL(GRAVATAR_URL + getEmailHash(email));
			XMLRPCClient client = new XMLRPCClient(url, USER_AGENT);
			return client.call(command, arguments);
		} catch (XMLRPCServerException ex) {
			// The server throw an error.
		} catch (XMLRPCException ex) {
			// An error occurred in the client.
		} catch (Exception ex) {
			// Any other exception
		}
		return null;
	}

	public String getEmailHash(String email) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(email.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (Exception ex) {
			return "";
		}
	}

	public boolean verifyCredentials() {
		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("password", password);
		Object result = callService("grav.test", arguments);
		return result != null
				&& ((Map) result).get("response") != null;
	}

	public boolean deleteImage(String userImageId) {
		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("password", password);
		arguments.put("userimage", userImageId);
		Object result = callService("grav.deleteUserimage", arguments);

		return (result != null && (Boolean) result);
	}

	public String saveImage(String image, int rating) {
		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("password", password);
		arguments.put("data", image);
		arguments.put("rating", rating);
		Object result = callService("grav.saveData", arguments);

		if (result != null) {
			return (String) result;
		}
		return "";
	}

	public boolean useUserImage(String userimage, List<String> addresses) {
		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("password", password);
		arguments.put("userimage", userimage);
		arguments.put("addresses", addresses.toArray());
		Object result = callService("grav.useUserimage", arguments);

		return (result != null);
	}

	public List<GravatarUserImage> getUserImages() {
		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("password", password);
		@SuppressWarnings(value = "unchecked")
		Map<String, Object[]> result = (Map) callService("grav.userimages ", arguments);

		List<GravatarUserImage> addressList = new ArrayList<GravatarUserImage>();
		for (Map.Entry<String, Object[]> entry : result.entrySet()) {
			String id = entry.getKey();
			Object[] addressData = entry.getValue();
			int rating = Integer.parseInt((String) addressData[0]);
			String url = (String) addressData[1];
			GravatarUserImage ga = new GravatarUserImage(id, rating, url);

			addressList.add(ga);
		}

		return addressList;
	}

	public List<GravatarAddress> getAddresses() {
		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("password", password);
		@SuppressWarnings(value = "unchecked")
		Map<String, Map<String, Object>> result = (Map) callService("grav.addresses", arguments);

		List<GravatarAddress> addressList = new ArrayList<GravatarAddress>();
		for (Map.Entry<String, Map<String, Object>> entry : result.entrySet()) {
			String address = entry.getKey().toLowerCase();
			Map<String, Object> addressData = entry.getValue();
			int rating = (Integer) addressData.get("rating");
			String url = (String) addressData.get("userimage_url");
			String userImageId = (String) addressData.get("userimage");
			GravatarAddress ga = new GravatarAddress(address, rating, url, userImageId);

			addressList.add(ga);
		}

		Collections.sort(addressList, new Comparator<GravatarAddress>() {
			public int compare(GravatarAddress ga1, GravatarAddress ga2) {
				return ga1.getEmail().compareTo(ga2.getEmail());
			}
		});
		return addressList;
	}

	public Map<String, Boolean> checkGravatarExists() {
		return checkGravatarExists(new ArrayList<String>());
	}

	public Map<String, Boolean> checkGravatarExists(List<String> emailList) {
		Map<String, Object> arguments = new HashMap<String, Object>();
		List<String> hashList = new ArrayList<String>();
		for (String e : emailList) {
			hashList.add(getEmailHash(e));
		}
		String emailHash = getEmailHash(email);
		if (!hashList.contains(emailHash)) {
			hashList.add(emailHash);
		}
		arguments.put("hashes", hashList.toArray());
		arguments.put("password", password);
		@SuppressWarnings(value = "unchecked")
		Map<String, Integer> result = (Map<String, Integer>) callService("grav.exists", arguments);

		Map<String, Boolean> correctedResult = new HashMap<String, Boolean>();
		for (Map.Entry<String, Integer> entry : result.entrySet()) {
			String hash = entry.getKey();
			Integer hasGravatar = entry.getValue();
			correctedResult.put(hash, (hasGravatar > 0));
		}

		return correctedResult;
	}
}
