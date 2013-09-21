package com.xbcx.jianhua;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class BaseInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public static final int ROLE_INNER 	= 0;
	public static final int ROLE_NORMAL = 1;

	private String 	mUser;
	private String 	mName;
	private String 	mIMUser;
	private String 	mIMPwd;
	private String 	mAvatarUrl;
	private String 	mDepartment;
	private int		mRole;
	private String	mMobilePhone;
	private String	mFixPhone;
	private String	mEmail;
	
	public BaseInfo(JSONObject jo) throws JSONException{
		mUser = jo.getString("user");
		mName = jo.getString("name");
		mIMUser = jo.getString("imuser");
		mIMPwd = jo.getString("impwd");
		mAvatarUrl = jo.getString("avatar");
		mDepartment = jo.getString("department");
		if(jo.has("role")){
			mRole = jo.getInt("role");
		}else{
			mRole = ROLE_NORMAL;
		}
		mMobilePhone = jo.getString("phone");
		mFixPhone = jo.getString("tel");
		mEmail = jo.getString("email");
	}

	public String getUser() {
		return mUser;
	}

	public String getName() {
		return mName;
	}

	public String getIMUser() {
		return mIMUser;
	}

	public String getIMPwd() {
		return mIMPwd;
	}

	public String getAvatarUrl() {
		return mAvatarUrl;
	}

	public String getDepartment() {
		return mDepartment;
	}

	public int getRole() {
		return mRole;
	}

	public String getMobilePhone() {
		return mMobilePhone;
	}
	public String getFixPhone() {
		return mFixPhone;
	}

	public String getEmail() {
		return mEmail;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public void setAvatarUrl(String mAvatarUrl) {
		this.mAvatarUrl = mAvatarUrl;
	}

	public void setDepartment(String mDepartment) {
		this.mDepartment = mDepartment;
	}

	public void setMobilePhone(String mMobilePhone) {
		this.mMobilePhone = mMobilePhone;
	}

	public void setFixPhone(String mFixPhone) {
		this.mFixPhone = mFixPhone;
	}

	public void setEmail(String mEmail) {
		this.mEmail = mEmail;
	}
	
}
