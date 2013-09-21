package com.xbcx.im.messageprocessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.xbcx.im.IMKernel;
import com.xbcx.im.IMModule;
import com.xbcx.im.XMessage;
import com.xbcx.utils.HttpUtils.ProgressRunnable;

public abstract class MessageDownloadProcessor extends IMModule{
	
	protected Map<String, DownloadInfo> mMapIdToDownloadInfo = new ConcurrentHashMap<String,DownloadInfo>();
	protected Map<String, DownloadInfo> mMapIdToThumbDownloadInfo = new ConcurrentHashMap<String,DownloadInfo>();
	
	public void requestDownload(XMessage m,boolean bThumb){
		if(bThumb){
			if(!isThumbDownloading(m)){
				onDownload(m, bThumb);
			}
		}else{
			if(!isDownloading(m)){
				onDownload(m, bThumb);
			}
		}
	}
	
	protected abstract void onDownload(XMessage m,boolean bThumb);
	
	@Override
	protected void onInitial(IMKernel kernel) {
	}

	@Override
	protected void onRelease() {
	}
	
	public void stopDownload(XMessage m,boolean bThumb){
		if(bThumb){
			DownloadInfo di = mMapIdToThumbDownloadInfo.get(m.getId());
			if(di != null){
				di.mCancel.set(true);
			}
		}else{
			DownloadInfo di = mMapIdToDownloadInfo.get(m.getId());
			if(di != null){
				di.mCancel.set(true);
			}
		}
	}
	
	public void stopAllDownload(){
		for(DownloadInfo di : mMapIdToDownloadInfo.values()){
			di.mCancel.set(true);
		}
		for(DownloadInfo di : mMapIdToThumbDownloadInfo.values()){
			di.mCancel.set(true);
		}
	}
	
	public boolean isThumbDownloading(XMessage m){
		DownloadInfo di =  mMapIdToThumbDownloadInfo.get(m.getId());
		if(di == null){
			return false;
		}else{
			return !di.mCancel.get();
		}
	}
	
	public boolean isDownloading(XMessage m){
		DownloadInfo di = mMapIdToDownloadInfo.get(m.getId());
		if(di == null){
			return false;
		}else{
			return !di.mCancel.get();
		}
	}
	
	public int	getThumbDownloadPercentage(XMessage m){
		DownloadInfo di = mMapIdToThumbDownloadInfo.get(m.getId());
		if(di != null){
			return di.getDownloadPercentage();
		}
		return -1;
	}
	
	public int getDownloadPercentage(XMessage m){
		DownloadInfo di = mMapIdToDownloadInfo.get(m.getId());
		if(di != null){
			return di.getDownloadPercentage();
		}
		return -1;
	}
	
	protected abstract void onPercentageChanged(DownloadInfo di);
	
	protected class DownloadInfo{
		
		public XMessage 			mMessage;
		public boolean				mIsDownloadThumb;
		public int					mPercentage;
		public ProgressRunnable 	mRunnable;
		public AtomicBoolean		mCancel = new AtomicBoolean(false);
		
		protected DownloadInfo(XMessage m,boolean bDownloadThumb){
			mMessage = m;
			mIsDownloadThumb = bDownloadThumb;
			
			mRunnable = new ProgressRunnable() {
				@Override
				public void run() {
					setDownloadPercentage(getPercentage());
					onPercentageChanged(DownloadInfo.this);
				}
			};
		}
		
		private void setDownloadPercentage(int nPer){
			mPercentage = nPer;
		}
		
		public int getDownloadPercentage(){
			return mPercentage;
		}
	}
}
