package com.xbcx.jianhua.httprunner;

import java.net.URLEncoder;
import java.util.Locale;

import org.json.JSONObject;

import com.xbcx.core.Event;
import com.xbcx.jianhua.BaseInfo;
import com.xbcx.jianhua.JHApplication;
import com.xbcx.jianhua.URLUtils;
import com.xbcx.utils.Encrypter;

public class LoginRunner extends HttpRunner {

	@SuppressWarnings("deprecation")
	@Override
	public void onEventRun(Event event) throws Exception {
		mAddUserParam = false;
		final String user = (String)event.getParamAtIndex(0);
		final String pwd = (String)event.getParamAtIndex(1);
		JSONObject jo = doGet(String.format(
				Locale.getDefault(), URLUtils.Login, 
				URLEncoder.encode(user + "-" + Encrypter.encryptBySHA1(user + JHApplication.KEY_HTTP)),
				Encrypter.encryptByMD5(pwd)));
		event.addReturnParam(new BaseInfo(jo));
		event.setSuccess(true);
	}

}
