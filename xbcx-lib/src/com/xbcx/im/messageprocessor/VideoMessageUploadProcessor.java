package com.xbcx.im.messageprocessor;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventManager.OnEventRunner;
import com.xbcx.im.XMessage;

public class VideoMessageUploadProcessor extends MessageUploadProcessor implements 
																		OnEventRunner{

	public static VideoMessageUploadProcessor getInstance(){
		return sInstance;
	}
	
	private static VideoMessageUploadProcessor sInstance;
	
	protected Handler mHandler = new Handler(Looper.getMainLooper());
	
	protected VideoMessageUploadProcessor(){
		sInstance = this;
		AndroidEventManager.getInstance().registerEventRunner(EventCode.UploadChatVideo, this);
	}
	
	@Override
	public void requestUpload(XMessage m) {
		if(m.getType() == XMessage.TYPE_VIDEO){
			UploadInfo ui = new UploadInfo(m);
			mMapIdToUploadInfo.put(m.getId(), ui);
			AndroidEventManager.getInstance().pushEvent(EventCode.UploadChatVideo, m);
		}
	}
	
	@Override
	public void onEventRun(Event event) throws Exception {
		doUpload(event);
	}
	
	@Override
	protected boolean onUpload(XMessage xm, UploadInfo ui) throws Exception {
		final String thumbType = getUploadThumbType(xm);
		if(!TextUtils.isEmpty(thumbType)){
			Event e = AndroidEventManager.getInstance().runEvent(
					EventCode.HTTP_PostFile,
					thumbType,xm.getVideoThumbFilePath());
			if(e.isSuccess()){
				xm.setVideoThumbDownloadUrl((String)e.getReturnParamAtIndex(0));
				final String type = getUploadType(xm);
				if(!TextUtils.isEmpty(type)){
					e = AndroidEventManager.getInstance().runEvent(
							EventCode.HTTP_PostFile, type,xm.getVideoFilePath(),
							ui.mRunnable,mHandler,ui.mCancel);
					if(e.isSuccess()){
						xm.setVideoDownloadUrl((String)e.getReturnParamAtIndex(0));
						return true;
					}
				}
			}
		}
		
		return false;
	}

	@Override
	protected void onPercentageChanged(UploadInfo ui) {
		AndroidEventManager.getInstance().runEvent(
				EventCode.UploadChatVideoPercentChanged,
				ui.mMessage);
	}

	@Override
	protected String getUploadType(XMessage xm) {
		return null;
	}
	
	protected String getUploadThumbType(XMessage xm){
		return null;
	}
}
