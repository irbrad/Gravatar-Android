package com.gravatar.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.gravatar.android.GravatarApplication;
import com.gravatar.android.R;
import com.gravatar.android.adapter.GravatarAddressAdapter;
import com.gravatar.android.util.ImageHelper;
import com.gravatar.xmlrpc.GravatarAddress;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SelectEmail extends Activity {
	private static final int ACTIVITY_REQUEST_CODE_PICTURE_LIBRARY = 0;
	private static final int ACTIVITY_REQUEST_CODE_TAKE_PHOTO = 1;
	private static final int ACTIVITY_REQUEST_CODE_EXISTING_GRAVATAR = 2;
	private static final int ACTIVITY_REQUEST_CODE_CROP = 3;
	private static final int ACTIVITY_REQUEST_CODE_RATING = 4;
	private ProgressDialog mProgressDialog;
	private List<GravatarAddress> mAddressList;
	private ListView mListView;
	private int mCurrentEmailSelected = -1; // used to temporarily store the select email
	private String mMediaCapturePath = "";  // temporary path to the last captured image
	private String mMediaCropPath = ""; // temporary path to the last cropped image

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selectemail);

		// start downloading the existing email addresses for this account
		AsyncTask<Void, Void, List<GravatarAddress>> task = new UpdateEmailAddressListTask().execute();

		mListView = (ListView) findViewById(R.id.emailListView);
		mListView.setOnItemClickListener(new

				                                 AdapterView.OnItemClickListener() {
					                                 public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
						                                 parent.showContextMenuForChild(v);
					                                 }
				                                 });
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		// save the current email selected
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		mCurrentEmailSelected = info.position;

		menu.add(0, 0, 0, getResources().getText(R.string.select_gravatar));
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			menu.add(0, 1, 0, getResources().getText(R.string.take_photo));
		}
		menu.add(0, 2, 0, getResources().getText(R.string.select_photo));
	}

	private void launchPictureLibrary() {
		if (!GravatarApplication.isSdCardMounted()) {
			showNoSdCardAlert();
		} else {
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			startActivityForResult(photoPickerIntent, ACTIVITY_REQUEST_CODE_PICTURE_LIBRARY);
		}
	}

	private void launchCamera() {
		if (!GravatarApplication.isSdCardMounted()) {
			showNoSdCardAlert();
		} else {
			mMediaCapturePath = Environment.getExternalStorageDirectory()
					+ File.separator + "gravatar" + File.separator + "g-"
					+ System.currentTimeMillis() + ".jpg";
			Intent takePictureFromCameraIntent = new Intent(
					MediaStore.ACTION_IMAGE_CAPTURE);
			takePictureFromCameraIntent.putExtra(
					android.provider.MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(mMediaCapturePath)));

			// make sure the directory we plan to store the recording in exists
			File directory = new File(mMediaCapturePath).getParentFile();
			if (!directory.exists() && !directory.mkdirs()) {
				try {
					throw new IOException("Path to file could not be created.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			startActivityForResult(takePictureFromCameraIntent,
					ACTIVITY_REQUEST_CODE_TAKE_PHOTO);
		}
	}

	private void showNoSdCardAlert() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
				SelectEmail.this);
		dialogBuilder.setTitle(getResources()
				.getText(R.string.sdcard_title));
		dialogBuilder.setMessage(getResources().getText(
				R.string.sdcard_message));
		dialogBuilder.setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
					                    int whichButton) {
						dialog.dismiss();
					}
				});
		dialogBuilder.setCancelable(true);
		dialogBuilder.create().show();
	}

	private void getEmailAddresses() {
		mAddressList = GravatarApplication.GravatarService.getAddresses();
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mListView.setAdapter(new GravatarAddressAdapter(SelectEmail.this, R.layout.listitem, mAddressList));
				registerForContextMenu(mListView);
			}
		});
		mProgressDialog.dismiss();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 0:
				Intent intent = new Intent(SelectEmail.this, SelectImage.class);
				startActivityForResult(intent, ACTIVITY_REQUEST_CODE_EXISTING_GRAVATAR);
				return true;
			case 1:
				launchCamera();
				return true;
			case 2:
				launchPictureLibrary();
				return true;
		}
		return false;
	}

	private Uri createTemporaryFileForCroppedImage() {
		mMediaCropPath = Environment.getExternalStorageDirectory()
				+ File.separator + "gravatar" + File.separator + "cropped.jpg";

		return Uri.fromFile(createFile(mMediaCropPath));
	}

	private File createFile(String fileName) {
		if (GravatarApplication.isSdCardMounted()) {
			File directory = new File(fileName).getParentFile();
			if (!directory.exists() && !directory.mkdirs()) {
				try {
					throw new IOException("Path to file could not be created.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			File file = new File(fileName);
			try {
				if (file.exists()) {
					file.delete();
				}

				file.createNewFile();

			} catch (IOException e) {
				Toast.makeText(this, getString(R.string.unable_to_create_crop), Toast.LENGTH_LONG).show();
			}
			return file;

		} else {
			Toast.makeText(this, getString(R.string.no_sd_card), Toast.LENGTH_LONG).show();
			return null;
		}
	}

	private void performCrop(Uri picUri) {
		try {
			//call the standard crop action intent (the user device may not support it)
			Intent cropIntent = new Intent("com.android.camera.action.CROP");
			cropIntent.setDataAndType(picUri, "image/*");
			cropIntent.putExtra("crop", "true");
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			cropIntent.putExtra("scale", false);
			cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, createTemporaryFileForCroppedImage());
			cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
			startActivityForResult(cropIntent, ACTIVITY_REQUEST_CODE_CROP);
		} catch (ActivityNotFoundException anfe) {
			Toast toast = Toast.makeText(this, getString(R.string.no_crop_on_device), Toast.LENGTH_LONG);
			toast.show();
		}
	}

	private byte[] getByteArrayFromCroppedImage() {
		byte[] bytes = null;
		try {
			File f = new File(mMediaCropPath);
			Uri capturedImage = Uri
					.parse(android.provider.MediaStore.Images.Media
							.insertImage(getContentResolver(),
									f.getAbsolutePath(), null, null));
			ImageHelper ih = new ImageHelper();
			HashMap<String, Object> mediaData = ih.getImageBytesForPath(capturedImage.toString(), SelectEmail.this);

			if (mediaData == null) {
				// data stream not returned
				return null;
			}

			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			bytes = (byte[]) mediaData.get("bytes");
		} catch (Exception ex) {
		}
		return bytes;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Bundle extras;
		if (resultCode == RESULT_CANCELED) {
			return;
		}

		if (data != null || (requestCode == ACTIVITY_REQUEST_CODE_TAKE_PHOTO)) {
			switch (requestCode) {
				case ACTIVITY_REQUEST_CODE_PICTURE_LIBRARY:
					performCrop(data.getData());
					break;
				case ACTIVITY_REQUEST_CODE_TAKE_PHOTO:
					if (resultCode == Activity.RESULT_OK) {
						try {
							File f = new File(mMediaCapturePath);
							Uri capturedImage = Uri
									.parse(android.provider.MediaStore.Images.Media
											.insertImage(getContentResolver(),
													f.getAbsolutePath(), null, null));
							f.delete();
							performCrop(capturedImage);
						} catch (Exception e) {
							Toast.makeText(getApplicationContext(), getString(R.string.photo_not_found), Toast.LENGTH_LONG).show();
						}
					}
					break;
				case ACTIVITY_REQUEST_CODE_EXISTING_GRAVATAR:
					extras = data.getExtras();
					if (extras != null) {
						final String userImageId = extras.getString("id");
						final String email = mAddressList.get(mCurrentEmailSelected).getEmail();
						AsyncTask<String, Void, Boolean> task = new SelectExistingImageTask().execute(userImageId, email);
					}
					break;
				case ACTIVITY_REQUEST_CODE_CROP:
					startActivityForResult(new Intent(SelectEmail.this,
							SelectRating.class), ACTIVITY_REQUEST_CODE_RATING);
					break;
				case ACTIVITY_REQUEST_CODE_RATING:
					AsyncTask<Void, Void, Boolean> task = new UploadImageTask().execute();
					break;
			}
		}
	}

	/**
	 * save the orientation when the camera is brought up
	 */
	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences prefs = this.getSharedPreferences("com.gravatar.android", Context.MODE_PRIVATE);
		int o = getResources().getConfiguration().orientation;
		prefs.edit().putInt("orientation", o).commit();
	}

	/**
	 * restore the orientation after the camera is dismissed
	 */
	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences prefs = this.getSharedPreferences("com.gravatar.android", Context.MODE_PRIVATE);
		int orientation = prefs.getInt("orientation", -1);
		if (orientation > -1) {
			this.setRequestedOrientation(orientation);
		}
	}

	private class UploadImageTask extends AsyncTask<Void, Void, Boolean> {
		protected Boolean doInBackground(Void... args) {
			try {
				byte[] bytes = getByteArrayFromCroppedImage();
				if (bytes == null) {
					return Boolean.valueOf(false);
				}

				// upload the image
				String encodedString = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
				String userImageId = GravatarApplication.GravatarService.saveImage(encodedString, 0);

				// select the newly uploaded image
				String email = mAddressList.get(mCurrentEmailSelected).getEmail();
				ArrayList<String> emails = new ArrayList<String>();
				emails.add(email);
				GravatarApplication.GravatarService.useUserImage(userImageId, emails);

				// update the list of addresses
				mAddressList = GravatarApplication.GravatarService.getAddresses();
				return Boolean.valueOf(true);
			} catch (Exception e) {
				return Boolean.valueOf(false);
			}
		}

		protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(SelectEmail.this, getString(R.string.sending_changes), getString(R.string.please_wait_while),
					true, false);
		}

		protected void onPostExecute(Boolean updateSuccess) {
			if (updateSuccess) {
				GravatarAddressAdapter a = (GravatarAddressAdapter) mListView.getAdapter();
				a.setGravatarAddressList(mAddressList);
				a.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), getString(R.string.updated_gravatar), Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.failed_updating_gravatar), Toast.LENGTH_LONG).show();
			}
			mProgressDialog.dismiss();
		}
	}

	private class SelectExistingImageTask extends AsyncTask<String, Void, Boolean> {
		protected Boolean doInBackground(String... args) {
			try {
				String userImageId = args[0];
				String email = args[1];

				// set the user image
				ArrayList<String> emails = new ArrayList<String>();
				emails.add(email);
				GravatarApplication.GravatarService.useUserImage(userImageId, emails);
				mAddressList = GravatarApplication.GravatarService.getAddresses();

				return Boolean.valueOf(true);
			} catch (Exception e) {
				return Boolean.valueOf(false);
			}
		}

		protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(SelectEmail.this, getString(R.string.sending_changes), getString(R.string.please_wait_while),
					true, false);
		}

		protected void onPostExecute(Boolean updateSuccess) {
			if (updateSuccess) {
				GravatarAddressAdapter a = (GravatarAddressAdapter) mListView.getAdapter();
				a.setGravatarAddressList(mAddressList);
				a.notifyDataSetChanged();
				mProgressDialog.dismiss();
				Toast.makeText(getApplicationContext(), getString(R.string.updated_gravatar), Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.failed_updating_gravatar), Toast.LENGTH_LONG).show();
			}
			mProgressDialog.dismiss();
		}
	}

	private class UpdateEmailAddressListTask extends AsyncTask<Void, Void, List<GravatarAddress>> {
		protected List<GravatarAddress> doInBackground(Void... args) {
			return GravatarApplication.GravatarService.getAddresses();
		}

		protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(SelectEmail.this, getString(R.string.downloading),
					getString(R.string.syncing_account_message), true, false);
		}

		protected void onPostExecute(List<GravatarAddress> addressList) {
			mAddressList = addressList;
			mListView.setAdapter(new GravatarAddressAdapter(SelectEmail.this, R.layout.listitem, mAddressList));
			registerForContextMenu(mListView);
			mProgressDialog.dismiss();
		}
	}
}

