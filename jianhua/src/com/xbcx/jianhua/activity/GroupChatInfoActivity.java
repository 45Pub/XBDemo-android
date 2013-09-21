package com.xbcx.jianhua.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.im.IMGroup;
import com.xbcx.im.IMKernel;
import com.xbcx.im.ui.simpleimpl.GroupMemberActivity;
import com.xbcx.jianhua.R;

public class GroupChatInfoActivity extends ChatInfoActivity {
	
	protected IMGroup	mGroup;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		findViewById(R.id.btnDelete).setOnClickListener(this);
		findViewById(R.id.viewViewGroupMember).setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public static void launch(Activity activity,String groupId){
		Intent intent = new Intent(activity, GroupChatInfoActivity.class);
		intent.putExtra("id", groupId);
		activity.startActivity(intent);
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		IMGroup group = IMKernel.getInstance().getGroup(mId);
		/*if(group == null){
			ba.mTitleTextStringId = R.string.chat_info;
		}else{
			ba.mTitleText = getString(R.string.chat_info) + 
					"(" + group.getMemberCount() + getString(R.string.people) + ")";
		}*/
		ba.mTitleTextStringId = R.string.discussion;
		mGroup = group;
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		final int id = v.getId();
		if(id == R.id.viewAddFromAdb){
			AddMemberFromOrgActivity.launch(this, mId,null,null);
		}else if(id == R.id.viewAddFromFriend){ 
			AddMemberFromFriendActivity.launch(this,mId,null,null);
		}else if(id == R.id.btnDelete){
			if(mGroup != null){
				if(IMGroup.ROLE_ADMIN.equals(mGroup.getMemberRole(IMKernel.getLocalUser()))){
					pushEventBlock(EventCode.IM_DeleteGroupChat, mId);
				}else{
					pushEventBlock(EventCode.IM_QuitGroupChat, mId);
				}
			}
		}else if(id == R.id.viewViewGroupMember){
			GroupMemberActivity.launch(this, mId);
		}
	}

	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == EventCode.IM_DeleteGroupChat ||
				code == EventCode.IM_QuitGroupChat){
			if(event.isSuccess()){
				finish();
			}else{
				mToastManager.show(R.string.toast_disconnect);
			}
		}
	}
}
