package com.xbcx.im;

import com.xbcx.im.XMessage;

public class MessageFilter {
	
	public static boolean accept(String strAcceptFromId,XMessage m){
		return strAcceptFromId.equals(m.getOtherSideId());
	}
}
