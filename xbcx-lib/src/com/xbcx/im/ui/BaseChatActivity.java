package com.xbcx.im.ui;

import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.xbcx.core.EventCode;
import com.xbcx.im.RecentChatManager;
import com.xbcx.im.XMessage;
import com.xbcx.im.db.DBReadMessageCountParam;
import com.xbcx.im.db.DBReadMessageParam;
import com.xbcx.im.messageprocessor.PhotoMessageDownloadProcessor;
import com.xbcx.im.messageprocessor.VideoMessageDownloadProcessor;
import com.xbcx.im.messageprocessor.VoiceMessageNormalPlayProcessor;
import com.xbcx.im.messageprocessor.VoicePlayProcessor;

public abstract class BaseChatActivity extends ChatActivity {
	
	protected static final String EXTRA_ID 		= "id";
	protected static final String EXTRA_NAME 	= "name";
	
	protected String mId;
	protected String mName;
	
	protected int	mInitReadCount = 0;
	
	protected VoiceMessageNormalPlayProcessor	mPlayProcessor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mId = getIntent().getStringExtra(EXTRA_ID);
		mName = getIntent().getStringExtra(EXTRA_NAME);
		if(TextUtils.isEmpty(mId)){
			finish();
			return;
		}
		super.onCreate(savedInstanceState);
		
		VoicePlayProcessor.getInstance().initial(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPlayProcessor.onDestroy();
		
		VoicePlayProcessor.getInstance().stop();
		
		VoicePlayProcessor.getInstance().release();
	}


	@Override
	protected void onInitChatAttribute(ChatAttribute attr) {
		super.onInitChatAttribute(attr);
		attr.mFromId = mId;
	}
	
	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
		ba.mTitleText = mName;
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		RecentChatManager.getInstance().clearUnreadMessageCount(
				RecentChatManager.getInstance().getRecentChat(mId));
		
		mPlayProcessor.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		mPlayProcessor.onPause();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		final String id = intent.getStringExtra("id");
		final String name = intent.getStringExtra("name");
		if(!mId.equals(id)){
			mId = id;
			mName = name;
			mTextViewTitle.setText(mName);
			mMessageAdapter.clear();
			mPlayProcessor.clear();
			initLoad();
			
			mListView.setCanRun(true);
		}
	}
	
	@Override
	protected void onInit() {
		super.onInit();
		
		mPlayProcessor = new VoiceMessageNormalPlayProcessor();
		mPlayProcessor.onCreate();
		
		initLoad();
		
		mPlayProcessor.start();
	}

	protected void initLoad(){
		final int nUnreadMessageCount = RecentChatManager.getInstance()
				.getUnreadMessageCount(mId);
		
		DBReadMessageCountParam param = new DBReadMessageCountParam(
				getFromType(), mId);
		mEventManager.runEvent(EventCode.DB_ReadMessageCount, param);
		mLastReadPosition = param.mReturnCount - 1;

		mInitReadCount = nUnreadMessageCount;
		if(mInitReadCount == 0){
			mInitReadCount = 15;
		}
		
		loadOnePage();
		
		mListView.setSelection(mMessageAdapter.getCount() - 1);
	}
	
	protected abstract int 		getFromType();
	
	protected abstract boolean 	isGroupChat();
	
	@Override
	protected void onLoadOnePageMessage(List<XMessage> listMessage, int nPosition) {
		super.onLoadOnePageMessage(listMessage, nPosition);
		
		DBReadMessageParam param = new DBReadMessageParam(mId,getFromType());
		param.mMessages = listMessage;
		if(mInitReadCount == 0){
			param.mReadCount = 15;
		}else{
			param.mReadCount = mInitReadCount > 15 ? mInitReadCount : 15;
		}
		param.mReadPosition = nPosition;
		mEventManager.runEvent(EventCode.DB_ReadMessage, param);
		
		if(mInitReadCount != 0){
			for(XMessage m : listMessage){
				if(m.getType() == XMessage.TYPE_VOICE){
					mPlayProcessor.addMessage(m);
				}else if(m.getType() == XMessage.TYPE_PHOTO){
					if(!m.isFromSelf()){
						if(!m.isThumbPhotoDownloading() && !m.isThumbPhotoFileExists()){
							PhotoMessageDownloadProcessor.getInstance().requestDownload(m, true);
						}
					}else{
						if(!m.isThumbPhotoDownloading()){
							if(!m.isPhotoFileExists()){
								if(!m.isThumbPhotoFileExists()){
									PhotoMessageDownloadProcessor.getInstance().requestDownload(m, true);
								}
							}
						}
					}
				}else if(m.getType() == XMessage.TYPE_VIDEO){
					if(!m.isFromSelf()){
						if(!m.isVideoThumbDownloading() && !m.isVideoThumbFileExists()){
							VideoMessageDownloadProcessor.getInstance().requestDownload(m, true);
						}
					}
				}
			}
			mInitReadCount = 0;
		}
	}
	
	
	protected void reloadMessageFromDB(){
		DBReadMessageParam param = new DBReadMessageParam(mId, getFromType());
		param.mMessages = new LinkedList<XMessage>();
		param.mReadPosition = mLastReadPosition >= 0 ? mLastReadPosition : 0;
		param.mReadCount = mMessageAdapter.getCount();
		mEventManager.runEvent(EventCode.DB_ReadMessage, param);
		mMessageAdapter.clear();
		List<XMessage> xms = addGroupTimeMessage(param.mMessages);
		mMessageAdapter.addAllItem(xms);
	}

	@Override
	protected void onDeleteMessage(XMessage m) {
		super.onDeleteMessage(m);
		mEventManager.runEvent(EventCode.DB_DeleteMessage, mId,m.getId());
		
		mPlayProcessor.removeMessage(m);
	}

	@Override
	public boolean onSendCheck() {
		return true;
	}

	@Override
	protected void onInitMessage(XMessage m) {
		super.onInitMessage(m);
		m.setFromType(getFromType());
		if(isGroupChat()){
			m.setGroupId(mId);
			m.setGroupName(mName);
		}else{
			m.setUserId(mId);
			m.setUserName(mName);
		}
	}
}
