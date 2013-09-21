package com.xbcx.jianhua.activity;

import android.content.Intent;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.xbcx.core.Event;
import com.xbcx.core.SharedPreferenceDefine;
import com.xbcx.im.ui.XBaseActivity;
import com.xbcx.jianhua.BaseInfo;
import com.xbcx.jianhua.JHApplication;
import com.xbcx.jianhua.JHEventCode;
import com.xbcx.jianhua.R;

public class LoginActivity extends XBaseActivity implements View.OnClickListener{
	
	private EditText	mEditTextUser;
	private EditText	mEditTextPwd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mEditTextUser = (EditText)findViewById(R.id.etUser);
		mEditTextPwd = (EditText)findViewById(R.id.etPwd);
		
		SharedPreferences sp = getSharedPreferences(SharedPreferenceDefine.SP_IM, 0);
		mEditTextUser.setText(sp.getString(JHApplication.KEY_EMAIL, ""));
		
		findViewById(R.id.btnRegister).setOnClickListener(this);
		findViewById(R.id.btnLogin).setOnClickListener(this);
		
		addAndManageEventListener(JHEventCode.MainActivityLaunched);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mTitleTextStringId = R.string.login_and_register;
	}
	
	public static void launch(Activity activity){
		Intent intent = new Intent(activity, LoginActivity.class);
		activity.startActivity(intent);
	}

	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == JHEventCode.HTTP_Login){
			if(event.isSuccess()){
				BaseInfo bi = (BaseInfo)event.getReturnParamAtIndex(0);
				
				JHApplication.saveBaseInfo(bi);
				
				JHApplication.login(bi.getIMUser(), bi.getIMPwd());
				
				MainActivity.launch(this);
			}
		}else if(code == JHEventCode.MainActivityLaunched){
			finish();
		}
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if(id == R.id.btnRegister){
			RegisterActivity.launch(this);
		}else if(id == R.id.btnLogin){
			final String user = mEditTextUser.getText().toString();
			final String pwd = mEditTextPwd.getText().toString();
			if(TextUtils.isEmpty(user) ||
					TextUtils.isEmpty(pwd)){
				return;
			}
			//if(!SystemUtils.isEmail(user)){
			//	mToastManager.show(R.string.toast_email_error);
			//	return;
			//}
			
			pushEvent(JHEventCode.HTTP_Login, user,pwd);
		}
	}

}
