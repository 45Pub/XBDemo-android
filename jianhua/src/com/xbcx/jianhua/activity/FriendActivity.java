package com.xbcx.jianhua.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.xbcx.im.IMContact;
import com.xbcx.im.IMGroup;
import com.xbcx.im.ui.simpleimpl.AddressBooksActivity;
import com.xbcx.jianhua.R;

public class FriendActivity extends AddressBooksActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mTitleTextStringId = R.string.friend;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		super.onItemClick(parent, view, position, id);
		final Object item = parent.getItemAtPosition(position);
		if(item != null){
			if(item instanceof IMGroup){
				final IMGroup group = (IMGroup)item;
				JHGroupChatActivity.launch(this, group.getId(), group.getName());
			}else if(item instanceof IMContact){
				final IMContact contact = (IMContact)item;
				DetailUserActivity.launch(this, contact.getId(), contact.getName(), false);
			}
		}
	}
}
