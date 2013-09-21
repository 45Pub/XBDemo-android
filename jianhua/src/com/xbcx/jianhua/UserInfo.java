package com.xbcx.jianhua;

import org.json.JSONException;
import org.json.JSONObject;

import com.xbcx.core.NameObject;

public class UserInfo extends NameObject {

	private static final long serialVersionUID = 1L;
	
	private String mUser;
	private String mAvatarUrl;
	private String mDepartment;
	private String mMobilePhone;
	private String mFixPhone;
	private String mEmail;

	public UserInfo(String id,JSONObject jo) throws JSONException {
		super(id);
		mName = jo.getString("name");
		mUser = jo.getString("user");
		mAvatarUrl = jo.getString("avatar");
		mDepartment = jo.getString("department");
		mMobilePhone = jo.getString("phone");
		mFixPhone = jo.getString("tel");
		mEmail = jo.getString("email");
	}

	public String getUser() {
		return mUser;
	}

	public String getAvatarUrl() {
		return mAvatarUrl;
	}

	public String getDepartment() {
		return mDepartment;
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

}
