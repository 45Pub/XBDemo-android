package com.xbcx.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.util.SparseArray;

public class ActivityType {
	
	public static final int SingleChat 			= 1;
	public static final int GroupChat			= 2;
	public static final int DiscussionChat		= 3;
	public static final int FriendVerify		= 4;
	
	public static final int UserDetailActivity	= 6;
	public static final int SelfDetailActivity	= 7;
	public static final int ChooseFileActivity	= 8;

	private static SparseArray<String> sMapActivityTypeToActivityClass = new SparseArray<String>();
	
	public static String	getActivityClassName(int activity){
		return sMapActivityTypeToActivityClass.get(activity);
	}
	
	public static void		registerActivityClassName(int activity,String className){
		if(sMapActivityTypeToActivityClass.get(activity) != null){
			throw new IllegalArgumentException("activityType:" + activity + "has added");
		}
		sMapActivityTypeToActivityClass.put(activity, className);
	}
	
	public static void		launchChatActivity(Activity activity,
			int activityType,String id,String name){
		try{
			Intent intent = new Intent(activity, 
					Class.forName(getActivityClassName(activityType)));
			intent.putExtra("id", id);
			intent.putExtra("name", name);
			activity.startActivity(intent);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
