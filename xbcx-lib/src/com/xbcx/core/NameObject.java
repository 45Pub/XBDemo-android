package com.xbcx.core;

import android.text.TextUtils;

public abstract class NameObject extends IDObject implements 
									Comparable<NameObject>{

	private static final long serialVersionUID = 1L;
	
	protected String mName;
	
	public NameObject(String id) {
		super(id);
	}

	public String getName(){
		return mName == null ? "" : mName;
	}
	
	@Override
	public int compareTo(NameObject another) {
		final String strNameR = another.getName();
		if(TextUtils.isEmpty(mName)){
			return -1;
		}
		if(TextUtils.isEmpty(strNameR)){
			return 1;
		}
		int nRet = mName.compareTo(strNameR);
		if(nRet == 0){
			nRet = -1;
		}
		return nRet;
	}
}
