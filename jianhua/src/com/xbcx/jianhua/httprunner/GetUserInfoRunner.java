package com.xbcx.jianhua.httprunner;

import java.util.Locale;

import org.json.JSONObject;

import com.xbcx.core.Event;
import com.xbcx.jianhua.URLUtils;
import com.xbcx.jianhua.UserInfo;

public class GetUserInfoRunner extends HttpRunner {

	@Override
	public void onEventRun(Event event) throws Exception {
		final String id = (String)event.getParamAtIndex(0);
		JSONObject jo = doGet(String.format(Locale.getDefault(),
				URLUtils.GetUserInfo, id));
		event.addReturnParam(new UserInfo(id, jo));
		event.setSuccess(true);
	}

}
