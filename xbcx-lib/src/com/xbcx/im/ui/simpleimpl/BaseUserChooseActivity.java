package com.xbcx.im.ui.simpleimpl;

import java.util.HashMap;

import android.os.Bundle;
import android.text.TextUtils;

import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.NameObject;
import com.xbcx.im.IMKernel;
import com.xbcx.im.VCardProvider;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.im.ui.XBaseActivity;
import com.xbcx.library.R;

public class BaseUserChooseActivity extends XBaseActivity {

	protected HashMap<String, String> 	mMapCheckUserIds = new HashMap<String, String>();
	protected HashMap<String, String> 	mMapUserIdToName = new HashMap<String, String>();
	
	protected String					mGroupId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final String defaultUser = getIntent().getStringExtra("defaultUserId");
		if(!TextUtils.isEmpty(defaultUser)){
			String defaultName = getIntent().getStringExtra("defaultUserName");
			if(TextUtils.isEmpty(defaultName)){
				defaultName = VCardProvider.getInstance().loadUserName(defaultUser);
			}
			mMapUserIdToName.put(defaultUser, defaultName);
		}
	}

	protected void createGroupOrAddGroupMember(){
		if(mMapCheckUserIds.size() > 0){
			if(TextUtils.isEmpty(mGroupId)){
				final String localUser = IMKernel.getLocalUser();
				mMapCheckUserIds.put(localUser, localUser);
				mMapUserIdToName.put(localUser, 
						VCardProvider.getInstance().loadUserName(localUser));
				pushEventBlock(EventCode.IM_CreateGroupChat,
						generateGroupNameFromCheckedUser(),mMapCheckUserIds.keySet());
			}else{
				pushEventBlock(EventCode.IM_AddGroupChatMember,
						mGroupId,mMapCheckUserIds.keySet());
			}
		}
	}
	
	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == EventCode.IM_CreateGroupChat){
			if(event.isSuccess()){
				final String groupId = (String)event.getReturnParamAtIndex(0);
				final String name = (String)event.getParamAtIndex(0);
				onCreateGroupSuccess(groupId, name);
			}else{
				mToastManager.show(R.string.toast_disconnect);
			}
		}else if(code == EventCode.IM_AddGroupChatMember){
			if(event.isSuccess()){
				onAddGroupMemberSuccess();
			}else{
				mToastManager.show(R.string.toast_disconnect);
			}
		}
	}
	
	protected void onCreateGroupSuccess(String groupId,String groupName){
		ActivityType.launchChatActivity(this, ActivityType.GroupChat, groupId, groupName);
		finish();
	}
	
	protected void onAddGroupMemberSuccess(){
		finish();
	}
	
	protected void addCheckUser(NameObject no){
		mMapCheckUserIds.put(no.getId(), no.getId());
		mMapUserIdToName.put(no.getId(), no.getName());
	}
	
	protected String generateGroupNameFromCheckedUser(){
		StringBuffer buf = new StringBuffer();
		int index = 0;
		for(String uid : mMapCheckUserIds.keySet()){
			if(index == 0){
				buf.append(mMapUserIdToName.get(uid));
			}else{
				buf.append(",").append(mMapUserIdToName.get(uid));
			}
			++index;
			if(index == 3){
				break;
			}
		}
		return buf.toString();
	}
}
