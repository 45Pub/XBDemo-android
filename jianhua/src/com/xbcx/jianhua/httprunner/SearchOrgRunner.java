package com.xbcx.jianhua.httprunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import com.xbcx.core.Event;
import com.xbcx.jianhua.Departmember;
import com.xbcx.jianhua.URLUtils;

public class SearchOrgRunner extends HttpRunner {

	@Override
	public void onEventRun(Event event) throws Exception {
		final String key = (String)event.getParamAtIndex(0);
		JSONObject jo = doGet(String.format(Locale.getDefault(), 
				URLUtils.SearchOrg, key));
		JSONArray ja = jo.getJSONArray("departments");
		int length = ja.length();
		List<Departmember> dms = new ArrayList<Departmember>();
		for(int index = 0;index < length;++index){
			dms.add(new Departmember(ja.getJSONObject(index), false));
		}
		
		ja = jo.getJSONArray("peoples");
		length = ja.length();
		for(int index = 0;index < length;++index){
			dms.add(new Departmember(ja.getJSONObject(index), true));
		}
		
		event.addReturnParam(dms);
		event.setSuccess(true);
	}

}
