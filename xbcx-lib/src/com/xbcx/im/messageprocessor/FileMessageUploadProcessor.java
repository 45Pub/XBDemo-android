package com.xbcx.im.messageprocessor;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventManager.OnEventRunner;
import com.xbcx.im.XMessage;

public class FileMessageUploadProcessor extends MessageUploadProcessor implements 
																	OnEventRunner{

	public static FileMessageUploadProcessor getInstance(){
		return sInstance;
	}
	
	private static FileMessageUploadProcessor sInstance;
	
	protected Handler mHandler = new Handler(Looper.getMainLooper());
	
	protected FileMessageUploadProcessor(){
		sInstance = this;
		
		AndroidEventManager.getInstance().registerEventRunner(
				EventCode.UploadChatFile, this);
	}
	
	@Override
	public void requestUpload(XMessage m) {
		if(m.getType() == XMessage.TYPE_FILE){
			UploadInfo ui = new UploadInfo(m);
			mMapIdToUploadInfo.put(m.getId(), ui);
			AndroidEventManager.getInstance().pushEvent(EventCode.UploadChatFile, m);
		}
	}

	@Override
	protected void onPercentageChanged(UploadInfo ui) {
		AndroidEventManager.getInstance().runEvent(
				EventCode.UploadChatFilePerChanged, ui.mMessage);
	}

	@Override
	public void onEventRun(Event event) throws Exception {
		final int code = event.getEventCode();
		if(code == EventCode.UploadChatFile){
			doUpload(event);
		}
	}

	@Override
	protected boolean onUpload(XMessage xm,UploadInfo ui) throws Exception{
		final String type = getUploadType(xm);
		if(!TextUtils.isEmpty(type)){
			Event e = AndroidEventManager.getInstance().runEvent(EventCode.HTTP_PostFile, 
					type,xm.getFilePath(),
					ui.mRunnable,mHandler,ui.mCancel);
			if(e.isSuccess()){
				xm.setOfflineFileDownloadUrl((String)e.getReturnParamAtIndex(0));
				return true;
			}
		}
		return false;
	}
	
	protected String getUploadType(XMessage xm){
		return "";
	}
}
