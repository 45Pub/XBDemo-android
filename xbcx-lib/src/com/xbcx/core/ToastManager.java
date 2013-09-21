package com.xbcx.core;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class ToastManager {

	private static ToastManager sInstance;
	
	private Toast sToastLast;
	private int sResIdLast;
	private long sShowTimeLast;
	
	private Context mContext;
	private final Handler mHandler;
	
	public static ToastManager getInstance(Context context){
		if(sInstance == null){
			sInstance = new ToastManager();
		}
		sInstance.mContext = context;
		return sInstance;
	}
	
	private ToastManager(){
		mHandler = new Handler();
	}
	
	private Runnable mRunnable = new Runnable() {
		public void run() {
			sToastLast = Toast.makeText(mContext, sResIdLast, Toast.LENGTH_SHORT);
			sToastLast.show();
			sShowTimeLast = System.currentTimeMillis();
		}
	};
	
	public void show(int nResId){
		if(nResId == sResIdLast){
			if(System.currentTimeMillis() - sShowTimeLast < 2000){
				return;
			}
		}
		if(sToastLast != null){
			sToastLast.cancel();
		}
		
		sResIdLast = nResId;
		mHandler.removeCallbacks(mRunnable);
		mHandler.post(mRunnable);
	}
	
	public void show(final String strText){
		mHandler.post(new Runnable() {
			public void run() {
				sToastLast = Toast.makeText(mContext, strText, Toast.LENGTH_LONG);
				sToastLast.show();
				sShowTimeLast = System.currentTimeMillis();
			}
		});
	}
}
