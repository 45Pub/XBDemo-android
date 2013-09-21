package com.xbcx.im.ui;

import android.util.SparseIntArray;

public class LocalAvatar {
	
	public static final int Group			= 1;
	public static final int Discussion		= 2;
	public static final int FriendVerify	= 3;

	private static SparseIntArray sMapAvatarToResId = new SparseIntArray();
	
	public static int 	getAvatarResId(int avatar){
		return sMapAvatarToResId.get(avatar);
	}
	
	public static void 	registerAvatarResId(int avatar,int resId){
		if(sMapAvatarToResId.get(avatar) != 0){
			throw new IllegalArgumentException("avatar:" + avatar + " has added");
		}
		sMapAvatarToResId.put(avatar, resId);
	}
}
