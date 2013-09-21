package com.xbcx.im;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import com.xbcx.im.db.DBColumns;

import android.database.Cursor;

public class RecentChat {
	
	private final String 	mId;
	
	private String 			mName;
	
	private int				mUnreadMessageCount;
	
	private String	 		mContent;
	
	private long			mTime;
	
	private int				mLocalAvatar;
	
	private int				mActivityType;
	
	private Object			mExtraObj;
	private boolean			mIsExtraObjChanged;
	
	public RecentChat(String strId){
		mId = strId;
	}
	
	public RecentChat(Cursor cursor){
		mId = cursor.getString(cursor.getColumnIndex(DBColumns.RecentChatDB.COLUMN_ID));
		mName = cursor.getString(cursor.getColumnIndex(DBColumns.RecentChatDB.COLUMN_NAME));
		mUnreadMessageCount = cursor.getInt(cursor.getColumnIndex(DBColumns.RecentChatDB.COLUMN_UNREADCOUNT));
		mContent = cursor.getString(cursor.getColumnIndex(DBColumns.RecentChatDB.COLUMN_CONTENT));
		mLocalAvatar = cursor.getInt(cursor.getColumnIndex(DBColumns.RecentChatDB.COLUMN_LOCAL_AVATAR));
		mActivityType = cursor.getInt(cursor.getColumnIndex(DBColumns.RecentChatDB.COLUMN_ACTIVITY_TYPE));
		mTime = cursor.getLong(cursor.getColumnIndex(DBColumns.RecentChatDB.COLUMN_UPDATETIME));
		byte blob[] = cursor.getBlob(cursor.getColumnIndex(DBColumns.RecentChatDB.COLUMN_EXTRAOBJ));
		if(blob != null){
			ByteArrayInputStream bais = new ByteArrayInputStream(blob);
			try{
				ObjectInputStream ois = new ObjectInputStream(bais);
				mExtraObj = ois.readObject();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o){
			return true;
		}
		
		if(o != null && o instanceof RecentChat){
			return getId().equals(((RecentChat)o).getId());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return mId.hashCode();
	}

	public String 	getId(){
		return mId;
	}
	
	public String 	getName(){
		if(mName == null){
			return "";
		}
		return mName;
	}
	
	public int		getUnreadMessageCount(){
		return mUnreadMessageCount;
	}
	
	public String	getContent(){
		return mContent;
	}
	
	public boolean	isLocalAvatar(){
		return mLocalAvatar != 0;
	}
	
	public int		getLocalAvatar(){
		return mLocalAvatar;
	}
	
	public long		getTime(){
		return mTime;
	}
	
	public int		getActivityType(){
		return mActivityType;
	}
	
	public Object	getExtraObj(){
		return mExtraObj;
	}
	
	public boolean	isExtraObjChanged(){
		return mIsExtraObjChanged;
	}
	
	public void setName(String strName){
		mName = strName;
	}
	
	void setUnreadMessageCount(int nCount){
		mUnreadMessageCount = nCount;
	}
	
	void addUnreadMessageCount(){
		++mUnreadMessageCount;
	}
	
	public void setContent(String content){
		mContent = content;
	}
	
	public void setLocalAvatar(int localAvatar){
		mLocalAvatar = localAvatar;
	}
	
	void setTime(long time){
		mTime = time;
	}
	
	public void setActivityType(int type){
		mActivityType = type;
	}
	
	public void setExtraObj(Object obj){
		mExtraObj = obj;
		setExtraObjChanged(true);
	}
	
	public void setExtraObjChanged(boolean bChanged){
		mIsExtraObjChanged = bChanged;
	}
}
