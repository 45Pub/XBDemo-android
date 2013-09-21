package com.xbcx.im.messageprocessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.im.IMKernel;
import com.xbcx.im.IMModule;
import com.xbcx.im.XMessage;
import com.xbcx.utils.HttpUtils.ProgressRunnable;

public abstract class MessageUploadProcessor extends IMModule{
	
	protected Map<String, UploadInfo> 	mMapIdToUploadInfo = new ConcurrentHashMap<String,UploadInfo>();
	
	public abstract void requestUpload(XMessage m);
	
	@Override
	protected void onInitial(IMKernel kernel) {
	}

	@Override
	protected void onRelease() {
	}

	public void 	stopUpload(XMessage m){
		UploadInfo ui = mMapIdToUploadInfo.get(m.getId());
		if(ui != null){
			ui.mCancel.set(true);
		}
	}
	
	public void 	stopAllUpload(){
		for(UploadInfo ui : mMapIdToUploadInfo.values()){
			ui.mCancel.set(true);
		}
	}
	
	public boolean 	isUploading(XMessage m){
		UploadInfo ui = mMapIdToUploadInfo.get(m.getId());
		if(ui == null){
			return false;
		}else{
			return !ui.mCancel.get();
		}
	}
	
	public int		getUploadPercentage(XMessage m){
		UploadInfo ui = mMapIdToUploadInfo.get(m.getId());
		if(ui != null){
			return ui.getUploadPercentage();
		}
		return -1;
	}
	
	protected void doUpload(Event event) throws Exception{
		final XMessage xm = (XMessage)event.getParamAtIndex(0);
		final UploadInfo ui = mMapIdToUploadInfo.get(xm.getId());
		if(ui != null){
			try{
				if(onUpload(xm, ui)){
					if(!ui.mCancel.get()){
						xm.setUploadSuccess(true);
						xm.updateDB();
						
						AndroidEventManager.getInstance().pushEvent(EventCode.IM_SendMessage, xm);
					}
				}
			}finally{
				mMapIdToUploadInfo.remove(xm.getId());
			}
		}
	}
	
	protected abstract boolean 	onUpload(XMessage xm,UploadInfo ui) throws Exception;
	
	protected abstract void 	onPercentageChanged(UploadInfo ui);
	
	protected abstract String 	getUploadType(XMessage xm);
	
	protected class UploadInfo{
		public XMessage			mMessage;
		public int 				mPercentage;
		public ProgressRunnable	mRunnable;
		public AtomicBoolean	mCancel = new AtomicBoolean();
		
		public UploadInfo(XMessage m){
			mMessage = m;
			
			mRunnable = new ProgressRunnable() {
				@Override
				public void run() {
					setUploadPercentage(getPercentage());
					onPercentageChanged(UploadInfo.this);
				}
			};
		}
		
		private void setUploadPercentage(int nPer){
			mPercentage = nPer;
		}
		
		public int getUploadPercentage(){
			return mPercentage;
		}
	}
}
