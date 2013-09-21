package com.xbcx.im.ui.simpleimpl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.im.XMessage;
import com.xbcx.im.ui.BaseChatActivity;
import com.xbcx.im.ui.messageviewprovider.FriendVerifyNoticeViewProvider;
import com.xbcx.im.ui.messageviewprovider.TimeViewProvider;
import com.xbcx.library.R;

public class FriendVerifyChatActivity extends BaseChatActivity implements
												AdapterView.OnItemClickListener{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addAndManageEventListener(EventCode.IM_AddFriendConfirm);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	public static void launch(Activity activity,String id,String name){
		Intent intent = new Intent(activity,FriendVerifyChatActivity.class);
		activity.startActivity(intent);
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
		ba.mTitleText = mName;
		ba.mActivityLayoutId = R.layout.activity_friendverifychat;
	}

	@Override
	protected void onInit() {
		super.onInit();
		mListView.setOnItemClickListener(this);
	}

	@Override
	protected void onAddMessageViewProvider() {
		mMessageAdapter.addIMMessageViewProvider(new TimeViewProvider());
		mMessageAdapter.addIMMessageViewProvider(new FriendVerifyNoticeViewProvider(this));
	}

	@Override
	protected int getFromType() {
		return XMessage.FROMTYPE_GROUP;
	}

	@Override
	protected boolean isGroupChat() {
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	}

	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int nCode = event.getEventCode();
		if(nCode == EventCode.IM_AddFriendConfirm){
			if(event.isSuccess()){
				Object tag = getTag();
				if(tag != null && tag instanceof XMessage){
					XMessage m = (XMessage)tag;
					m.setAddFriendAskHandled(true);
					m.updateDB();
					redrawMessage(m);
				}
			}
		}
	}

}
