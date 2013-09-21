package com.xbcx.im.messageprocessor;

import java.util.ArrayList;
import java.util.HashMap;

import com.xbcx.im.XMessage;

public class VoiceMessageProcessor {

	protected final ArrayList<XMessage> mListVoiceMessage = new ArrayList<XMessage>();
	protected final HashMap<String,Integer> mMapMessageIdToPos = new HashMap<String, Integer>(1024);
	
	public void onHandleMessage(XMessage m){
		if(m.getType() == XMessage.TYPE_VOICE){
			addMessage(m);
			
			onNewVoiceMessage(m);
		}
	}
	
	protected void onNewVoiceMessage(XMessage m){
	}
	
	public void onCreate(){
	}
	
	public void onDestroy(){
		mListVoiceMessage.clear();
		mMapMessageIdToPos.clear();
	}
	
	public void addMessage(XMessage m){
		mMapMessageIdToPos.put(m.getId(), mListVoiceMessage.size());
		mListVoiceMessage.add(m);
	}
	
	public void removeMessage(XMessage m){
		mListVoiceMessage.remove(m);
		int nIndex = 0;
		for(XMessage xm : mListVoiceMessage){
			mMapMessageIdToPos.put(xm.getId(),nIndex++);
		}
	}
	
	public void clear(){
		mListVoiceMessage.clear();
		mMapMessageIdToPos.clear();
	}
	
	public int getVoiceCount(){
		return mListVoiceMessage.size();
	}
	
	public XMessage getFirstNotDownloadedMessage(){
		for(XMessage m : mListVoiceMessage){
			if(!m.isFromSelf() && !m.isVoiceFileExists()){
				return m;
			}
		}
		return null;
	}
	
	public XMessage getFirstNotPlayedMessage(){
		for(XMessage m : mListVoiceMessage){
			if(!m.isFromSelf() && !m.isPlayed()){
				return m;
			}
		}
		return null;
	}
	
	public XMessage getRecentlyNotPlayedMessage(){
		final int nSize = mListVoiceMessage.size();
		if(nSize > 0){
			XMessage m = mListVoiceMessage.get(nSize - 1);
			if(!m.isFromSelf() && !m.isPlayed()){
				return m;
			}
		}
		return null;
	}

	public XMessage getNextNotDownloadedMessage(String strMessageId){
		Integer pos = mMapMessageIdToPos.get(strMessageId);
		if(pos != null){
			final int nPos = pos.intValue();
			final int nSize = mListVoiceMessage.size();
			if(nSize > nPos + 1){
				XMessage m = null;
				for(int nIndex = nPos + 1;nIndex < nSize;++nIndex){
					m = mListVoiceMessage.get(nIndex);
					if(!m.isFromSelf() && !m.isVoiceFileExists()){
						return m;
					}
				}
			}
		}
		return null;
	}
	
	public XMessage getNextDownloadedAndNotPlayedMessage(String strMessageId){
		Integer pos = mMapMessageIdToPos.get(strMessageId);
		if(pos != null){
			final int nPos = pos.intValue();
			final int nSize = mListVoiceMessage.size();
			if(nSize > nPos + 1){
				XMessage m = null;
				for(int nIndex = nPos + 1;nIndex < nSize;++nIndex){
					m = mListVoiceMessage.get(nIndex);
					if(!m.isFromSelf() && !m.isPlayed()){
						if(m.isVoiceFileExists()){
							return m;
						}else{
							return null;
						}
					}
				}
			}
		}
		return null;
	}
}
