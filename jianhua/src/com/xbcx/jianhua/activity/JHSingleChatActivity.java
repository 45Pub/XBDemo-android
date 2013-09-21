package com.xbcx.jianhua.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.xbcx.im.ui.simpleimpl.SingleChatActivity;
import com.xbcx.jianhua.R;

public class JHSingleChatActivity extends SingleChatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addImageButtonInTitleRight(R.drawable.nav_btn_info);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public static void launch(Activity activity,String id,String name){
		Intent intent = new Intent(activity, JHSingleChatActivity.class);
		intent.putExtra(EXTRA_ID, id);
		intent.putExtra(EXTRA_NAME, name);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(intent);
	}

	@Override
	protected void onTitleRightButtonClicked(View v) {
		super.onTitleRightButtonClicked(v);
		SingleChatInfoActivity.launch(this, mId,mName);
	}
}
