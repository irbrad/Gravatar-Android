package com.gravatar.android.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.gravatar.android.util.ImageManager;
import com.gravatar.android.R;
import com.gravatar.xmlrpc.GravatarUserImage;

import java.util.List;

public class GravatarUserImageAdapter extends ArrayAdapter<GravatarUserImage> {
	private static final int IMAGE_SIZE = 150;
	public ImageManager mImageManager;
	private List<GravatarUserImage> mGravatarUserImageList;
	private Activity activity;

	public GravatarUserImageAdapter(Activity a, int textViewResourceId, List<GravatarUserImage> gravatarUserImageList) {
		super(a, textViewResourceId, gravatarUserImageList);
		this.mGravatarUserImageList = gravatarUserImageList;
		activity = a;

		mImageManager = new ImageManager(activity.getApplicationContext());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {
			imageView = new ImageView(activity);
			imageView.setLayoutParams(new GridView.LayoutParams(IMAGE_SIZE, IMAGE_SIZE));
		} else {
			imageView = (ImageView) convertView;
		}
		final GravatarUserImage image = mGravatarUserImageList.get(position);
		if (image != null) {
			if (!image.getUrl().isEmpty()) {
				imageView.setTag(image.getUrl(IMAGE_SIZE));
				mImageManager.displayImage(image.getUrl(IMAGE_SIZE), imageView, R.drawable.default_gravatar, true);
				imageView.getLayoutParams().width = IMAGE_SIZE;
				imageView.getLayoutParams().height = IMAGE_SIZE;
			}
		}
		return imageView;
	}
}