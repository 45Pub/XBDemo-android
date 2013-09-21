package com.xbcx.im.ui;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.xbcx.core.XApplication;
import com.xbcx.im.XMessage;
import com.xbcx.utils.SystemUtils;

public class MessageThumbnailProvider {
	
	private static HashMap<String, SoftReference<Bitmap>> mMapIdToThumbPhoto = new HashMap<String, SoftReference<Bitmap>>();
		
	public static Bitmap loadThumbPhoto(XMessage m){
		SoftReference<Bitmap> sf = mMapIdToThumbPhoto.get(m.getId());
		Bitmap bmp = sf == null ? null : sf.get();
		if(bmp == null){
			if(m.getType() == XMessage.TYPE_PHOTO){
				final int nMaxWidth = SystemUtils.dipToPixel(XApplication.getApplication(), 100);
				if (m.isThumbPhotoFileExists()) {
					bmp = BitmapFactory.decodeFile(m.getThumbPhotoFilePath());
					if(bmp != null){
						if(bmp.getWidth() > nMaxWidth){
							final int height = bmp.getHeight() * nMaxWidth / bmp.getWidth();
							bmp = Bitmap.createScaledBitmap(bmp, nMaxWidth, height, true);
						}
					}
				} else {
					if (m.isFromSelf()) {
						BitmapFactory.Options op = new BitmapFactory.Options();
						op.inJustDecodeBounds = true;
						final String strPhotoFilePath = m.getPhotoFilePath();
						BitmapFactory.decodeFile(strPhotoFilePath, op);
						if (op.outWidth > nMaxWidth) {
							op.inJustDecodeBounds = false;
							op.inSampleSize = SystemUtils.nextPowerOf2(op.outWidth / nMaxWidth);
							if (op.inSampleSize == 1) {
								op.inSampleSize = 2;
							}
							try {
								bmp = BitmapFactory.decodeFile(strPhotoFilePath, op);
							} catch (OutOfMemoryError e) {
								e.printStackTrace();
								op.inSampleSize = op.inSampleSize * 2;
								bmp = BitmapFactory.decodeFile(strPhotoFilePath, op);
							}
						} else {
							bmp = BitmapFactory.decodeFile(strPhotoFilePath);
						}
					} else {
						bmp = BitmapFactory.decodeFile(m.getThumbPhotoFilePath());
					}
				}
				if(bmp != null){
					addToCache(m, bmp);
				}
			}else if(m.getType() == XMessage.TYPE_VIDEO){
				bmp = BitmapFactory.decodeFile(m.getVideoThumbFilePath());
				
				if(bmp != null){
					addToCache(m, bmp);
				}
			}else if(m.getType() == XMessage.TYPE_LOCATION){
				bmp = BitmapFactory.decodeFile(m.getLocationFilePath());
				if(bmp != null){
					addToCache(m, bmp);
				}
			}
		}
		
		return bmp;
	}
	
	private static void addToCache(XMessage m,Bitmap bmp){
		mMapIdToThumbPhoto.put(m.getId(), new SoftReference<Bitmap>(bmp));
	}
}
