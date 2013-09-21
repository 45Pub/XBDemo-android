package com.xbcx.jianhua;

import com.xbcx.core.ToastManager;
import com.xbcx.im.IMContact;
import com.xbcx.im.IMKernel;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.jianhua.activity.DetailUserActivity;

import android.app.Activity;


public class JHUtils {
	public static void launchDetails(Activity activity,Object obj){
		if(obj instanceof Departmember){
			Departmember dm = (Departmember)obj;
			if(dm.isUser()){
				DetailUserActivity.launch(activity, dm.getId(), dm.getName(), false);
			}else{
				//launchDepartmemberDetails(dm);
			}
		}else if(obj instanceof IMContact){
			final IMContact c = (IMContact)obj;
			DetailUserActivity.launch(activity, c.getId(), c.getName(), false);
		}
	}
	
	public static void handleUserItemClick(Activity activity,String id,String name){
		if(IMKernel.isLocalUser(id)){
			ToastManager.getInstance(activity).show(
					R.string.toast_cannot_chat_with_self);
		}else{
			ActivityType.launchChatActivity(activity, ActivityType.SingleChat, id, name);
		}
	}
}
