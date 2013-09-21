package com.xbcx.jianhua.httprunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import com.xbcx.core.Event;
import com.xbcx.jianhua.Departmember;
import com.xbcx.jianhua.URLUtils;
import com.xbcx.jianhua.im.JHVCardProvider;

public class GetOrgRunner extends HttpRunner {

	@Override
	public void onEventRun(Event event) throws Exception {
		final String id = (String)event.getParamAtIndex(0);
		JSONObject jo = doGet(String.format(Locale.getDefault(), 
				URLUtils.GetOrg, id == null ? "0" : id));
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
		
		for(Departmember dm : dms){
			if(dm.isUser()){
				JHVCardProvider.getInstance().saveInfo(
						dm.getId(), dm.getName(), dm.getAvatar());
			}
		}
		
		event.addReturnParam(dms);
		event.setSuccess(true);
	}

}
