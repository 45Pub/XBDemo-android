package com.xbcx.im;

public abstract class IMModule {

	protected boolean mIsInitial;
	
	void initial(IMKernel kernel){
		if(mIsInitial){
			release();
		}
		onInitial(kernel);
		mIsInitial = true;
	}
	
	void release(){
		onRelease();
		mIsInitial = false;
	}
	
	protected abstract void onInitial(IMKernel kernel);
	
	protected abstract void onRelease();
}
