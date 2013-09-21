package com.xbcx.jianhua.activity;

import com.xbcx.jianhua.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SingleChatInfoActivity extends ChatInfoActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public static void launch(Activity activity,String id,String name){
		Intent intent = new Intent(activity, SingleChatInfoActivity.class);
		intent.putExtra("id", id);
		intent.putExtra("name", name);
		activity.startActivity(intent);
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mTitleTextStringId = R.string.create_discussion;
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		final int id = v.getId();
		if(id == R.id.viewAddFromAdb){
			AddMemberFromOrgActivity.launch(this, null,mId,getIntent().getStringExtra("name"));
		}else if(id == R.id.viewAddFromFriend){ 
			AddMemberFromFriendActivity.launch(this,null,mId,getIntent().getStringExtra("name"));
		}
	}
}
