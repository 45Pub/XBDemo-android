package com.xbcx.im.ui;


import java.util.Collection;
import java.util.Collections;

import com.xbcx.im.XMessage;

import android.view.View;
import android.view.ViewGroup;

public abstract class IMMessageViewProvider {
	
	protected OnViewClickListener mOnViewClickListener;
	
	private static IMMessageViewProviderFactory sProviderFactory = new IMMessageViewProviderFactory();
	
	protected boolean			mIsCheck;
	
	protected IMMessageAdapter	mAdapter;
	
	public void setIsCheck(boolean bCheck){
		mIsCheck = bCheck;
	}
	
	public Collection<XMessage> getCheckedMessage(){
		return Collections.emptySet();
	}
	
	public void setCheckItem(XMessage xm,boolean bCheck){
	}
	
	public boolean isCheckedItem(XMessage xm){
		return false;
	}
	
	public void setIMMessageAdapter(IMMessageAdapter adapter){
		mAdapter = adapter;
	}
	
	public void notifyDataSetChanged(){
		if(mAdapter != null){
			mAdapter.notifyDataSetChanged();
		}
	}
	
	protected void onCheckChanged(XMessage xm){
		if(mAdapter != null){
			mAdapter.onCheckChanged(xm);
		}
	}
	
	public abstract boolean acceptHandle(XMessage message);
	
	public abstract View 	getView(XMessage message,View convertView,ViewGroup parent);
	
	public void setOnViewClickListener(OnViewClickListener listener){
		mOnViewClickListener = listener;
	}
	
	public static void setIMMessageViewProviderFactory(IMMessageViewProviderFactory factory){
		sProviderFactory = factory;
	}
	
	public static IMMessageViewProviderFactory getIMMessageViewProviderFactory(){
		return sProviderFactory;
	}

	public static interface OnViewClickListener{
		
		public void 	onViewClicked(XMessage message,int nViewId);
		
		public boolean 	onViewLongClicked(XMessage message,int nViewId);
	}
}
