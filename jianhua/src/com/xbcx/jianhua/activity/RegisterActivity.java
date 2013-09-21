package com.xbcx.jianhua.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.xbcx.core.Event;
import com.xbcx.im.ui.XBaseActivity;
import com.xbcx.jianhua.BaseInfo;
import com.xbcx.jianhua.JHApplication;
import com.xbcx.jianhua.JHEventCode;
import com.xbcx.jianhua.R;

public class RegisterActivity extends XBaseActivity implements View.OnClickListener{
	
	private EditText	mEditTextUser;
	private EditText	mEditTextName;
	private TextView	mTextViewRegion;
	private EditText	mEditTextMobilePhone;
	private EditText	mEditTextFixPhone;
	private EditText	mEditTextPwd;
	private EditText	mEditTextPwdSure;
	
	private int		mRequestCodeRegion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mEditTextUser = (EditText)findViewById(R.id.etUser);
		mEditTextName = (EditText)findViewById(R.id.etName);
		mTextViewRegion = (TextView)findViewById(R.id.tvRegion);
		mEditTextMobilePhone = (EditText)findViewById(R.id.etMobilePhone);
		mEditTextFixPhone = (EditText)findViewById(R.id.etFixPhone);
		mEditTextPwd = (EditText)findViewById(R.id.etPwd);
		mEditTextPwdSure = (EditText)findViewById(R.id.etPwdSure);
		findViewById(R.id.btnRegister).setOnClickListener(this);
		mTextViewRegion.setOnClickListener(this);
		
		addAndManageEventListener(JHEventCode.MainActivityLaunched);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mTitleTextStringId = R.string.register_new_account;
		ba.mAddBackButton = true;
	}
	
	public static void launch(Activity activity){
		Intent intent = new Intent(activity, RegisterActivity.class);
		activity.startActivity(intent);
	}

	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == JHEventCode.HTTP_Register){
			if(event.isSuccess()){
				final BaseInfo bi = (BaseInfo)event.getReturnParamAtIndex(0);
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
			final String user = mEditTextUser.getText().toString();
			final String name = mEditTextName.getText().toString();
			final String region = mTextViewRegion.getText().toString();
			final String mobilephone = mEditTextMobilePhone.getText().toString();
			final String fixphone = mEditTextFixPhone.getText().toString();
			final String pwd = mEditTextPwd.getText().toString();
			final String pwdSure = mEditTextPwdSure.getText().toString();
			if(TextUtils.isEmpty(user) ||
					TextUtils.isEmpty(name) ||
					TextUtils.isEmpty(region) ||
					TextUtils.isEmpty(mobilephone) ||
					TextUtils.isEmpty(fixphone) ||
					TextUtils.isEmpty(pwd) ||
					TextUtils.isEmpty(pwdSure)){
				mToastManager.show(R.string.toast_please_fill_full);
				return;
			}
			//if(!SystemUtils.isEmail(user)){
			//	mToastManager.show(R.string.toast_email_error);
			//	return;
			//}
			if(!pwd.equals(pwdSure)){
				mToastManager.show(R.string.toast_pwd_inconsistent);
				return;
			}
			
			pushEvent(JHEventCode.HTTP_Register, user,pwd,name,
					null,region,mobilephone,fixphone);
		}else if(id == R.id.tvRegion){
			if(mRequestCodeRegion == 0){
				mRequestCodeRegion = generateRequestCode();
			}
			ChooseRegionActivity.launchForResult(this, mRequestCodeRegion);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(requestCode == mRequestCodeRegion){
				final String region = data.getStringExtra(
						ChooseRegionActivity.EXTRA_RETURN_REGION);
				mTextViewRegion.setText(region);
			}
		}
	}
}
