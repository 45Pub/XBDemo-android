package com.xbcx.jianhua.activity;

import android.app.Activity;
import android.content.Intent;

import com.xbcx.jianhua.BaseInfo;
import com.xbcx.jianhua.JHApplication;
import com.xbcx.jianhua.JHEventCode;
import com.xbcx.jianhua.R;

public class EmailActivity extends BaseEditActivity {

	@Override
	protected void onSaveChange(String text) {
		BaseInfo bi = JHApplication.getBaseInfo();
		bi.setEmail(text);
		pushEvent(JHEventCode.HTTP_ChangeUserInfo, bi);
	}
	
	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mTitleTextStringId = R.string.setup_email;
	}

	public static void launch(Activity activity,String def){
		Intent intent = new Intent(activity, EmailActivity.class);
		intent.putExtra("def", def);
		activity.startActivity(intent);
	}
}
