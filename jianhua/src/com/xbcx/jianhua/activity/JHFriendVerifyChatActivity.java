package com.xbcx.jianhua.activity;

import android.view.View;
import android.widget.AdapterView;

import com.xbcx.im.XMessage;
import com.xbcx.im.ui.simpleimpl.FriendVerifyChatActivity;

public class JHFriendVerifyChatActivity extends FriendVerifyChatActivity {
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		super.onItemClick(parent, view, position, id);
		final Object item = parent.getItemAtPosition(position);
		if(item != null && item instanceof XMessage){
			final XMessage xm = (XMessage)item;
			if(!xm.isAddFriendAskHandled()){
				setTag(xm);
				DetailUserActivity.launch(this, xm.getUserId(), xm.getUserName(), true);
			}
		}
	}

}
