package com.gravatar.android;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import com.gravatar.xmlrpc.GravatarService;

public class GravatarApplication extends Application {

	public static String versionName;
	public static GravatarService GravatarService;

	public static boolean isSdCardMounted() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	@Override
	public void onCreate() {
		versionName = getVersionName();
		super.onCreate();
	}

	/**
	 * Get versionName from Manifest.xml
	 *
	 * @return versionName
	 */
	private String getVersionName() {
		PackageManager pm = getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
			return pi.versionName;
		} catch (NameNotFoundException e) {
			return "";
		}
	}
}
