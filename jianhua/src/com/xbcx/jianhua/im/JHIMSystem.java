package com.xbcx.jianhua.im;

import com.xbcx.im.XIMSystem;

public class JHIMSystem extends XIMSystem {

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	protected void onLoginGet() throws Exception {
		super.onLoginGet();
		try{
			loadBlackList();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
