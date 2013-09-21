package com.xbcx.jianhua.httprunner;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;

import android.os.Handler;
import android.text.TextUtils;

import com.xbcx.core.ToastManager;
import com.xbcx.core.XApplication;
import com.xbcx.core.EventManager.OnEventRunner;
import com.xbcx.jianhua.JHApplication;
import com.xbcx.jianhua.R;
import com.xbcx.utils.Encrypter;
import com.xbcx.utils.HttpUtils;
import com.xbcx.utils.HttpUtils.ProgressRunnable;
import com.xbcx.utils.SystemUtils;

public abstract class HttpRunner implements OnEventRunner {

	protected boolean 	mIsNetSuccess; 
	protected boolean 	mIsRequestSuccess;
	
	protected String	mHttpRetString;
	
	protected String	mUrl;
	
	protected boolean	mAddUserParam = true;
	
	public boolean 	isNetSuccess(){
		return mIsNetSuccess;
	}
	
	public boolean 	isRequestSuccess(){
		return mIsRequestSuccess;
	}
	
	public String	getHttpRetString(){
		return mHttpRetString;
	}
	
	public final String	getUrl(){
		return mUrl;
	}
	
	protected String getUrlParam(String strUrl,String strParamName){
		int nIndexStart = strUrl.indexOf("&" + strParamName);
		if(nIndexStart >= 0){
			nIndexStart = strUrl.indexOf("=", nIndexStart) + 1;
			int nEnd = strUrl.indexOf("&",nIndexStart);
			if(nEnd == -1){
				nEnd = strUrl.length();
			}
			return strUrl.substring(nIndexStart, nEnd);
		}
		return "";
	}
	
	protected boolean checkRequestSuccess(JSONObject jsonObject){
		try{
			mIsRequestSuccess = "true".equals(jsonObject.getString("ok"));
		}catch(Exception e){
			e.printStackTrace();
			mIsRequestSuccess = false;
		}
		return mIsRequestSuccess;
	}
	
	protected boolean checkRequestSuccess(String strJson){
		try{
			JSONObject jsonObject = new JSONObject(strJson);
			mIsRequestSuccess = "true".equals(jsonObject.getString("ok"));
		}catch(Exception e){
			e.printStackTrace();
			mIsRequestSuccess = false;
		}
		return mIsRequestSuccess;
	}
	
	protected JSONObject doGet(String url) throws Exception{
		mUrl = addUrlCommonParams(url);
		XApplication.getLogger().info(getClass().getName() + " execute url = " + mUrl);
		mHttpRetString = HttpUtils.doGetString(mUrl);
		return onHandleHttpRet(mHttpRetString);
	}
	
	protected JSONObject doPost(String url,HashMap<String, String> mapValues) throws Exception{
		return doPost(url, mapValues, null);
	}
	
	protected JSONObject doPost(String url,
			HashMap<String, String> mapValues,
			HashMap<String, String> mapFiles) throws Exception{
		mUrl = addUrlCommonParams(url);
		XApplication.getLogger().info(getClass().getName() + " execute url = " + mUrl);
		mHttpRetString = HttpUtils.doPost(mUrl, mapValues, mapFiles);
		return onHandleHttpRet(mHttpRetString);
	}
	
	protected JSONObject doPost(String url,
			HashMap<String, String> mapValues,
			HashMap<String, String> mapFiles,
			ProgressRunnable pr,Handler h,AtomicBoolean cancel) throws Exception{
		mUrl = addUrlCommonParams(url);
		XApplication.getLogger().info(getClass().getName() + " execute url = " + mUrl);
		mHttpRetString = HttpUtils.doPost(mUrl, mapValues, mapFiles,pr,h,cancel);
		return onHandleHttpRet(mHttpRetString);
	}
	
	protected JSONObject onHandleHttpRet(String ret) throws Exception{
		XApplication.getLogger().info(getClass().getName() + " ret:" + ret);
		if(TextUtils.isEmpty(mHttpRetString)){
			mIsNetSuccess = false;
			mIsRequestSuccess = false;
			ToastManager.getInstance(XApplication.getApplication())
			.show(R.string.toast_disconnect);
			throw new Exception();
		}else{
			JSONObject jo = new JSONObject(mHttpRetString);
			mIsNetSuccess = true;
			if(!checkRequestSuccess(jo)){
				final String error = jo.getString("error");
				XApplication.getLogger().info("error:" + error);
				ToastManager.getInstance(XApplication.getApplication()).show(error);
				throw new Exception();
			}
			return jo;
		}
	}

	@SuppressWarnings("deprecation")
	protected String addUrlCommonParams(String url){
		StringBuilder buf = new StringBuilder(url);
		final String strDeviceUUID = Encrypter.encryptBySHA1(
				SystemUtils.getDeviceUUID(XApplication.getApplication()));
		final String strUser = JHApplication.getUser();
		final long lTime = System.currentTimeMillis() / 1000;
		
		buf.append("&device=android")
		.append("&ver=").append(SystemUtils.getVersionName(XApplication.getApplication()))
		.append("&deviceuuid=")
		.append(strDeviceUUID + "-" + Encrypter.encryptBySHA1(strDeviceUUID + JHApplication.KEY_HTTP));
		if(mAddUserParam && !TextUtils.isEmpty(strUser)){
			buf.append("&user=")
			.append(URLEncoder.encode(strUser + "-" + Encrypter.encryptBySHA1(strUser + JHApplication.KEY_HTTP)));
		}
		buf.append("&timesign=")
		.append(lTime + "-" + Encrypter.encryptBySHA1(lTime + JHApplication.KEY_HTTP));
		
		buf.append("&width=").append(XApplication.getScreenWidth())
		.append("&height=").append(XApplication.getScreenHeight())
		.append("&dpi=").append(XApplication.getScreenDpi());
		
		return buf.toString();
	}
}
