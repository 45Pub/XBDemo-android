package com.xbcx.jianhua.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.xbcx.core.Event;
import com.xbcx.im.ui.XBaseActivity;
import com.xbcx.jianhua.JHEventCode;
import com.xbcx.jianhua.R;

public abstract class BaseEditActivity extends XBaseActivity implements View.OnClickListener{
	
	protected EditText	mEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addTextButtonInTitleRight(R.string.save);
		
		mEditText = (EditText)findViewById(R.id.et);
		findViewById(R.id.ivClear).setOnClickListener(this);
		
		mEditText.setText(getIntent().getStringExtra("def"));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
		ba.mActivityLayoutId = R.layout.activity_edit;
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if(id == R.id.ivClear){
			mEditText.setText(null);
		}
	}

	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == JHEventCode.HTTP_ChangeUserInfo){
			if(event.isSuccess()){
				finish();
			}
		}
	}

	@Override
	protected void onTitleRightButtonClicked(View v) {
		super.onTitleRightButtonClicked(v);
		final String text = mEditText.getText().toString();
		if(!TextUtils.isEmpty(text)){
			onSaveChange(text);
		}
	}
	
	protected abstract void onSaveChange(String text);
}
