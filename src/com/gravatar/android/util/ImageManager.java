package com.gravatar.android.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import com.gravatar.android.GravatarApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Stack;

/**
 * code from:
 * http://codehenge.net/blog/2011/06/android-development-tutorial-asynchronous-lazy-loading-and-caching-of-listview-images/
 */
public class ImageManager {

	private HashMap<String, Bitmap> imageMap = new HashMap<String, Bitmap>();
	private File cacheDir;
	private ImageQueue imageQueue = new ImageQueue();
	private Thread imageLoaderThread = new Thread(new ImageQueueManager());

	public ImageManager(Context context) {
		// Make background thread low priority, to avoid affecting UI performance
		imageLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);

		// Find the dir to save cached images
		if (GravatarApplication.isSdCardMounted()) {
			File sdDir = android.os.Environment.getExternalStorageDirectory();
			cacheDir = new File(sdDir, "gravatar");

		} else {
			cacheDir = context.getCacheDir();
		}

		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
	}

	public void displayImage(String url, ImageView imageView, int defaultDrawableId, boolean validUrl) {
		if (imageMap.containsKey(url)) {
			imageView.setImageBitmap(imageMap.get(url));
		} else {
			if (validUrl) {
				queueImage(url, imageView, defaultDrawableId);
			}
			imageView.setImageResource(defaultDrawableId);
		}
	}

	private void queueImage(String url, ImageView imageView, int defaultDrawableId) {
		// This ImageView might have been used for other images, so we clear 
		// the queue of old tasks before starting.
		imageQueue.Clean(imageView);
		ImageRef p = new ImageRef(url, imageView, defaultDrawableId);

		synchronized (imageQueue.imageRefs) {
			imageQueue.imageRefs.push(p);
			imageQueue.imageRefs.notifyAll();
		}

		// Start thread if it's not started yet
		if (imageLoaderThread.getState() == Thread.State.NEW) {
			imageLoaderThread.start();
		}
	}

	private Bitmap getBitmap(String url) {
		try {
			URLConnection openConnection = new URL(url).openConnection();

			String filename = String.valueOf(url.hashCode());

			File bitmapFile = new File(cacheDir, filename);
			Bitmap bitmap = BitmapFactory.decodeFile(bitmapFile.getPath());

			// check if the image is in the cache
			if (bitmap != null) {
				return bitmap;
			}

			bitmap = BitmapFactory.decodeStream(openConnection.getInputStream());
			writeFile(bitmap, bitmapFile);

			return bitmap;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private void writeFile(Bitmap bmp, File f) {
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(f);
			bmp.compress(Bitmap.CompressFormat.PNG, 80, out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) out.close();
			} catch (Exception ex) {
			}
		}
	}

	private class ImageRef {
		public String url;
		public ImageView imageView;
		public int defDrawableId;

		public ImageRef(String u, ImageView i, int defaultDrawableId) {
			url = u;
			imageView = i;
			defDrawableId = defaultDrawableId;
		}
	}

	//stores list of images to download
	private class ImageQueue {
		private Stack<ImageRef> imageRefs =
				new Stack<ImageRef>();

		//removes all instances of this ImageView
		public void Clean(ImageView view) {

			for (int i = 0; i < imageRefs.size(); ) {
				if (imageRefs.get(i).imageView == view)
					imageRefs.remove(i);
				else ++i;
			}
		}
	}

	private class ImageQueueManager implements Runnable {
		@Override
		public void run() {
			try {
				while (true) {
					// Thread waits until there are images in the 
					// queue to be retrieved
					if (imageQueue.imageRefs.size() == 0) {
						synchronized (imageQueue.imageRefs) {
							imageQueue.imageRefs.wait();
						}
					}

					// When we have images to be loaded
					if (imageQueue.imageRefs.size() != 0) {
						ImageRef imageToLoad;

						synchronized (imageQueue.imageRefs) {
							imageToLoad = imageQueue.imageRefs.pop();
						}

						Bitmap bmp = getBitmap(imageToLoad.url);
						imageMap.put(imageToLoad.url, bmp);
						Object tag = imageToLoad.imageView.getTag();

						// Make sure we have the right view - thread safety defender
						if (tag != null && ((String) tag).equals(imageToLoad.url)) {
							BitmapDisplayer bmpDisplayer =
									new BitmapDisplayer(bmp, imageToLoad.imageView, imageToLoad.defDrawableId);

							Activity a =
									(Activity) imageToLoad.imageView.getContext();

							a.runOnUiThread(bmpDisplayer);
						}
					}

					if (Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {
			}
		}
	}

	//Used to display bitmap in the UI thread
	private class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		ImageView imageView;
		int defDrawableId;

		public BitmapDisplayer(Bitmap b, ImageView i, int defaultDrawableId) {
			bitmap = b;
			imageView = i;
			defDrawableId = defaultDrawableId;
		}

		public void run() {
			if (bitmap != null)
				imageView.setImageBitmap(bitmap);
			else
				imageView.setImageResource(defDrawableId);
		}
	}
}