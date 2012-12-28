package com.gravatar.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import com.gravatar.android.R;
import com.gravatar.android.model.RatingImage;
import com.gravatar.android.adapter.RatingsAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectRating extends Activity {
	private GridView mGridview;
	private List<RatingImage> mRatingImages;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selectrating);

		mGridview = (GridView) findViewById(R.id.selectrating_gridview);

		mRatingImages = getRatingImages();
		mGridview.setAdapter(new RatingsAdapter(SelectRating.this, 0, mRatingImages));

		mGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

				Intent i = new Intent(SelectRating.this, SelectEmail.class);
				i.putExtra("rating", mRatingImages.get(position).rating);
				setResult(RESULT_OK, i);
				finish();
			}
		});
	}

	private List<RatingImage> getRatingImages() {
		RatingImage rating0 = new RatingImage(R.drawable.rating0, 0);
		RatingImage rating1 = new RatingImage(R.drawable.rating1, 0);
		RatingImage rating2 = new RatingImage(R.drawable.rating2, 0);
		RatingImage rating3 = new RatingImage(R.drawable.rating3, 0);
		ArrayList<RatingImage> ratingImages = new ArrayList<RatingImage>();
		ratingImages.add(rating0);
		ratingImages.add(rating1);
		ratingImages.add(rating2);
		ratingImages.add(rating3);
		return ratingImages;
	}
}