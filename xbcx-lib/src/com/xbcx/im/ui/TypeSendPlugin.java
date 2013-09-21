package com.xbcx.im.ui;

import android.app.Activity;
import android.content.Intent;

import com.xbcx.im.XMessage;

public class TypeSendPlugin implements SendPlugin {
	
	protected int	mType;
	
	public TypeSendPlugin(int type){
		mType = type;
	}

	@Override
	public int getSendType() {
		return mType;
	}

	@Override
	public int getLaunchActivityRequestCode() {
		return 0;
	}

	@Override
	public void startActivityForResult(Activity activity, int requestCode) {
	}

	@Override
	public XMessage onActivityResult(int resultCode, int requestCode, Intent data) {
		return null;
	}

	@Override
	public XMessage onCreateXMessage() {
		return null;
	}

	@Override
	public boolean isHideEditPullUpView() {
		return true;
	}

}
