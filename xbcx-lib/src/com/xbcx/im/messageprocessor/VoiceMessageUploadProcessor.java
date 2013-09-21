package com.xbcx.im.messageprocessor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.core.EventManager.OnEventRunner;
import com.xbcx.im.IMKernel;
import com.xbcx.im.IMModule;
import com.xbcx.im.XMessage;
import com.xbcx.utils.HttpUtils;

public class VoiceMessageUploadProcessor extends IMModule 
											implements OnEventListener,OnEventRunner{

	public static VoiceMessageUploadProcessor getInstance(){
		return sInstance;
	}
	
	private static VoiceMessageUploadProcessor sInstance;
	
	private List<XMessage> mListMessageWaitUpload = new LinkedList<XMessage>();
	
	private HashMap<String, String> mMapIdCancel = new HashMap<String, String>();
	
	private XMessage	mUploadingMessage;
	
	protected VoiceMessageUploadProcessor(){
		sInstance = this;
		AndroidEventManager.getInstance().registerEventRunner(EventCode.UploadChatVoice, this);
	}
	
	public void 	clearWaitUpload(){
		mListMessageWaitUpload.clear();
	}
	
	public boolean	isUploading(XMessage m){
		if(m.equals(mUploadingMessage) || 
				mListMessageWaitUpload.contains(m)){
			return true;
		}
		return false;
	}
	
	public void requestUpload(XMessage m) {
		if(m.getType() == XMessage.TYPE_VOICE){
			if(mUploadingMessage == null){
				mUploadingMessage = m;
				AndroidEventManager.getInstance().pushEvent(EventCode.UploadChatVoice, m);
			}else{
				mListMessageWaitUpload.add(m);
			}
		}
	}
	
	public void stopUpload(XMessage m){
		mListMessageWaitUpload.remove(m);
		if(m != null && m.equals(mUploadingMessage)){
			mMapIdCancel.put(m.getId(), m.getId());
		}
	}
	
	@Override
	public void onEventRun(Event event) throws Exception {
		XMessage xm = (XMessage)event.getParamAtIndex(0);
		
		Map<String, String> mapNameValues = new HashMap<String, String>();
		if(xm.isFromGroup()){
			mapNameValues.put("type", "10");
		}else{
			mapNameValues.put("type", "0");
		}
		Map<String, String> mapFilePaths = new HashMap<String, String>();
		mapFilePaths.put("upfile", xm.getVoiceFilePath());
		
		final String ret = HttpUtils.doPost(
				IMKernel.getInstance().getUploadVoiceUrl(), 
				mapNameValues,mapFilePaths);
		JSONObject jo = new JSONObject(ret);
		if("true".equals(jo.getString("ok"))){
			xm.setVoiceDownloadUrl(jo.getString("url"));
			event.setSuccess(true);
		}
	}

	@Override
	public void onEventRunEnd(Event event) {
		final int nCode = event.getEventCode();
		if(nCode == EventCode.UploadChatVoice ){
			final String strId = mMapIdCancel.remove(mUploadingMessage.getId());
			
			if(event.isSuccess()){
				if(strId == null){
					mUploadingMessage.setUploadSuccess(true);
					mUploadingMessage.setSendSuccess(true);
					mUploadingMessage.updateDB();
				
					AndroidEventManager.getInstance().pushEvent(
							EventCode.IM_SendMessage,mUploadingMessage);
				}
			}
			
			mUploadingMessage = null;
			
			if(mListMessageWaitUpload.size() > 0){
				XMessage m = mListMessageWaitUpload.get(0);
				mListMessageWaitUpload.remove(m);
				
				requestUpload(m);
			}
		}
	}

	@Override
	protected void onInitial(IMKernel kernel) {
		AndroidEventManager.getInstance().addEventListener(EventCode.UploadChatVoice, this, false);
	}

	@Override
	protected void onRelease() {
		AndroidEventManager.getInstance().removeEventListener(EventCode.UploadChatVoice, this);
	}
}
