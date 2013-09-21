package com.xbcx.im.db;

import java.util.List;

import com.xbcx.im.XMessage;

public class DBReadMessageParam {
	public String 			mId;
	public int				mFromType;
	
	public int				mReadPosition;
	public int				mReadCount;
	
	public List<XMessage>	mMessages;
	
	public DBReadMessageParam(String id,int fromType){
		mId = id;
		mFromType = fromType;
	}
}
