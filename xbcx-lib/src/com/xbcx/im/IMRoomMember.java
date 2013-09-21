package com.xbcx.im;

import com.xbcx.core.NameObject;

public class IMRoomMember extends NameObject{
	
	private static final long serialVersionUID = 1L;
	
	private final String	mRole;
	
	public IMRoomMember(String strId,String strName,String role){
		super(strId);
		mName = strName;
		mRole = role;
	}
	
	public String getRoleString(){
		return mRole;
	}
}
