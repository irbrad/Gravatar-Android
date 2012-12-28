package com.gravatar.android.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.gravatar.android.util.ImageManager;
import com.gravatar.android.model.RatingImage;

import java.util.List;

public class RatingsAdapter extends ArrayAdapter<RatingImage> {
	private static final int IMAGE_SIZE = 150;
	public ImageManager mImageManager;
	private List<RatingImage> ratingImageList;
	private Activity activity;

	public RatingsAdapter(Activity a, int textViewResourceId, List<RatingImage> ratingImageList) {
		super(a, textViewResourceId, ratingImageList);
		this.ratingImageList = ratingImageList;
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
		final RatingImage image = ratingImageList.get(position);
		if (image != null) {
			imageView.setImageResource(image.resourceId);
			imageView.getLayoutParams().width = IMAGE_SIZE;
			imageView.getLayoutParams().height = IMAGE_SIZE;
		}
		return imageView;
	}
}