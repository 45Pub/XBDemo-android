package com.xbcx.jianhua.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.im.IMKernel;
import com.xbcx.im.VerifyType;
import com.xbcx.im.ui.XBaseActivity;
import com.xbcx.jianhua.R;

public class PrivacyActivity extends XBaseActivity implements View.OnClickListener{

	private CheckBox	mCheckBoxAllowAddFriend;
	private CheckBox	mCheckBoxNeedVerify;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mCheckBoxAllowAddFriend = (CheckBox)findViewById(R.id.cbAllowAddFriend);
		mCheckBoxNeedVerify = (CheckBox)findViewById(R.id.cbNeedVerify);
		mCheckBoxAllowAddFriend.setOnClickListener(this);
		mCheckBoxNeedVerify.setOnClickListener(this);
		
		updateUI(IMKernel.getInstance().getVerifyType());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
		ba.mTitleTextStringId = R.string.setup_privacy;
	}

	public static void launch(Activity activity){
		Intent intent = new Intent(activity, PrivacyActivity.class);
		activity.startActivity(intent);
	}
	
	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == EventCode.IM_SetVerifyType){
			if(event.isSuccess()){
				final VerifyType type = (VerifyType)event.getParamAtIndex(0);
				updateUI(type);
			}else{
				updateUI(IMKernel.getInstance().getVerifyType());
				mToastManager.show(R.string.toast_disconnect);
			}
		}
	}
	
	protected void updateUI(VerifyType type){
		if(VerifyType.TYPE_FORBID.equals(type)){
			mCheckBoxAllowAddFriend.setChecked(false);
			mCheckBoxNeedVerify.setChecked(false);
			mCheckBoxNeedVerify.setEnabled(false);
		}else if(VerifyType.TYPE_AUTH.equals(type)){
			mCheckBoxAllowAddFriend.setChecked(true);
			mCheckBoxNeedVerify.setChecked(true);
			mCheckBoxNeedVerify.setEnabled(true);
		}else{
			mCheckBoxAllowAddFriend.setChecked(true);
			mCheckBoxNeedVerify.setChecked(false);
			mCheckBoxNeedVerify.setEnabled(true);
		}
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if(id == R.id.cbAllowAddFriend){
			if(mCheckBoxAllowAddFriend.isChecked()){
				pushEventBlock(EventCode.IM_SetVerifyType, VerifyType.TYPE_NONE);
			}else{
				pushEventBlock(EventCode.IM_SetVerifyType, VerifyType.TYPE_FORBID);
			}
		}else if(id == R.id.cbNeedVerify){
			if(mCheckBoxNeedVerify.isChecked()){
				pushEventBlock(EventCode.IM_SetVerifyType, VerifyType.TYPE_AUTH);
			}else{
				pushEventBlock(EventCode.IM_SetVerifyType, VerifyType.TYPE_NONE);
			}
		}
	}

}
