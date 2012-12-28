package com.gravatar.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import com.gravatar.android.GravatarApplication;
import com.gravatar.android.R;
import com.gravatar.android.adapter.GravatarUserImageAdapter;
import com.gravatar.xmlrpc.GravatarUserImage;

import java.util.List;

public class SelectImage extends Activity {
	private ProgressDialog mProgressDialog;
	private GridView mGridview;
	private List<GravatarUserImage> mUserImages;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selectimage);

		mGridview = (GridView) findViewById(R.id.selectimage_gridview);

		AsyncTask<Void, Void, List<GravatarUserImage>> task = new UpdateUserImagesTask().execute();
	}

	private class UpdateUserImagesTask extends AsyncTask<Void, Void, List<GravatarUserImage>> {
		protected List<GravatarUserImage> doInBackground(Void... args) {
			return GravatarApplication.GravatarService.getUserImages();
		}

		protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(SelectImage.this, getString(R.string.downloading),
					getString(R.string.syncing_images_message), true, false);
		}

		protected void onPostExecute(List<GravatarUserImage> userImages) {
			mUserImages = userImages;
			mGridview.setAdapter(new GravatarUserImageAdapter(SelectImage.this, 0, mUserImages));

			mGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					Intent i = new Intent(SelectImage.this, SelectEmail.class);
					i.putExtra("id", mUserImages.get(position).getId());
					setResult(RESULT_OK, i);
					finish();
				}
			});
			mProgressDialog.dismiss();
		}
	}
}