package com.xbcx.im.messageprocessor;

import java.util.Locale;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.XApplication;
import com.xbcx.core.EventManager.OnEventRunner;
import com.xbcx.im.IMKernel;
import com.xbcx.im.XMessage;
import com.xbcx.utils.HttpUtils;
import com.xbcx.utils.SystemUtils;

public class LocationMessageDownloadProcessor extends MessageDownloadProcessor implements
														OnEventRunner{
	
	public static LocationMessageDownloadProcessor getInstance(){
		return sInstance;
	}
	
	private static LocationMessageDownloadProcessor sInstance;

	protected LocationMessageDownloadProcessor(){
		sInstance = this;
		AndroidEventManager.getInstance().registerEventRunner(EventCode.DownloadLocation, this);
	}
	
	@Override
	protected void onDownload(XMessage m, boolean bThumb) {
		if(m.getType() == XMessage.TYPE_LOCATION){
			mMapIdToDownloadInfo.put(m.getId(), new DownloadInfo(m, false));
			AndroidEventManager.getInstance().pushEvent(EventCode.DownloadLocation, m);
		}
	}

	@Override
	protected void onPercentageChanged(DownloadInfo di) {
	}

	@Override
	public void onEventRun(Event event) throws Exception {
		final int code = event.getEventCode();
		if(code == EventCode.DownloadLocation){
			final XMessage xm = (XMessage)event.getParamAtIndex(0);
			DownloadInfo di = mMapIdToDownloadInfo.get(xm.getId());
			try{
				final String location[] = xm.getLocation();
				final String url = String.format(Locale.getDefault(),
						XApplication.URL_GetLocationImage, 
						Double.parseDouble(location[0]),Double.parseDouble(location[1]),
						12,
						SystemUtils.dipToPixel(IMKernel.getInstance().getContext(), 150),
						SystemUtils.dipToPixel(IMKernel.getInstance().getContext(), 100));
				if(HttpUtils.doDownload(url, xm.getLocationFilePath(), null, null, di.mCancel)){
					event.setSuccess(true);
				}
				if(!di.mCancel.get()){
					xm.setDownloaded();
					xm.updateDB();
				}
			}finally{
				mMapIdToDownloadInfo.remove(xm.getId());
			}
		}
	}

}
