package com.xbcx.core;

import android.content.Context;
import android.view.View;

public abstract class BaseUIFactory {
	
	protected Context	mContext;
	
	public BaseUIFactory(Context context){
		mContext = context;
	}
	
	public abstract View 	createTitleBackButton();
	 
	public abstract int		getTitleBackButtonLeftMargin();
	 
	public abstract int		getTitleBackButtonTopMargin();
	 
	public abstract View 	createTitleRightImageButton(int resId);
	 
	public abstract int		getTitleRightImageButtonRightMargin();
	 
	public abstract int		getTitleRightImageButtonTopMargin();
	 
	public abstract View 	createTitleRightTextButton(int textId);
	 
	public abstract int		getTitleRightTextButtonRightMargin();
	 
	public abstract int		getTitleRightTextButtonTopMargin();
}
