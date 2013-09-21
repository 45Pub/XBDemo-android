package com.xbcx.im;

public class Role {
	
	public static final Role MODERATOR 		= 	new Role("moderator");
	public static final Role PARTICIPANT 	= 	new Role("participant");
	public static final Role VISITOR		= 	new Role("visitor");
	
	
	private String mRole;
	
	private Role(String strRole){
		mRole = strRole;
	}
	
	public String getStringValue(){
		return mRole;
	}
	
	public static Role valueOf(String strRole){
		if("moderator".equals(strRole)){
			return MODERATOR;
		}else if("participant".equals(strRole)){
			return PARTICIPANT;
		}else if("visitor".equals(strRole)){
			return VISITOR;
		}
		return PARTICIPANT;
	}
}
