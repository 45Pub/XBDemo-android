package com.xbcx.im.messageprocessor;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.XApplication;
import com.xbcx.core.EventManager.OnEventRunner;
import com.xbcx.im.XMessage;
import com.xbcx.utils.HttpUtils;

public class VideoMessageDownloadProcessor extends MessageDownloadProcessor implements 
																		OnEventRunner{

	public static VideoMessageDownloadProcessor getInstance(){
		return sInstance;
	}
	
	private static VideoMessageDownloadProcessor sInstance;
	
	protected VideoMessageDownloadProcessor(){
		sInstance = this;
		AndroidEventManager.getInstance().registerEventRunner(EventCode.DownloadChatVideo, this);
		AndroidEventManager.getInstance().registerEventRunner(EventCode.DownloadChatVideoThumb, this);
	}

	@Override
	protected void onDownload(XMessage m, boolean bThumb) {
		if(m.getType() == XMessage.TYPE_VIDEO){
			if(bThumb){
				mMapIdToThumbDownloadInfo.put(m.getId(), new DownloadInfo(m, true));
				AndroidEventManager.getInstance().pushEvent(EventCode.DownloadChatVideoThumb, m);
			}else{
				mMapIdToDownloadInfo.put(m.getId(), new DownloadInfo(m, false));
				AndroidEventManager.getInstance().pushEvent(EventCode.DownloadChatVideo, m);
			}
		}
	}
	
	@Override
	public void onEventRun(Event event) throws Exception {
		final XMessage m = (XMessage)event.getParamAtIndex(0);
		final int code = event.getEventCode();
		if(m.getType() == XMessage.TYPE_VIDEO){
			if(code == EventCode.DownloadChatVideoThumb){
				DownloadInfo di = mMapIdToThumbDownloadInfo.get(m.getId());
				if(di != null){
					if(HttpUtils.doDownload(m.getVideoThumbDownloadUrl(), 
							m.getVideoThumbFilePath(),
							di.mRunnable, XApplication.getMainThreadHandler(), 
							di.mCancel)){
						event.setSuccess(true);
					}
					mMapIdToThumbDownloadInfo.remove(m.getId());
				}
			}else if(code == EventCode.DownloadChatVideo){
				DownloadInfo di = mMapIdToDownloadInfo.get(m.getId());
				if(di != null){
					if(HttpUtils.doDownload(m.getVideoDownloadUrl(), 
							m.getVideoFilePath(), 
							di.mRunnable, XApplication.getMainThreadHandler(),
							di.mCancel)){
						event.setSuccess(true);
					}
					if(!di.mCancel.get()){
						m.setDownloaded();
						m.updateDB();
					}
					
					mMapIdToDownloadInfo.remove(m.getId());
				}
			}
		}
	}

	@Override
	protected void onPercentageChanged(DownloadInfo di) {
		AndroidEventManager.getInstance().runEvent(
				di.mIsDownloadThumb ? 
						EventCode.DownloadChatVideoThumbPerChanged : 
							EventCode.DownloadChatVideoPerChanged,
				di.mMessage);
	}

}
