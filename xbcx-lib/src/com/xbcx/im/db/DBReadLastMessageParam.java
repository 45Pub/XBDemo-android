package com.xbcx.im.db;

import java.util.HashMap;
import java.util.Map;

import com.xbcx.im.XMessage;

public class DBReadLastMessageParam {
	public String 				mId;
	
	public String				mColumnNames[];
	
	public boolean				mHasValue;
	
	public boolean				mSetMessage;
	public XMessage				mMessageOut;
	
	public Map<String,String> 	mMapColumnNameToValue = new HashMap<String, String>();
	
	public int getIntValue(String strColumnName,int nDefaultValue){
		int nRet = nDefaultValue;
		try{
			nRet = Integer.parseInt(mMapColumnNameToValue.get(strColumnName));
		}catch(Exception e){
			e.printStackTrace();
		}
		return nRet;
	}
	
	public long getLongValue(String strColumnName,long lDefaultValue){
		long lRet = lDefaultValue;
		try{
			lRet = Long.parseLong(mMapColumnNameToValue.get(strColumnName));
		}catch(Exception e){
			e.printStackTrace();
		}
		return lRet;
	}
	
	public String getStringValue(String strColumnName,String strDefaultValue){
		String strRet = mMapColumnNameToValue.get(strColumnName);
		if(strRet == null){
			strRet = strDefaultValue;
		}
		return strRet;
	}
}
