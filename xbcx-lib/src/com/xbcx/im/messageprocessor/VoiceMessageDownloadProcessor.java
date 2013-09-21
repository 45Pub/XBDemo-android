package com.xbcx.im.messageprocessor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.core.EventManager.OnEventRunner;
import com.xbcx.im.IMKernel;
import com.xbcx.im.IMModule;
import com.xbcx.im.XMessage;
import com.xbcx.utils.FileHelper;
import com.xbcx.utils.HttpUtils;

public class VoiceMessageDownloadProcessor extends IMModule implements OnEventListener,
														OnEventRunner{
	
	public static VoiceMessageDownloadProcessor getInstance(){
		return sInstance;
	}
	
	private static VoiceMessageDownloadProcessor sInstance;
	
	private List<XMessage> mListWaitDownloadMessage = new LinkedList<XMessage>();
	private HashMap<String, AtomicBoolean> mMapIdToCancel = new HashMap<String, AtomicBoolean>();
	
	private XMessage mDownloadingMessage;
	
	protected VoiceMessageDownloadProcessor(){
		sInstance = this;
		AndroidEventManager.getInstance().registerEventRunner(EventCode.DownloadChatVoice, this);
	}
	
	public void clearWaitDownload(){
		mListWaitDownloadMessage.clear();
	}
	
	public boolean isDownloading(XMessage m){
		if(m.equals(mDownloadingMessage) || mListWaitDownloadMessage.contains(m)){
			return true;
		}
		return false;
	}
	
	public void requestDownload(XMessage m){
		if(m.getType() == XMessage.TYPE_VOICE){
			if(mDownloadingMessage == null){
				mDownloadingMessage = m;
				
				AtomicBoolean cancel = new AtomicBoolean(false);
				mMapIdToCancel.put(m.getId(), cancel);
				
				AndroidEventManager.getInstance().pushEvent(EventCode.DownloadChatVoice, m,cancel);
			}else{
				mListWaitDownloadMessage.add(m);
			}
		}
	}
	
	public void stopDownload(XMessage m){
		mListWaitDownloadMessage.remove(m);
		AtomicBoolean bCancel = mMapIdToCancel.get(m.getId());
		if(bCancel != null){
			bCancel.set(true);
		}
	}
	
	public void stopAllDownload(XMessage m){
		mListWaitDownloadMessage.clear();
		for(AtomicBoolean cancel : mMapIdToCancel.values()){
			cancel.set(true);
		}
	}

	@Override
	public void onEventRun(Event event) throws Exception {
		final int code = event.getEventCode();
		if(code == EventCode.DownloadChatVoice){
			XMessage xm = (XMessage)event.getParamAtIndex(0);
			AtomicBoolean cancel = (AtomicBoolean)event.getParamAtIndex(1);
			event.setSuccess(HttpUtils.doDownload(xm.getVoiceDownloadUrl(),
					xm.getVoiceFilePath(), null, null, cancel));
			if(!cancel.get()){
				xm.setDownloaded();
				xm.updateDB();
			}
		}
	}

	@Override
	public void onEventRunEnd(Event event) {
		final int nCode = event.getEventCode();
		if(nCode == EventCode.DownloadChatVoice){
			XMessage xm = (XMessage)event.getParamAtIndex(0);
			if(mDownloadingMessage != null && 
					mDownloadingMessage.equals(xm)){
				
				AtomicBoolean bCancel = mMapIdToCancel.remove(
						mDownloadingMessage.getId());
				if(bCancel != null && bCancel.get()){
					FileHelper.deleteFile(mDownloadingMessage.getVoiceFilePath());
				}
				
				mDownloadingMessage = null;
			
				if(mListWaitDownloadMessage.size() > 0){
					XMessage m = mListWaitDownloadMessage.get(0);
					mListWaitDownloadMessage.remove(m);
				
					requestDownload(m);
				}
			}
		}
	}

	@Override
	protected void onInitial(IMKernel kernel) {
		AndroidEventManager.getInstance().addEventListener(EventCode.DownloadChatVoice, this, false);
	}

	@Override
	protected void onRelease() {
		AndroidEventManager.getInstance().removeEventListener(EventCode.DownloadChatVoice, this);
	}
}
