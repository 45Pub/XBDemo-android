package com.xbcx.im;

import android.content.ContentValues;
import android.database.Cursor;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.EventCode;
import com.xbcx.im.XMessage;
import com.xbcx.im.db.DBColumns;
import com.xbcx.im.messageprocessor.FileMessageDownloadProcessor;
import com.xbcx.im.messageprocessor.FileMessageUploadProcessor;
import com.xbcx.im.messageprocessor.LocationMessageDownloadProcessor;
import com.xbcx.im.messageprocessor.PhotoMessageDownloadProcessor;
import com.xbcx.im.messageprocessor.PhotoMessageUploadProcessor;
import com.xbcx.im.messageprocessor.VideoMessageDownloadProcessor;
import com.xbcx.im.messageprocessor.VideoMessageUploadProcessor;
import com.xbcx.im.messageprocessor.VoiceMessageDownloadProcessor;
import com.xbcx.im.messageprocessor.VoiceMessageUploadProcessor;
import com.xbcx.utils.SystemUtils;

public class IMMessage extends XMessage {
	
	protected ContentValues	mContentValues = new ContentValues();
	
	public IMMessage(String strId,int nType){
		super(strId, nType);
		
		mContentValues.put(DBColumns.Message.COLUMN_ID, strId);
		mContentValues.put(DBColumns.Message.COLUMN_TYPE, nType);
	}
	
	public IMMessage(Cursor cursor){
		super(cursor.getString(cursor.getColumnIndex(DBColumns.Message.COLUMN_ID)),
				cursor.getInt(cursor.getColumnIndex(DBColumns.Message.COLUMN_TYPE)));
		mUserId = cursor.getString(cursor.getColumnIndex(DBColumns.Message.COLUMN_USERID));
		mUserName = cursor.getString(cursor.getColumnIndex(DBColumns.Message.COLUMN_USERNAME));
		mContent = cursor.getString(cursor.getColumnIndex(DBColumns.Message.COLUMN_CONTENT));
		mIsFromSelf = SystemUtils.getCursorBoolean(cursor, 
				cursor.getColumnIndex(DBColumns.Message.COLUMN_FROMSELF));
		mSendTime = cursor.getLong(cursor.getColumnIndex(DBColumns.Message.COLUMN_SENDTIME));
		final int nExtension = cursor.getInt(cursor.getColumnIndex(DBColumns.Message.COLUMN_EXTENSION));
		setExtension(nExtension);
		mUrl = cursor.getString(cursor.getColumnIndex(DBColumns.Message.COLUMN_URL));
		mSize = cursor.getLong(cursor.getColumnIndex(DBColumns.Message.COLUMN_SIZE));
		mDisplayName = cursor.getString(cursor.getColumnIndex(DBColumns.Message.COLUMN_DISPLAY));
		mExtString = cursor.getString(cursor.getColumnIndex(DBColumns.Message.COLUMN_EXTSTRING));
		try{
			mExtObj = SystemUtils.byteArrayToObject(
					cursor.getBlob(
							cursor.getColumnIndex(DBColumns.Message.COLUMN_EXTOBJ)));
		}catch(Exception e){
			e.printStackTrace();
		}
		
		setStoraged();
	}

	@Override
	public void setUserId(String strUserId) {
		super.setUserId(strUserId);
		mContentValues.put(DBColumns.Message.COLUMN_USERID, strUserId);
	}

	@Override
	public void setUserName(String strUserName) {
		super.setUserName(strUserName);
		mContentValues.put(DBColumns.Message.COLUMN_USERNAME, strUserName);
	}

	@Override
	public void setContent(String strContent) {
		super.setContent(strContent);
		mContentValues.put(DBColumns.Message.COLUMN_CONTENT, strContent);
	}

	@Override
	public void setFromSelf(boolean bFromSelf) {
		super.setFromSelf(bFromSelf);
		mContentValues.put(DBColumns.Message.COLUMN_FROMSELF, bFromSelf);
	}

	@Override
	public void setSendTime(long lTime) {
		super.setSendTime(lTime);
		mContentValues.put(DBColumns.Message.COLUMN_SENDTIME, lTime);
	}

	@Override
	protected void setExtension(int nIndex, boolean bValue) {
		super.setExtension(nIndex, bValue);
		mContentValues.put(DBColumns.Message.COLUMN_EXTENSION, getExtension());
	}

	@Override
	public void setUrl(String url) {
		super.setUrl(url);
		mContentValues.put(DBColumns.Message.COLUMN_URL, url);
	}

	@Override
	public void setSize(long size) {
		super.setSize(size);
		mContentValues.put(DBColumns.Message.COLUMN_SIZE, size);
	}

	@Override
	public void setBubbleId(String id) {
		super.setBubbleId(id);
		mContentValues.put(DBColumns.Message.COLUMN_BUBBLEID, id);
	}

	@Override
	public void setDisplayName(String name) {
		super.setDisplayName(name);
		mContentValues.put(DBColumns.Message.COLUMN_DISPLAY,mDisplayName);
	}

	@Override
	public void setExtString(String ext) {
		super.setExtString(ext);
		mContentValues.put(DBColumns.Message.COLUMN_EXTSTRING, mExtString);
	}

	@Override
	public void setExtObj(Object ext) {
		super.setExtObj(ext);
		try{
			mContentValues.put(DBColumns.Message.COLUMN_EXTOBJ, SystemUtils.objectToByteArray(ext));
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public ContentValues getSaveContentValues(){
		return mContentValues;
	}
	
	@Override
	protected void onUpdateDB() {
		if(mContentValues.size() > 0){
			AndroidEventManager.getInstance().runEvent(EventCode.DB_SaveMessage, this);
		}
	}

	@Override
	public boolean isVoiceDownloading() {
		return VoiceMessageDownloadProcessor.getInstance().isDownloading(this);
	}

	@Override
	public boolean isVoiceUploading() {
		return VoiceMessageUploadProcessor.getInstance().isUploading(this);
	}

	@Override
	public boolean isThumbPhotoDownloading() {
		return PhotoMessageDownloadProcessor.getInstance().isThumbDownloading(this);
	}

	@Override
	public int getThumbPhotoDownloadPercentage() {
		return PhotoMessageDownloadProcessor.getInstance().getDownloadPercentage(this);
	}

	@Override
	public boolean isPhotoUploading() {
		return PhotoMessageUploadProcessor.getInstance().isUploading(this);
	}

	@Override
	public int getPhotoUploadPercentage() {
		return PhotoMessageUploadProcessor.getInstance().getUploadPercentage(this);
	}

	@Override
	public boolean isVideoUploading() {
		return VideoMessageUploadProcessor.getInstance().isUploading(this);
	}

	@Override
	public int getVideoUploadPercentage() {
		return VideoMessageUploadProcessor.getInstance().getUploadPercentage(this);
	}

	@Override
	public boolean isVideoDownloading() {
		return VideoMessageDownloadProcessor.getInstance().isDownloading(this);
	}

	@Override
	public int getVideoDownloadPercentage() {
		return VideoMessageDownloadProcessor.getInstance().getDownloadPercentage(this);
	}

	@Override
	public boolean isVideoThumbDownloading() {
		return VideoMessageDownloadProcessor.getInstance().isThumbDownloading(this);
	}

	@Override
	public int getVideoThumbDownloadPercentage() {
		return VideoMessageDownloadProcessor.getInstance().getThumbDownloadPercentage(this);
	}

	@Override
	public int getFileUploadPercentage() {
		return FileMessageUploadProcessor.getInstance().getUploadPercentage(this);
	}

	@Override
	public int getFileDownloadPercentage() {
		return FileMessageDownloadProcessor.getInstance().getDownloadPercentage(this);
	}

	@Override
	public boolean isFileUploading() {
		return FileMessageUploadProcessor.getInstance().isUploading(this);
	}

	@Override
	public boolean isFileDownloading() {
		return FileMessageDownloadProcessor.getInstance().isDownloading(this);
	}

	@Override
	public boolean isLocationDownloading() {
		return LocationMessageDownloadProcessor.getInstance().isDownloading(this);
	}
}
