package com.xbcx.core;

import com.xbcx.core.EventManager.OnEventRunner;
import com.xbcx.utils.HttpUtils;

public class HttpDownloadRunner implements OnEventRunner {

	@Override
	public void onEventRun(Event event) throws Exception {
		final String url = (String)event.getParamAtIndex(0);
		final String filePath = (String)event.getParamAtIndex(1);
		event.setSuccess(doDownload(url, filePath));
	}

	protected boolean doDownload(String url,String filePath){
		return HttpUtils.doDownload(url, filePath, true, null, null, null);
	}
}
