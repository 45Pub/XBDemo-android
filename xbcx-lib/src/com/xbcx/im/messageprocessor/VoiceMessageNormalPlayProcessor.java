package com.xbcx.im.messageprocessor;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.im.XMessage;

public class VoiceMessageNormalPlayProcessor extends VoiceMessageProcessor implements 
														OnEventListener{
	protected XMessage 	mDownloadingMessage;
	
	protected boolean		mIsPlaying;
	
	protected boolean		mIsPaused;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		AndroidEventManager.getInstance().addEventListener(EventCode.DownloadChatVoice, this, false);
		AndroidEventManager.getInstance().addEventListener(EventCode.VoicePlayStarted, this, false);
		AndroidEventManager.getInstance().addEventListener(EventCode.VoicePlayErrored, this, false);
		AndroidEventManager.getInstance().addEventListener(EventCode.VoicePlayCompletioned, this, false);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		AndroidEventManager.getInstance().removeEventListener(EventCode.DownloadChatVoice, this);
		AndroidEventManager.getInstance().removeEventListener(EventCode.VoicePlayStarted, this);
		AndroidEventManager.getInstance().removeEventListener(EventCode.VoicePlayErrored, this);
		AndroidEventManager.getInstance().removeEventListener(EventCode.VoicePlayCompletioned, this);
	}
	
	public void onResume(){
		mIsPaused = false;
	}
	
	public void onPause(){
		mIsPaused = true;
	}
	
	public void start(){
		XMessage messageFirstNotDownload = getFirstNotDownloadedMessage();
		if(messageFirstNotDownload == null){
			XMessage messageFirstNotPlayed = getFirstNotPlayedMessage();
			if(messageFirstNotPlayed != null){
				VoicePlayProcessor.getInstance().play(messageFirstNotPlayed);
			}
		}else{
			requestDownloadVoice(messageFirstNotDownload);
		}
	}

	@Override
	protected void onNewVoiceMessage(XMessage m) {
		super.onNewVoiceMessage(m);
		if(!m.isFromSelf()){
			requestDownloadVoice(m);
		}
	}

	protected void requestDownloadVoice(XMessage m){
		if(mDownloadingMessage == null){
			mDownloadingMessage = m;
			VoiceMessageDownloadProcessor.getInstance().requestDownload(m);
		}
	}

	@Override
	public void onEventRunEnd(Event event) {
		if(!mIsPaused){
			final int nCode = event.getEventCode();
			if(nCode == EventCode.DownloadChatVoice){
				final XMessage messageDownloaded = (XMessage)event.getParamAtIndex(0);
				if(mDownloadingMessage != null && 
						mDownloadingMessage.equals(messageDownloaded)){
					mDownloadingMessage = null;
					if(event.isSuccess()){
						if(!mIsPlaying){
							VoicePlayProcessor.getInstance().play(messageDownloaded);
						}
					}else{
						XMessage messageNextDownload = getNextNotDownloadedMessage(messageDownloaded.getId());
						if(messageNextDownload != null){
							requestDownloadVoice(messageNextDownload);
						}
					}
				}
			}else if(nCode == EventCode.VoicePlayStarted){
				mIsPlaying = true;
				XMessage messagePlaying = (XMessage)event.getParamAtIndex(0);
				if(mDownloadingMessage == null){
					XMessage messageNextDownload = getNextNotDownloadedMessage(messagePlaying.getId());
					if(messageNextDownload != null){
						requestDownloadVoice(messageNextDownload);
					}
				}
			}else if(nCode == EventCode.VoicePlayCompletioned){
				mIsPlaying = false;
				XMessage messagePlayCompletioned = (XMessage)event.getParamAtIndex(0);
				if(messagePlayCompletioned != null){
					XMessage messageNextPlay = getNextDownloadedAndNotPlayedMessage(
							messagePlayCompletioned.getId());
					if(messageNextPlay != null){
						VoicePlayProcessor.getInstance().play(messageNextPlay);
					}
				}
			}else if(nCode == EventCode.VoicePlayErrored){
				XMessage messagePlayErrored = (XMessage)event.getParamAtIndex(0);
				if(messagePlayErrored != null){
					XMessage messageNextPlay = getNextDownloadedAndNotPlayedMessage(messagePlayErrored.getId());
					if(messageNextPlay != null){
						VoicePlayProcessor.getInstance().play(messageNextPlay);
					}
				}
			}
		}
	}
}
