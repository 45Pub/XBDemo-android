package com.xbcx.im.messageprocessor;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventManager.OnEventRunner;
import com.xbcx.im.XMessage;

public class PhotoMessageUploadProcessor extends MessageUploadProcessor implements 
																	OnEventRunner{
	public static PhotoMessageUploadProcessor getInstance(){
		return sInstance;
	}
	
	private static PhotoMessageUploadProcessor sInstance;
	
	protected Handler mHandler = new Handler(Looper.getMainLooper());
	
	protected PhotoMessageUploadProcessor(){
		sInstance = this;
		AndroidEventManager.getInstance().registerEventRunner(EventCode.UploadChatPhoto, this);
	}
	
	@Override
	public void requestUpload(XMessage m){
		if(m.getType() == XMessage.TYPE_PHOTO){
			if(!mMapIdToUploadInfo.containsKey(m.getId())){
				UploadInfo ui = new UploadInfo(m);
				mMapIdToUploadInfo.put(m.getId(), ui);
				AndroidEventManager.getInstance().pushEvent(EventCode.UploadChatPhoto, m);
			}
		}
	}

	@Override
	public void onEventRun(Event event) throws Exception {
		doUpload(event);
	}
	
	@Override
	protected boolean onUpload(XMessage xm, UploadInfo ui) throws Exception {
		final String type = getUploadType(xm);
		if(!TextUtils.isEmpty(type)){
			Event e = AndroidEventManager.getInstance().runEvent(EventCode.HTTP_PostFile, 
					type,xm.getPhotoFilePath(),
					ui.mRunnable,mHandler,ui.mCancel);
			if(e.isSuccess()){
				xm.setPhotoDownloadUrl((String)e.getReturnParamAtIndex(0));
				xm.setThumbPhotoDownloadUrl((String)e.getReturnParamAtIndex(1));
				return true;
			}
		}
		
		return false;
	}

	@Override
	protected void onPercentageChanged(UploadInfo ui) {
		AndroidEventManager.getInstance().runEvent(
				EventCode.UploadChatPhotoPercentChanged,
				ui.mMessage);
	}

	@Override
	protected String getUploadType(XMessage xm) {
		return null;
	}
}
