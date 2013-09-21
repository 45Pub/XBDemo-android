package com.xbcx.jianhua.httprunner;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;

import android.os.Handler;

import com.xbcx.core.Event;
import com.xbcx.jianhua.URLUtils;
import com.xbcx.utils.HttpUtils.ProgressRunnable;

public class PostFileRunner extends HttpRunner {

	@Override
	public void onEventRun(Event event) throws Exception {
		final String type = (String)event.getParamAtIndex(0);
		final String filePath = (String)event.getParamAtIndex(1);
		final ProgressRunnable run = (ProgressRunnable)event.getParamAtIndex(2);
		final Handler handler = (Handler)event.getParamAtIndex(3);
		final AtomicBoolean	cancel = (AtomicBoolean)event.getParamAtIndex(4);
		HashMap<String, String> mapValues = new HashMap<String, String>();
		mapValues.put("type", type);
		HashMap<String, String> mapFiles = new HashMap<String, String>();
		mapFiles.put("upfile", filePath);
		JSONObject jo = doPost(URLUtils.PostFile, mapValues, mapFiles,
				run,handler,cancel);
		final String url = jo.getString("url");
		String thumburl = null;
		if(jo.has("thumurl")){
			thumburl = jo.getString("thumurl");
		}
		event.addReturnParam(url);
		event.addReturnParam(thumburl);
		event.setSuccess(true);
	}

}
