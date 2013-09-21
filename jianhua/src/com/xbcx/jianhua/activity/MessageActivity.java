package com.xbcx.jianhua.activity;

import com.xbcx.im.ui.simpleimpl.RecentChatActivity;
import com.xbcx.jianhua.R;

import android.os.Bundle;

public class MessageActivity extends RecentChatActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mTitleTextStringId = R.string.lexin;
	}
}
