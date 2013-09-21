package com.xbcx.jianhua.httprunner;

import java.util.HashMap;

import org.json.JSONObject;

import com.xbcx.core.Event;
import com.xbcx.jianhua.BaseInfo;
import com.xbcx.jianhua.JHApplication;
import com.xbcx.jianhua.URLUtils;
import com.xbcx.utils.Encrypter;

public class RegisterRunner extends HttpRunner {

	@Override
	public void onEventRun(Event event) throws Exception {
		mAddUserParam = false;
		final String user = (String)event.getParamAtIndex(0);
		final String pwd = (String)event.getParamAtIndex(1);
		final String name = (String)event.getParamAtIndex(2);
		final String region = (String)event.getParamAtIndex(4);
		final String mobilephone = (String)event.getParamAtIndex(5);
		final String fixphone = (String)event.getParamAtIndex(6);
		HashMap<String, String> mapValues = new HashMap<String, String>();
		mapValues.put("user", user + "-" + Encrypter.encryptBySHA1(user + JHApplication.KEY_HTTP));
		mapValues.put("pwd", Encrypter.encryptByMD5(pwd));
		mapValues.put("name", name);
		mapValues.put("department",region);
		mapValues.put("phone", mobilephone);
		mapValues.put("tel", fixphone);
		mapValues.put("email", user);
		
		JSONObject jo = doPost(URLUtils.Register, mapValues);
		event.addReturnParam(new BaseInfo(jo));
		event.setSuccess(true);
	}

}
