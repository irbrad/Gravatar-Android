package com.gravatar.android.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.gravatar.android.util.ImageManager;
import com.gravatar.android.R;
import com.gravatar.xmlrpc.GravatarAddress;

import java.util.List;

public class GravatarAddressAdapter extends ArrayAdapter<GravatarAddress> {
	private static final int IMAGE_SIZE = 150;
	private ImageManager mImageManager;
	private List<GravatarAddress> mGravatarAddressList;
	private Activity mActivity;

	public GravatarAddressAdapter(Activity a, int textViewResourceId, List<GravatarAddress> gravatarAddressList) {
		super(a, textViewResourceId, gravatarAddressList);

		this.mGravatarAddressList = gravatarAddressList;
		mActivity = a;
		mImageManager = new ImageManager(mActivity.getApplicationContext());
	}

	public void setGravatarAddressList(List<GravatarAddress> mGravatarAddressList) {
		this.mGravatarAddressList = mGravatarAddressList;
	}

	@Override
	public View getView(int position, View v, ViewGroup parent) {
		ViewHolder holder;
		if (v == null) {
			LayoutInflater vi =
					(LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.listitem, null);
			holder = new ViewHolder();
			holder.message = (TextView) v.findViewById(R.id.message);
			holder.image = (ImageView) v.findViewById(R.id.gravatar);
			v.setTag(holder);
		} else
			holder = (ViewHolder) v.getTag();

		final GravatarAddress gravatar = mGravatarAddressList.get(position);
		if (gravatar != null) {
			holder.message.setText(gravatar.getEmail());
			boolean validUrl = gravatar.getUserImageId() != null && !gravatar.getUserImageId().isEmpty();
			holder.image.setTag(gravatar.getUrl(IMAGE_SIZE));
			mImageManager.displayImage(gravatar.getUrl(IMAGE_SIZE), holder.image, R.drawable.default_gravatar, validUrl);
			holder.image.getLayoutParams().width = IMAGE_SIZE;
			holder.image.getLayoutParams().height = IMAGE_SIZE;
		}
		return v;
	}

	public static class ViewHolder {
		public TextView message;
		public ImageView image;
	}
}