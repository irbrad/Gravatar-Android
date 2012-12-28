package com.gravatar.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.gravatar.android.GravatarApplication;
import com.gravatar.android.R;
import com.gravatar.android.util.AlertUtil;
import com.gravatar.xmlrpc.GravatarService;

public class Login extends Activity implements View.OnClickListener {
	private ConnectivityManager mSystemService;
	private ProgressDialog mProgressDialog;
	private Button mLoginButton;
	private Button mSignUpButton;
	private EditText mEmailEdit;
	private EditText mPasswordEdit;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		mSystemService = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

		mLoginButton = (Button) findViewById(R.id.login);
		mSignUpButton = (Button) findViewById(R.id.gravatardotcom);
		mEmailEdit = (EditText) findViewById(R.id.email);
		mPasswordEdit = (EditText) findViewById(R.id.password);

		mLoginButton.setOnClickListener(this);
		mSignUpButton.setOnClickListener(this);
	}

	private void performLogin() {
		final String email = mEmailEdit.getText().toString().trim();
		final String password = mPasswordEdit.getText().toString().trim();

		GravatarApplication.GravatarService = new GravatarService(email, password);
		boolean loginSuccessful = GravatarApplication.GravatarService.verifyCredentials();
		mProgressDialog.dismiss();
		if (loginSuccessful) {
			startActivity(new Intent(getApplicationContext(), SelectEmail.class));
			this.finish();
		} else {
			AlertUtil.showAlert(Login.this, R.string.invalid_credentials, R.string.invalid_credentials_message);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.login:
				if (mSystemService.getActiveNetworkInfo() == null) {
					AlertUtil.showAlert(Login.this, R.string.no_network_title, R.string.no_network_message);
				} else {
					mProgressDialog = ProgressDialog.show(Login.this, getString(R.string.logging_in),
							getString(R.string.auth_and_sync), true, false);

					Thread action = new Thread() {
						public void run() {
							Looper.prepare();
							performLogin();
							Looper.loop();
						}
					};
					action.start();
				}
				break;
			case R.id.gravatardotcom:
				startActivity(new Intent(Login.this, SignUp.class));
				break;
		}
	}
}