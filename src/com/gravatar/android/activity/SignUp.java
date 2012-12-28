package com.gravatar.android.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;
import android.webkit.*;
import com.gravatar.android.GravatarApplication;
import com.gravatar.android.R;

public class SignUp extends Activity {
	public Activity activity = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		WebView webview = new WebView(this);
		setContentView(webview);
		setTitle(getResources().getText(R.string.new_account));
		setProgressBarIndeterminateVisibility(true);
		CookieSyncManager.createInstance(this);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
		webview.getSettings().setUserAgentString("gravatar-android/" + GravatarApplication.versionName);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		webview.setWebViewClient(new WebViewClient());
		webview.loadUrl("https://gravatar.com");
	}
}
