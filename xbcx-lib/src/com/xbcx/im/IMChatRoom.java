package com.xbcx.im;

import com.xbcx.core.NameObject;

public class IMChatRoom extends NameObject{
	
	private static final long serialVersionUID = 1L;
    
	public IMChatRoom(String strId,String strName){
		super(strId);
		mName = strName;
	}
}
