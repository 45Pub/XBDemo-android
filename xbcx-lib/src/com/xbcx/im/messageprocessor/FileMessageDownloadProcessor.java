package com.xbcx.im.messageprocessor;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.XApplication;
import com.xbcx.core.EventManager.OnEventRunner;
import com.xbcx.im.XMessage;
import com.xbcx.utils.HttpUtils;

public class FileMessageDownloadProcessor extends MessageDownloadProcessor implements 
																		OnEventRunner{

	public static FileMessageDownloadProcessor getInstance(){
		return sInstance;
	}
	
	private static FileMessageDownloadProcessor sInstance;
	
	protected FileMessageDownloadProcessor(){
		sInstance = this;
		AndroidEventManager.getInstance().registerEventRunner(
				EventCode.DownloadChatFile, this);
	}

	@Override
	protected void onDownload(XMessage m, boolean bThumb) {
		if(m.getType() == XMessage.TYPE_FILE){
			mMapIdToDownloadInfo.put(m.getId(), new DownloadInfo(m, false));
			AndroidEventManager.getInstance().pushEvent(EventCode.DownloadChatFile, m);
		}
	}

	@Override
	protected void onPercentageChanged(DownloadInfo di) {
		AndroidEventManager.getInstance().runEvent(
				EventCode.DownloadChatFilePerChanged, di.mMessage);
	}

	@Override
	public void onEventRun(Event event) throws Exception {
		final int code = event.getEventCode();
		if(code == EventCode.DownloadChatFile){
			XMessage xm = (XMessage)event.getParamAtIndex(0);
			DownloadInfo di = mMapIdToDownloadInfo.get(xm.getId());
			if(di != null){
				if(HttpUtils.doDownload(xm.getOfflineFileDownloadUrl(), 
						xm.getFilePath(), 
						di.mRunnable, 
						XApplication.getMainThreadHandler(), 
						di.mCancel)){
					event.setSuccess(true);
				}
				if(!di.mCancel.get()){
					xm.setDownloaded();
					xm.updateDB();
				}
				mMapIdToDownloadInfo.remove(xm.getId());
			}
		}
	}
}
