package com.xbcx.jianhua.activity;

import com.xbcx.im.ui.XBaseActivity;
import com.xbcx.jianhua.R;

import android.os.Bundle;
import android.view.View;

public class ChatInfoActivity extends XBaseActivity implements 
													View.OnClickListener{

	protected String	mId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mId = getIntent().getStringExtra("id");
		super.onCreate(savedInstanceState);
		
		findViewById(R.id.viewAddFromAdb).setOnClickListener(this);
		findViewById(R.id.viewAddFromFriend).setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
		ba.mTitleTextStringId = R.string.chat_info;
	}

	@Override
	public void onClick(View v) {
	}
}
