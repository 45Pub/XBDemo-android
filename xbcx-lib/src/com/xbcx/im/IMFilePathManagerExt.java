package com.xbcx.im;


public class IMFilePathManagerExt extends IMFilePathManager {
	@Override
	public String getMessagePhotoFilePath(XMessage m) {
		return super.getMessagePhotoFilePath(m) + ".jpg";
	}

	@Override
	public String getMessagePhotoThumbFilePath(XMessage m) {
		return super.getMessagePhotoFilePath(m) + "thumb.jpg";
	}

	@Override
	public String getMessageVideoFilePath(XMessage m) {
		return super.getMessageVideoFilePath(m) + ".mp4";
	}

	@Override
	public String getMessageVideoThumbFilePath(XMessage m) {
		return super.getMessageVideoThumbFilePath(m) + ".jpg";
	}
}
