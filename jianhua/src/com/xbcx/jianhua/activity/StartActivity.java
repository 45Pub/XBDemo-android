package com.xbcx.jianhua.activity;

import com.xbcx.im.IMKernel;

import android.app.Activity;
import android.os.Bundle;

public class StartActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(IMKernel.canLogin(this)){
			MainActivity.launch(this);
		}else{
			LoginActivity.launch(this);
		}
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
