package com.xbcx.jianhua.httprunner;

public class StringIdException extends Exception {

	private static final long serialVersionUID = 1L;

	private int	mStringId;
	
	public StringIdException(int stringId){
		mStringId = stringId;
	}
	
	public int getStringId(){
		return mStringId;
	}
}
