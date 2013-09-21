package com.xbcx.im.messageprocessor;

import android.os.Handler;
import android.os.Looper;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventManager.OnEventRunner;
import com.xbcx.im.XMessage;
import com.xbcx.utils.HttpUtils;

public class PhotoMessageDownloadProcessor extends MessageDownloadProcessor implements 
																		OnEventRunner{

	public static PhotoMessageDownloadProcessor getInstance(){
		return sInstance;
	}
	
	private static PhotoMessageDownloadProcessor sInstance;
	
	protected Handler mHandler = new Handler(Looper.getMainLooper());
	
	protected PhotoMessageDownloadProcessor(){
		sInstance = this;
		AndroidEventManager.getInstance().registerEventRunner(EventCode.DownloadChatPhoto, this);
		AndroidEventManager.getInstance().registerEventRunner(EventCode.DownloadChatThumbPhoto, this);
	}
	
	@Override
	protected void onDownload(XMessage m, boolean bThumb) {
		if(m.getType() == XMessage.TYPE_PHOTO){
			if(bThumb){
				mMapIdToThumbDownloadInfo.put(m.getId(), new DownloadInfo(m, true));
				AndroidEventManager.getInstance().pushEvent(EventCode.DownloadChatThumbPhoto, m);
			}else{
				mMapIdToDownloadInfo.put(m.getId(), new DownloadInfo(m, false));
				AndroidEventManager.getInstance().pushEvent(EventCode.DownloadChatPhoto, m);
			}
		}
	}

	@Override
	public void onEventRun(Event event) throws Exception {
		final XMessage m = (XMessage)event.getParamAtIndex(0);
		final int code = event.getEventCode();
		if(m.getType() == XMessage.TYPE_PHOTO){
			if(code == EventCode.DownloadChatThumbPhoto){
				DownloadInfo di = mMapIdToThumbDownloadInfo.get(m.getId());
				if(di != null){
					try{
						if(HttpUtils.doDownload(m.getThumbPhotoDownloadUrl(), 
								m.getThumbPhotoFilePath(),
								di.mRunnable, mHandler, di.mCancel)){
							event.setSuccess(true);
						}
						if(!di.mCancel.get()){
							m.setDownloaded();
							m.updateDB();
						}
					}finally{
						mMapIdToThumbDownloadInfo.remove(m.getId());
					}
				}
			}else{
				DownloadInfo di = mMapIdToDownloadInfo.get(m.getId());
				if(di != null){
					try{
						if(HttpUtils.doDownload(m.getPhotoDownloadUrl(), 
								m.getPhotoFilePath(), 
								di.mRunnable, mHandler, di.mCancel)){
							event.setSuccess(true);
						}
						if(!di.mCancel.get()){
							m.setDownloaded();
							m.updateDB();
						}
					}finally{
						mMapIdToDownloadInfo.remove(m.getId());
					}
				}
			}
		}
	}

	@Override
	protected void onPercentageChanged(DownloadInfo di) {
		AndroidEventManager.getInstance().runEvent(
				di.mIsDownloadThumb ? 
						EventCode.DownloadChatThumbPhotoPercentChanged : 
							EventCode.DownloadChatPhotoPercentChanged,
				di.mMessage);
	}
}
