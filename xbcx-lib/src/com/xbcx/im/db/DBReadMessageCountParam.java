package com.xbcx.im.db;

public class DBReadMessageCountParam {
	
	public final int 	mFromType;
	
	public final String mId;
	
	public int 			mReturnCount;
	
	public DBReadMessageCountParam(int nFromType,String strId){
		mFromType = nFromType;
		mId = strId;
	}
}
