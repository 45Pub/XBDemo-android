package com.xbcx.jianhua;

import org.json.JSONException;
import org.json.JSONObject;

import com.xbcx.core.NameObject;
import com.xbcx.utils.Encrypter;

public class Departmember extends NameObject{
	
	private static final long serialVersionUID = 1L;
	
	private boolean mIsUser;
	private String	mAvatarUrl;
	
	public Departmember(JSONObject jo,boolean bUser) throws JSONException{
		super(bUser ? Encrypter.encryptBySHA1(jo.getString("user") + JHApplication.KEY_HTTP) : jo.getString("id"));
		mName = jo.getString("name");
		if(bUser){
			mAvatarUrl = jo.getString("avatar");
		}
		mIsUser = bUser;
	}

	public boolean isUser(){
		return mIsUser;
	}
	
	public String getAvatar(){
		return mAvatarUrl;
	}
}
