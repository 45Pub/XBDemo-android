package com.xbcx.jianhua.httprunner;

import java.util.Locale;

import com.xbcx.core.Event;
import com.xbcx.jianhua.BaseInfo;
import com.xbcx.jianhua.URLUtils;

public class ChangeUserInfoRunner extends HttpRunner {

	@Override
	public void onEventRun(Event event) throws Exception {
		final BaseInfo bi = (BaseInfo)event.getParamAtIndex(0);
		String url = String.format(Locale.getDefault(), URLUtils.ChangeUserInfo, 
				bi.getName(),bi.getMobilePhone(),bi.getFixPhone(),
				bi.getEmail(),bi.getAvatarUrl());
		if(bi.getRole() == BaseInfo.ROLE_NORMAL){
			url += ("&area=" + bi.getDepartment());
		}
		doGet(url);
		event.setSuccess(true);
	}

}
