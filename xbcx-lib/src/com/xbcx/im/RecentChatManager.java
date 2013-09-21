package com.xbcx.im;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.im.RecentChatProvider;

public class RecentChatManager extends IMModule implements OnEventListener{

	public static RecentChatManager getInstance(){
		return sInstance;
	}
	
	private static RecentChatManager sInstance;
	
	private Map<String, RecentChatProvider> mMapClassNameToRecentChatProvider = new HashMap<String, RecentChatProvider>();
	
	private List<RecentChat> 		mListRecentChat 	= Collections.synchronizedList(new LinkedList<RecentChat>());
	private Map<String, RecentChat> mMapIdToRecentChat 	= new ConcurrentHashMap<String, RecentChat>();
	
	private Map<String, RecentChat> mMapIdToHasUnreadRecentChat = new ConcurrentHashMap<String, RecentChat>();
	private int						mUnreadMessageTotalCount = 0;
	
	private InternalHandler mHandler;
	
	RecentChatManager(){
		sInstance = this;
		AndroidEventManager.getInstance().addEventListener(EventCode.HandleRecentChat, this, false);
	}
	
	@Override
	protected void onInitial(IMKernel kernel) {
		HandlerThread handleThread = new HandlerThread("processRecentChat");
		handleThread.start();
		mHandler = new InternalHandler(handleThread.getLooper());
		
		AndroidEventManager.getInstance().runEvent(EventCode.DB_ReadRecentChat,
				mListRecentChat);
		for(RecentChat recentChat : mListRecentChat){
			mMapIdToRecentChat.put(recentChat.getId(), recentChat);
			if(recentChat.getUnreadMessageCount() > 0){
				mMapIdToHasUnreadRecentChat.put(recentChat.getId(), recentChat);
				mUnreadMessageTotalCount += recentChat.getUnreadMessageCount();
			}
		}
		
		List<RecentChat> rcPlugins = loadPluginRecentChat();
		if(rcPlugins != null){
			for(RecentChat rc : rcPlugins){
				if(!mMapIdToRecentChat.containsKey(rc.getId())){
					mMapIdToRecentChat.put(rc.getId(), rc);
					mListRecentChat.add(rc);
					if(rc.getUnreadMessageCount() > 0){
						mMapIdToHasUnreadRecentChat.put(rc.getId(), rc);
						mUnreadMessageTotalCount += rc.getUnreadMessageCount();
					}
				}
			}
		}
	}

	@Override
	protected void onRelease() {
		if(mHandler != null){
			mHandler.getLooper().quit();
		}
		mListRecentChat.clear();
		mMapIdToHasUnreadRecentChat.clear();
		mMapIdToRecentChat.clear();
		mUnreadMessageTotalCount = 0;
	}
	
	protected List<RecentChat> loadPluginRecentChat(){
		return null;
	}
	
	public void clearRecentChat(){
		AndroidEventManager.getInstance().runEvent(EventCode.DB_DeleteRecentChat);
		initial(null);
		AndroidEventManager.getInstance().runEvent(EventCode.RecentChatChanged,
				Collections.unmodifiableList(mListRecentChat));
		AndroidEventManager.getInstance().runEvent(EventCode.UnreadMessageCountChanged);
	}
	
	public void registerRecentChatProvider(String className,RecentChatProvider provider){
		mMapClassNameToRecentChatProvider.put(className, provider);
	}
	
	public void editRecentChat(String id,RecentChatEditCallback callback){
		final RecentChat rc = getRecentChat(id);
		if(rc != null){
			final int oldUnreadCount = rc.getUnreadMessageCount();
			callback.onEditRecentChat(rc);
			if(oldUnreadCount != rc.getUnreadMessageCount() &&
					rc.getUnreadMessageCount() > 0){
				mUnreadMessageTotalCount += rc.getUnreadMessageCount() - oldUnreadCount;
				mMapIdToHasUnreadRecentChat.put(rc.getId(), rc);
				notifyUnreadMessageCountChanged(rc);
			}
			AndroidEventManager.getInstance().runEvent(EventCode.DB_SaveRecentChat, rc);
			notifyRecentChatChanged();
		}
	}
	
	protected void onHandleObject(Object obj){
		if(obj != null){
			RecentChatProvider provider = mMapClassNameToRecentChatProvider.get(
					obj.getClass().getName());
			if(provider != null){
				synchronized (provider) {
					final String id = provider.getId(obj);
					if(!TextUtils.isEmpty(id)){
						long time = provider.getTime(obj);
						RecentChat rc = getRecentChat(id);
						if(rc == null){
							rc = new RecentChat(id);
							int index = 0;
							for(RecentChat temp : mListRecentChat){
								if(time < temp.getTime()){
									++index;
								}
							}
							mListRecentChat.add(index, rc);
							mMapIdToRecentChat.put(id, rc);
						}else{
							mListRecentChat.remove(rc);
							int index = 0;
							for(RecentChat temp : mListRecentChat){
								if(time < temp.getTime()){
									++index;
								}
							}
							mListRecentChat.add(index, rc);
						}
						rc.setTime(time);
						
						final int oldUnreadCount = rc.getUnreadMessageCount();

						provider.handleRecentChat(rc, obj);
						
						if(provider.isUnread(obj)){
							rc.addUnreadMessageCount();
							
							mMapIdToHasUnreadRecentChat.put(rc.getId(), rc);
							mUnreadMessageTotalCount += (rc.getUnreadMessageCount() - oldUnreadCount);
						
							notifyUnreadMessageCountChanged(rc);
						}
						
						AndroidEventManager.getInstance().runEvent(
								EventCode.DB_SaveRecentChat,rc);
						
						notifyRecentChatChanged();
					}
				}
			}
		}
	}
	
	@Override
	public void onEventRunEnd(Event event) {
		final int code = event.getEventCode();
		if(code == EventCode.HandleRecentChat){
			Object obj = event.getParamAtIndex(0);
			mHandler.sendMessage(mHandler.obtainMessage(1, obj));
		}
	}
	
	public void deleteRecentChat(String strId){
		RecentChat recentChatRemove = mMapIdToRecentChat.remove(strId);
		if(recentChatRemove != null){
			mListRecentChat.remove(recentChatRemove);
			if(recentChatRemove.getUnreadMessageCount() > 0){
				mMapIdToHasUnreadRecentChat.remove(strId);
				mUnreadMessageTotalCount -= recentChatRemove.getUnreadMessageCount();
			}
			AndroidEventManager.getInstance().runEvent(EventCode.DB_DeleteRecentChat,strId);
			
			notifyRecentChatChanged();
			notifyUnreadMessageCountChanged(null);
		}
	}
	
	public void notifyUnreadMessageCountChanged(RecentChat rc){
		AndroidEventManager.getInstance().runEvent(
				EventCode.UnreadMessageCountChanged,rc);
	}
	
	public void notifyRecentChatChanged(){
		AndroidEventManager.getInstance().runEvent(EventCode.RecentChatChanged,
				Collections.unmodifiableList(mListRecentChat));
	}
	
	public List<RecentChat> getAllRecentChat(){
		return Collections.unmodifiableList(mListRecentChat);
	}
	
	public Collection<RecentChat> getAllHasUnreadRecentChat(){
		return Collections.unmodifiableCollection(mMapIdToHasUnreadRecentChat.values());
	}
	
	public int	getUnreadMessageTotalCount(){
		return mUnreadMessageTotalCount;
	}
	
	public int  getUnreadMessageCount(String strId){
		RecentChat recentChat = mMapIdToRecentChat.get(strId);
		if(recentChat != null){
			return recentChat.getUnreadMessageCount();
		}
		return 0;
	}
	
	public void	clearUnreadMessageCount(RecentChat recentChat){
		if(recentChat != null && recentChat.getUnreadMessageCount() > 0){
			mUnreadMessageTotalCount -= recentChat.getUnreadMessageCount();
			recentChat.setUnreadMessageCount(0);
			mMapIdToHasUnreadRecentChat.remove(recentChat.getId());
			
			AndroidEventManager.getInstance().runEvent(EventCode.DB_SaveRecentChat,recentChat);
		
			AndroidEventManager.getInstance().runEvent(
					EventCode.UnreadMessageCountChanged,recentChat);
		}
	}
	
	public RecentChat getRecentChat(String strId){
		return mMapIdToRecentChat.get(strId);
	}
	
	private static class InternalHandler extends Handler{
		public InternalHandler(Looper looper){
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			getInstance().onHandleObject(msg.obj);
		}
	}
}
