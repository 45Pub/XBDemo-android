package com.xbcx.im;

import com.xbcx.core.SharedPreferenceDefine;

import android.content.Context;
import android.content.SharedPreferences;

public class IMConfigManager extends IMModule{

	public static IMConfigManager getInstance(){
		return sInstance;
	}
	
	private static IMConfigManager sInstance;
	
	private Context	mContext;
	
	private boolean mIsReceiveNewMessageNotify;
	
	private boolean mIsReceiveNewMessageSoundNotify;
	private boolean mIsReceiveNewMessageVibrateNotify;
	
	IMConfigManager(){
		sInstance = this;
	}
	
	@Override
	protected void onInitial(IMKernel kernel) {
		mContext = kernel.getContext();
		SharedPreferences sp = getConfigSharedPreferences();
		mIsReceiveNewMessageNotify = sp.getBoolean(SharedPreferenceDefine.KEY_RECEIVENOTIFY, true);
		mIsReceiveNewMessageSoundNotify = sp.getBoolean(SharedPreferenceDefine.KEY_RECEIVESOUNDNOTIFY, true);
		mIsReceiveNewMessageVibrateNotify = sp.getBoolean(SharedPreferenceDefine.KEY_RECEIVEVIBRATENOTIFY, true);
	}

	@Override
	protected void onRelease() {
		
	}
	
	public boolean isReceiveNewMessageNotify(){
		return mIsReceiveNewMessageNotify;
	}
	
	public boolean isReceiveNewMessageSoundNotify(){
		return mIsReceiveNewMessageSoundNotify;
	}
	
	public boolean isReceiveNewMessageVibrateNotify(){
		return mIsReceiveNewMessageVibrateNotify;
	}
	
	public void	setReceiveNewMessageNotify(boolean bNotify){
		mIsReceiveNewMessageNotify = bNotify;
		getConfigSharedPreferences().edit()
		.putBoolean(SharedPreferenceDefine.KEY_RECEIVENOTIFY, bNotify)
		.commit();
	}
	
	public void setReceiveNewMessageSoundNotify(boolean bNotify){
		mIsReceiveNewMessageSoundNotify = bNotify;
		getConfigSharedPreferences().edit()
		.putBoolean(SharedPreferenceDefine.KEY_RECEIVESOUNDNOTIFY, bNotify)
		.commit();
	}
	
	public void setReceiveNewMessageVibrateNotify(boolean bNotify){
		mIsReceiveNewMessageVibrateNotify = bNotify;
		getConfigSharedPreferences().edit()
		.putBoolean(SharedPreferenceDefine.KEY_RECEIVEVIBRATENOTIFY, bNotify)
		.commit();
	}
	
	private SharedPreferences getConfigSharedPreferences(){
		return mContext.getSharedPreferences(SharedPreferenceDefine.SP_CONFIG, Context.MODE_PRIVATE);
	}
}

