package com.xbcx.im;

import java.io.File;

import com.xbcx.im.ui.IMGlobalSetting;
import com.xbcx.utils.SystemUtils;



import android.text.TextUtils;

public abstract class XMessage{
	public static final int TYPE_TIME 		= 0;
	public static final int TYPE_TEXT 		= 1;
	public static final int TYPE_VOICE 		= 2;
	public static final int TYPE_PHOTO 		= 3;
	public static final int TYPE_VIDEO		= 4;
	public static final int TYPE_FILE		= 5;
	public static final int TYPE_LOCATION	= 6;
	public static final int TYPE_PROMPT		= 10;
	
	public static final int FROMTYPE_SINGLE 	= 1;
	public static final int FROMTYPE_GROUP 		= 2;
	public static final int FROMTYPE_DISCUSSION = 3;
	public static final int FROMTYPE_CHATROOM	= 4;
	
	protected static final int EXTENSION_COUNT = 8;
	
	protected static final int EXTENSION_SENDED 			= 0;
	protected static final int EXTENSION_SENDSUCCESS 		= 1;
	protected static final int EXTENSION_DOWNLOADED 		= 2;
	protected static final int EXTENSION_UPLOADSUCCESS 		= 3;
	protected static final int EXTENSION_PLAYED 			= 4;
	protected static final int EXTENSION_FRIENDASK_HANDLED 	= 5;
	
	protected String 		mId;
	protected int			mType;
	
	protected String		mUserId;
	protected String		mUserName;
	
	protected String		mContent;
	
	protected boolean		mIsFromSelf;
	
	protected int			mFromType;
	
	protected long			mSendTime;
	
	protected String		mGroupId;
	protected String		mGroupName;
	
	protected boolean		mExtension[] = new boolean[EXTENSION_COUNT];
	
	protected String		mUrl;
	protected long			mSize;
	protected String		mBubbleId;
	protected String		mDisplayName;
	
	protected String		mExtString;
	protected Object		mExtObj;
	
	protected boolean		mReaded;
	protected boolean 		mStoraged;
	
	protected Object		mTag;
	
	public XMessage(String strId,int nType){
		mId = strId;
		mType = nType;
		
		if(mId == null){
			throw new IllegalArgumentException("id can't be null");
		}
	}
	
	public static String buildMessageId(){
		return String.valueOf(System.currentTimeMillis()) + SystemUtils.randomRange(100, 999);
	}
	
	public static XMessage createTimeMessage(long sendTime){
		XMessage m = IMGlobalSetting.msgFactory.createXMessage(
				XMessage.buildMessageId(), TYPE_TIME);
		m.mSendTime = sendTime;
		return m;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o){
			return true;
		}
		if(o != null && o instanceof XMessage){
			return ((XMessage)o).getId().equals(getId());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return mId.hashCode();
	}

	public String 	getId() {
		return mId;
	}

	public int 		getType() {
		return mType;
	}
	
	public String	getUserId(){
		return mUserId;
	}
	
	public String	getUserName(){
		return mUserName;
	}
	
	public String	getContent(){
		return mContent;
	}

	public boolean	isFromSelf(){
		return mIsFromSelf;
	}
	
	public int		getFromType(){
		return mFromType;
	}
	
	public long		getSendTime(){
		return mSendTime;
	}
	
	public String	getGroupId(){
		return mGroupId;
	}
	
	public String	getGroupName(){
		return mGroupName;
	}
	
	public boolean	isFromGroup(){
		return mGroupId != null;
	}
	
	public String	getOtherSideId(){
		if(isFromGroup()){
			return getGroupId();
		}else{
			return getUserId();
		}
	}
	
	public void		setUserId(String strUserId){
		mUserId = strUserId;
	}
	
	public void 	setUserName(String strUserName){
		mUserName = strUserName;
	}
	
	public void		setContent(String strContent){
		mContent = strContent;
	}
	
	public void		setFromSelf(boolean bFromSelf){
		mIsFromSelf = bFromSelf;
	}
	
	public void		setFromType(int nFromType){
		mFromType = nFromType;
	}
	
	public void		setSendTime(long lTime){
		mSendTime = lTime;
	}
	
	public void 	setGroupId(String strGroupId){
		mGroupId = strGroupId;
	}
	
	public void		setGroupName(String name){
		mGroupName = name;
	}
	
	public boolean	isSended(){
		return mExtension[EXTENSION_SENDED];
	}
	
	public boolean 	isSendSuccess(){
		return mExtension[EXTENSION_SENDSUCCESS];
	}
	
	public boolean 	isDownloaded(){
		return mExtension[EXTENSION_DOWNLOADED];
	}
	
	public boolean 	isPlayed(){
		return mExtension[EXTENSION_PLAYED];
	}
	
	public boolean 	isUploadSuccess(){
		return mExtension[EXTENSION_UPLOADSUCCESS];
	}
	
	public void setSended(){
		setExtension(EXTENSION_SENDED,true);
	}
	
	public void setSendSuccess(boolean bSuccess) {
		setExtension(EXTENSION_SENDSUCCESS, bSuccess);
	}

	public void setDownloaded() {
		setExtension(EXTENSION_DOWNLOADED, true);
	}

	public void setUploadSuccess(boolean bSuccess) {
		setExtension(EXTENSION_UPLOADSUCCESS, bSuccess);
	}

	public void setPlayed(boolean bPlayed) {
		setExtension(EXTENSION_PLAYED, bPlayed);
	}
	
	public void setAddFriendAskHandled(boolean bHandled){
		setExtension(EXTENSION_FRIENDASK_HANDLED, bHandled);
	}
	
	public boolean isAddFriendAskHandled(){
		return mExtension[EXTENSION_FRIENDASK_HANDLED];
	}
	
	protected void setExtension(int nIndex,boolean bValue){
		if(mExtension[nIndex] != bValue){
			mExtension[nIndex] = bValue;
		}
	}
	
	protected void setExtension(int nExtension){
		for(int nIndex = 0;nIndex < EXTENSION_COUNT;++nIndex){
			mExtension[nIndex] = (((nExtension >> nIndex) & 0x01) == 1);
		}
	}
	
	protected int getExtension(){
		int nExtension = 0;
		for(int nIndex = 0;nIndex < EXTENSION_COUNT;++nIndex){
			nExtension = ((mExtension[nIndex] ? 1 : 0) << nIndex) | nExtension;
		}
		return nExtension;
	}
	
	public void setUrl(String url){
		mUrl = url;
	}
	
	public String getUrl(){
		return mUrl;
	}
	
	public void setSize(long size){
		mSize = size;
	}
	
	public void setBubbleId(String id){
		mBubbleId = id;
	}
	
	public String getBubbleId(){
		return mBubbleId;
	}
	
	public void setExtString(String ext){
		mExtString = ext;
	}
	
	public String getExtString(){
		return mExtString;
	}
	
	public void	setExtObj(Object ext){
		mExtObj = ext;
	}
	
	public Object getExtObj(){
		return mExtObj;
	}
	
	public boolean	isStoraged(){
		return mStoraged;
	}
	
	public boolean 	isReaded(){
		return mReaded;
	}
	
	public void 	setStoraged(){
		mStoraged = true;
	}
	
	public void 	setReaded(boolean bReaded){
		mReaded = bReaded;
	}
	
	public void		setTag(Object tag){
		mTag = tag;
	}
	
	public Object	getTag(){
		return mTag;
	}
	
	public void updateDB(){
		if(isStoraged()){
			onUpdateDB();
		}
	}
	
	public String getFileName(){
		return mId;
	}
	
	public void setDisplayName(String name){
		mDisplayName = name;
	}
	
	public String getDisplayName(){
		String ret;
		if(mType == XMessage.TYPE_VIDEO){
			ret = getVideoDisplayName();
		}else{
			ret = mDisplayName;
		}
		if(TextUtils.isEmpty(ret)){
			if(mType == XMessage.TYPE_PHOTO){
				return mId + ".jpg";
			}else if(mType == XMessage.TYPE_VOICE){
				return mId + ".amr";
			}else{
				return mId;
			}
		}else{
			return ret;
		}
	}
	
	public void		setPhotoDownloadUrl(String url){
		setUrl(url);
	}
	
	public String	getPhotoDownloadUrl(){
		return mUrl;
	}
	
	public void		setThumbPhotoDownloadUrl(String url){
		setContent(url);
	}
	
	public String	getThumbPhotoDownloadUrl(){
		return getContent();
	}
	
	public void		setVoiceDownloadUrl(String url){
		setContent(url);
	}
	
	public String	getVoiceDownloadUrl(){
		return getContent();
	}
	
	public void setVoiceFrameCount(int frameCount){
		setSize(frameCount);
	}
	
	public int getVoiceFrameCount(){
		return (int)mSize;
	}
	
	public int getVoiceSeconds(){
		if(mType == TYPE_VOICE){
			int nFrameCount = getVoiceFrameCount();
			int lSeconds = nFrameCount / 50;
			if(lSeconds <= 0)lSeconds = 1;
			return lSeconds;
		}
		return 0;
	}
	
	public int getVoiceMilliseconds(){
		if(mType == TYPE_VOICE){
			int nFrameCount = getVoiceFrameCount();
			return nFrameCount * 20;
		}
		return 0;
	}
	
	public void	setVideoDownloadUrl(String url){
		setUrl(url);
	}
	
	public String getVideoDownloadUrl(){
		return mUrl;
	}
	
	public void setVideoThumbDownloadUrl(String url){
		setContent(url);
	}
	
	public String getVideoThumbDownloadUrl(){
		return getContent();
	}
	
	public void setVideoFilePath(String filePath){
		setDisplayName(filePath);
	}
	
	public String getVideoFilePath(){
		if(mDisplayName != null && mDisplayName.contains(File.separator)){
			return mDisplayName;
		}else{
			return IMFilePathManager.getInstance().getMessageVideoFilePath(this);
		}
	}
	
	protected String getVideoDisplayName(){
		if(mDisplayName != null){
			int pos = mDisplayName.lastIndexOf(File.separator);
			if(pos >= 0){
				return mDisplayName.substring(pos + File.separator.length());
			}
			return mDisplayName;
		}
		return mDisplayName;
	}
	
	public void setVideoSeconds(int seconds){
		setSize(seconds);
	}
	
	public int	getVideoSeconds(){
		return (int)mSize;
	}
	
	public void setOfflineFileDownloadUrl(String url){
		setUrl(url);
	}
	
	public String getOfflineFileDownloadUrl(){
		return mUrl;
	}
	
	public void setFileSize(long size){
		setSize(size);
	}
	
	public long getFileSize(){
		return mSize;
	}
	
	public void setLocation(double lat,double lng){
		setUrl(String.valueOf(lat) + "," + String.valueOf(lng));
	}
	
	/**
	 * String[0]:lat
	 * </br>String[1]:lng
     */
	public String[] getLocation(){
		return mUrl.split(",");
	}
	
	public boolean isVoiceFileExists(){
		return new File(getVoiceFilePath()).exists();
	}
	
	public boolean	isPhotoFileExists(){
		return new File(getPhotoFilePath()).exists();
	}
	
	public boolean 	isThumbPhotoFileExists(){
		return new File(getThumbPhotoFilePath()).exists();
	}
	
	public boolean 	isVideoFileExists(){
		return new File(getVideoFilePath()).exists();
	}
	
	public boolean 	isVideoThumbFileExists(){
		return new File(getVideoThumbFilePath()).exists();
	}
	
	public boolean 	isFileExists(){
		return new File(getFilePath()).exists();
	}
	
	public boolean	isLocationFileExists(){
		return new File(getLocationFilePath()).exists();
	}
	
	public String	getPhotoFilePath(){
		return IMFilePathManager.getInstance().getMessagePhotoFilePath(this);
	}
	
	public String	getThumbPhotoFilePath(){
		return IMFilePathManager.getInstance().getMessagePhotoThumbFilePath(this);
	}
	
	public String 	getVoiceFilePath() {
		return IMFilePathManager.getInstance().getMessageVoiceFilePath(this);
	}
	
	public String 	getVideoThumbFilePath() {
		return IMFilePathManager.getInstance().getMessageVideoThumbFilePath(this);
	}
	
	public String 	getFilePath() {
		return IMFilePathManager.getInstance().getMessageFilePath(this);
	}
	
	public String	getLocationFilePath(){
		return IMFilePathManager.getInstance().getMessageLocationFilePath(this);
	}
	
	protected abstract void onUpdateDB();
	
	public abstract boolean	isVoiceDownloading();
	
	public abstract boolean	isVoiceUploading();
	
	public abstract boolean isThumbPhotoDownloading();
	
	public abstract int		getThumbPhotoDownloadPercentage();
	
	public abstract boolean	isPhotoUploading();
	
	public abstract int		getPhotoUploadPercentage();
	
	public abstract boolean	isVideoUploading();
	
	public abstract	int		getVideoUploadPercentage();
	
	public abstract boolean	isVideoDownloading();
	
	public abstract boolean isVideoThumbDownloading();
	
	public abstract int		getVideoDownloadPercentage();
	
	public abstract int		getVideoThumbDownloadPercentage();
	
	public abstract boolean	isFileUploading();
	
	public abstract boolean isFileDownloading();
	
	public abstract int		getFileUploadPercentage();
	
	public abstract int		getFileDownloadPercentage();
	
	public abstract boolean	isLocationDownloading();
}
