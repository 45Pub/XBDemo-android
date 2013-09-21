package com.xbcx.im;

import java.io.File;

import com.xbcx.utils.SystemUtils;

import android.content.Context;
import android.text.TextUtils;

public class IMFilePathManager extends IMModule{
	
	public static IMFilePathManager getInstance(){
		return sInstance;
	}
	
	private static IMFilePathManager sInstance;
	
	protected Context		mContext;
	
	protected String 		mFilePathPrefix;
	
	protected IMFilePathManager(){
		sInstance = this;
	}
	
	@Override
	protected void onInitial(IMKernel kernel) {
		mContext = kernel.getContext();
		mFilePathPrefix = SystemUtils.getExternalCachePath(mContext) + 
				File.separator + "users" + File.separator + kernel.getUserId();
	}

	@Override
	protected void onRelease() {
	}
	
	public String getAvatarSavePath(String imUser){
		return SystemUtils.getExternalCachePath(mContext) + 
				File.separator + "avatar" + File.separator + imUser;
	}
	
	public String getMessagePhotoFilePath(XMessage m){
		if(m.getType() == XMessage.TYPE_PHOTO){
			return getMessageFolderPath(m.getOtherSideId()) + File.separator +
					"photo" + File.separator + m.getId();
		}else{
			throw new IllegalArgumentException("photoPath juse use type = photo");
		}
	}
	
	public String getMessagePhotoThumbFilePath(XMessage m){
		String strPath = getMessagePhotoFilePath(m);
		return strPath + "thumb";
	}
	
	public String getMessageVoiceFilePath(XMessage m){
		if(m.getType() == XMessage.TYPE_VOICE){
			return getMessageFolderPath(m.getOtherSideId()) + File.separator +
					"voice" + File.separator + m.getId() + ".amr";
		}else{
			throw new IllegalArgumentException("voiceFilePath just use type = Voice");
		}
	}
	
	public String getMessageVideoFilePath(XMessage m){
		if(m.getType() == XMessage.TYPE_VIDEO){
			return getMessageFolderPath(m.getOtherSideId()) + File.separator +
					"video" + File.separator + m.getId();
		}else{
			throw new IllegalArgumentException("videoFilePath just use type = Video");
		}
	}
	
	public String getMessageVideoThumbFilePath(XMessage m){
		if(m.getType() == XMessage.TYPE_VIDEO){
			return getMessageFolderPath(m.getOtherSideId()) + File.separator +
					"video" + File.separator + "thumb" + File.separator + m.getId();
		}else{
			throw new IllegalArgumentException("videoFilePath just use type = Video");
		}
	}
	
	public String getMessageFilePath(XMessage m){
		if(m.getType() == XMessage.TYPE_FILE){
			return getMessageFolderPath(m.getOtherSideId()) + File.separator +
					"file" + File.separator + m.getId();
		}else{
			throw new IllegalArgumentException("filePath just use type = File");
		}
	}
	
	public String getMessageLocationFilePath(XMessage xm){
		return getMessageFolderPath(xm.getOtherSideId()) + File.separator +
				"location" + File.separator + xm.getId();
	}
	
	public String getMessageFolderPath(String strId){
		StringBuffer buf = new StringBuffer(mFilePathPrefix);
		buf.append(File.separator);
		if(!TextUtils.isEmpty(strId)){
			buf.append(strId);
		}else{
			throw new IllegalArgumentException("roomId is Empty");
		}
		
		return buf.toString();
	}
	
	public String getMessageFolderPath(){
		return mFilePathPrefix;
	}
}
