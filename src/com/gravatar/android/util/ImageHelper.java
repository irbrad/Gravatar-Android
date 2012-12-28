package com.gravatar.android.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class ImageHelper {

	public String getExifOrientation(String path, String orientation) {
		// get image EXIF orientation if Android 2.0 or higher, using reflection
		// http://developer.android.com/resources/articles/backward-compatibility.html
		Method exif_getAttribute;
		Constructor<ExifInterface> exif_construct;
		String exifOrientation = "";

		int sdk_int = 0;
		try {
			sdk_int = Integer.valueOf(android.os.Build.VERSION.SDK);
		} catch (Exception e1) {
			sdk_int = 3; // assume they are on cupcake
		}
		if (sdk_int >= 5) {
			try {
				exif_construct = ExifInterface.class
						.getConstructor(new Class<?>[]{String.class});
				Object exif = exif_construct.newInstance(path);
				exif_getAttribute = ExifInterface.class
						.getMethod("getAttribute", new Class<?>[]{String.class});
				try {
					exifOrientation = (String) exif_getAttribute.invoke(exif,
							ExifInterface.TAG_ORIENTATION);
					if (exifOrientation != null) {
						if (exifOrientation.equals("1")) {
							orientation = "0";
						} else if (exifOrientation.equals("3")) {
							orientation = "180";
						} else if (exifOrientation.equals("6")) {
							orientation = "90";
						} else if (exifOrientation.equals("8")) {
							orientation = "270";
						}
					} else {
						orientation = "0";
					}
				} catch (InvocationTargetException ite) {
					/* unpack original exception when possible */
					orientation = "0";
				} catch (IllegalAccessException ie) {
					System.err.println("unexpected " + ie);
					orientation = "0";
				}
				/* success, this is a newer device */
			} catch (NoSuchMethodException nsme) {
				orientation = "0";
			} catch (IllegalArgumentException e) {
				orientation = "0";
			} catch (InstantiationException e) {
				orientation = "0";
			} catch (IllegalAccessException e) {
				orientation = "0";
			} catch (InvocationTargetException e) {
				orientation = "0";
			}

		}
		return orientation;
	}

	public HashMap<String, Object> getImageBytesForPath(String filePath, Context ctx) {
		Uri curStream = null;
		String[] projection;
		HashMap<String, Object> mediaData = new HashMap<String, Object>();
		String title = "", orientation = "";
		byte[] bytes;
		if (filePath != null) {
			if (!filePath.contains("content://"))
				curStream = Uri.parse("content://media" + filePath);
			else
				curStream = Uri.parse(filePath);
		}
		if (curStream != null) {
			if (filePath.contains("video")) {
				int videoID = Integer.parseInt(curStream.getLastPathSegment());
				projection = new String[]{Video.Thumbnails._ID,
						Video.Thumbnails.DATA};
				ContentResolver crThumb = ctx.getContentResolver();
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 1;
				Bitmap videoBitmap = Video.Thumbnails.getThumbnail(
						crThumb, videoID,
						Video.Thumbnails.MINI_KIND, options);

				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				try {
					videoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
					bytes = stream.toByteArray();
					title = "Video";
					videoBitmap = null;
				} catch (Exception e) {
					return null;
				}

			} else {
				projection = new String[]{Images.Thumbnails._ID,
						Images.Thumbnails.DATA, Images.Media.ORIENTATION};

				String path = "";
				Cursor cur = ctx.getContentResolver().query(curStream,
						projection, null, null, null);
				File jpeg = null;
				if (cur != null) {
					String thumbData = "";

					if (cur.moveToFirst()) {

						int dataColumn, orientationColumn;

						dataColumn = cur.getColumnIndex(Images.Media.DATA);
						thumbData = cur.getString(dataColumn);
						orientationColumn = cur
								.getColumnIndex(Images.Media.ORIENTATION);
						orientation = cur.getString(orientationColumn);
					}

					if (thumbData == null) {
						return null;
					}

					jpeg = new File(thumbData);
					path = thumbData;
				} else {
					path = filePath.toString().replace("file://", "");
					jpeg = new File(path);

				}

				title = jpeg.getName();

				try {
					bytes = new byte[(int) jpeg.length()];
				} catch (Exception e) {
					return null;
				} catch (OutOfMemoryError e) {
					return null;
				}

				DataInputStream in = null;
				try {
					in = new DataInputStream(new FileInputStream(jpeg));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return null;
				}
				try {
					in.readFully(bytes);
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}

				title = jpeg.getName();
				if (orientation == "") {
					orientation = getExifOrientation(path, orientation);
				}
			}

			mediaData.put("bytes", bytes);
			mediaData.put("title", title);
			mediaData.put("orientation", orientation);

			return mediaData;

		} else {
			return null;
		}
	}
}
