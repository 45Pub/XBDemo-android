package com.xbcx.im.ui.simpleimpl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.im.XMessage;
import com.xbcx.im.ui.BaseChatActivity;
import com.xbcx.library.R;

public class GroupChatActivity extends BaseChatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addAndManageEventListener(EventCode.IM_ChangeGroupChatName);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mActivityLayoutId = R.layout.activity_chat;
	}

	public static void launch(Activity activity,String id,String name){
		Intent intent = new Intent(activity, GroupChatActivity.class);
		intent.putExtra(EXTRA_ID, id);
		intent.putExtra(EXTRA_NAME, name);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(intent);
	}
	
	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == EventCode.IM_ChangeGroupChatName){
			if(event.isSuccess()){
				final String id = (String)event.getParamAtIndex(0);
				if(mId.equals(id)){
					final String name = (String)event.getParamAtIndex(1);
					mTextViewTitle.setText(name);
				}
			}
		}
	}

	@Override
	protected int getFromType() {
		return XMessage.FROMTYPE_GROUP;
	}

	@Override
	protected boolean isGroupChat() {
		return true;
	}

	protected String getContextMenuTitle(XMessage message){
		return message.getUserName();
	}
}
