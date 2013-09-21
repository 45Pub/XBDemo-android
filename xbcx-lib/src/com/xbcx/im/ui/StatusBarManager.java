package com.xbcx.im.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.im.IMConfigManager;
import com.xbcx.im.IMKernel;
import com.xbcx.im.IMModule;
import com.xbcx.im.RecentChat;
import com.xbcx.im.RecentChatManager;
import com.xbcx.im.XMessage;
import com.xbcx.im.db.DBReadLastMessageParam;
import com.xbcx.library.R;

public class StatusBarManager extends IMModule 
								implements OnEventListener{
	
	public static StatusBarManager getInstance(){
		return sInstance;
	}
	
	private static StatusBarManager sInstance;
	
	private static final int NOTIFY_ID_SINGLECHAT 		= 1;
	private static final int NOTIFY_ID_GROUPCHAT 		= 2;
	private static final int NOTIFY_ID_DISCUSSIONCHAT	= 3;
	
	private NotificationManager mNotificationManager;
	
	private Context				mContext;
	
	private boolean				mIsMerge = true;
	
	private int					mIconResId = R.drawable.ic_launcher;
	
	@SuppressWarnings("rawtypes")
	private Class				mJumpActivityClass;
	
	private String 				mTickerLast;
	
	private long				mLastNotifyTime;
	
	private List<RecentChat> mSingleRcs = new ArrayList<RecentChat>();
	private List<RecentChat> mGroupRcs = new ArrayList<RecentChat>();
	private List<RecentChat> mDiscussionRcs = new ArrayList<RecentChat>();
	private int				 mSingleTotalUnreadCount = 0;
	private int				 mGroupTotalUnreadCount = 0;
	private int				 mDiscussionTotalUnreadCount = 0;
	private RecentChat		 mRecentChatUnreadChange;
	
	protected StatusBarManager(){
		sInstance = this;
	}
	
	@SuppressWarnings("rawtypes")
	public void setJumpActivityClass(Class c){
		mJumpActivityClass = c;
	}

	@Override
	public void onEventRunEnd(Event event) {
		final int code = event.getEventCode();
		if(code == EventCode.UnreadMessageCountChanged){
			Collection<RecentChat> collection = RecentChatManager.getInstance()
					.getAllHasUnreadRecentChat();
			final int recentChatSize = collection.size();
			if(recentChatSize > 0){
				if(!IMConfigManager.getInstance().isReceiveNewMessageNotify()){
					clearStatusBar();
					return;
				}
				
				final RecentChat rc = (RecentChat)event.getParamAtIndex(0);
				if(mIsMerge){
					processMergeNotify(collection, recentChatSize,rc);
				}else{
					mRecentChatUnreadChange = rc;
					String strTicker = null;
					if(rc != null){
						if(rc.getActivityType() == ActivityType.SingleChat ||
								rc.getActivityType() == ActivityType.GroupChat ||
								rc.getActivityType() == ActivityType.DiscussionChat){
							if(rc.getUnreadMessageCount() > 0){
								final String strNickname = rc.getName();
								strTicker = mContext.getString(
									R.string.statusbar_single_contact_text_notify, strNickname == null ? "" : strNickname);
								if(strTicker.equals(mTickerLast)){
									strTicker = strTicker + " ";
								}
								mTickerLast = strTicker;
							}
						}
					}
					
					final long curTime = System.currentTimeMillis();
					boolean bSound = true;
					if(curTime - mLastNotifyTime <= 2000){
						bSound = false;
					}
					mLastNotifyTime = curTime;
						
					classify(collection);
					if(rc == null){
						processSingleNotify(strTicker,bSound);
						processGroupNotify(strTicker,bSound);
						processDiscussionNotify(strTicker,bSound);
					}else{
						if(rc.getActivityType() == ActivityType.SingleChat){
							processSingleNotify(strTicker,bSound);
						}else if(rc.getActivityType() == ActivityType.GroupChat){
							processGroupNotify(strTicker,bSound);
						}else if(rc.getActivityType() == ActivityType.DiscussionChat){
							processDiscussionNotify(strTicker,bSound);
						}
					}
				}
			}else{
				clearStatusBar();
			}
		}else if(code == EventCode.IM_LoginOuted){
			clearStatusBar();
		}
	}
	
	@SuppressWarnings("deprecation")
	protected void processMergeNotify(Collection<RecentChat> rcs,int recentChatSize,RecentChat rc){
		String strTicker = null;
		if(rc != null){
			if(rc.getActivityType() == ActivityType.SingleChat ||
					rc.getActivityType() == ActivityType.GroupChat ||
					rc.getActivityType() == ActivityType.DiscussionChat){
				if(rc.getUnreadMessageCount() > 0){
					final String strNickname = rc.getName();
					strTicker = mContext.getString(
						R.string.statusbar_single_contact_text_notify, strNickname == null ? "" : strNickname);
					if(strTicker.equals(mTickerLast)){
						strTicker = strTicker + " ";
					}
					mTickerLast = strTicker;
				}
			}
		}
		
		final long curTime = System.currentTimeMillis();
		boolean bSound = true;
		if(curTime - mLastNotifyTime <= 2000){
			bSound = false;
		}
		mLastNotifyTime = curTime;
		
		int unreadTotalCount = calculateUnreadTotalCount(rcs);
		
		if(recentChatSize > 1){
			Notification notification = new Notification(mIconResId, strTicker,
					System.currentTimeMillis());
			notification.number = unreadTotalCount;
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			final boolean bReduce = rc == null ? 
					true : rc.getUnreadMessageCount() == 0;
			if(bSound){
				if(IMConfigManager.getInstance().isReceiveNewMessageSoundNotify() && !bReduce){
					notification.defaults |= Notification.DEFAULT_SOUND;
				}
				if(IMConfigManager.getInstance().isReceiveNewMessageVibrateNotify() && !bReduce){
					notification.defaults |= Notification.DEFAULT_VIBRATE;
				}
			}
			
			final Context app = mContext;
			notification.setLatestEventInfo(app, app
					.getString(R.string.app_name), app.getString(
					R.string.statusbar_multi_contact_notify, recentChatSize,
					unreadTotalCount), getPendingIntent(null, null, 0));
			mNotificationManager.notify(NOTIFY_ID_SINGLECHAT, notification);
		}else{
			RecentChat rcSingle = rcs.iterator().next();
			Notification notification = new Notification(R.drawable.ic_launcher, strTicker,
					System.currentTimeMillis());
			notification.number = unreadTotalCount;
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			final boolean bReduce = rc == null ? 
					true : rc.getUnreadMessageCount() == 0;
			if(bSound){
				if(IMConfigManager.getInstance().isReceiveNewMessageSoundNotify() && !bReduce){
					notification.defaults |= Notification.DEFAULT_SOUND;
				}
				if(IMConfigManager.getInstance().isReceiveNewMessageVibrateNotify() && !bReduce){
					notification.defaults |= Notification.DEFAULT_VIBRATE;
				}
			}
			
			final Context app = mContext;
			
			if (unreadTotalCount > 1) {
				notification.setLatestEventInfo(
								app,
								rcSingle.getName(),
								app.getString(
										R.string.statusbar_single_contact_multimsg_notify,
										unreadTotalCount),
										getPendingIntent(rcSingle.getId(), rcSingle.getName(), rcSingle.getActivityType()));
			} else {
				XMessage m = readLastMessage(rcSingle.getId());
				final int nMessageType = m == null ? XMessage.TYPE_TEXT : m
						.getType();
				notification.setLatestEventInfo(
								app,
								app.getString(
										nMessageType == XMessage.TYPE_VOICE ? R.string.statusbar_single_contact_voice_notify
												: R.string.statusbar_single_contact_text_notify,
										rcSingle.getName()),
								nMessageType == XMessage.TYPE_VOICE ? app
										.getString(R.string.voice)
										: nMessageType == XMessage.TYPE_PHOTO ? app
												.getString(R.string.photo)
												: m == null ? "" : m
														.getContent(),
														getPendingIntent(rcSingle.getId(), rcSingle.getName(), rcSingle.getActivityType()));
			}
			
			mNotificationManager.notify(NOTIFY_ID_SINGLECHAT, notification);
		}
	}
	
	protected int calculateUnreadTotalCount(Collection<RecentChat> rcs){
		int count = 0;
		for(RecentChat rc : rcs){
			count += rc.getUnreadMessageCount();
		}
		return count;
	}
	
	protected void classify(Collection<RecentChat> rcs){
		mSingleRcs.clear();
		mGroupRcs.clear();
		mDiscussionRcs.clear();
		mSingleTotalUnreadCount = 0;
		mGroupTotalUnreadCount = 0;
		mDiscussionTotalUnreadCount = 0;
		for(RecentChat rc : rcs){
			final int activity = rc.getActivityType();
			if(activity == ActivityType.SingleChat){
				mSingleRcs.add(rc);
				mSingleTotalUnreadCount += rc.getUnreadMessageCount();
			}else if(activity == ActivityType.GroupChat){
				mGroupRcs.add(rc);
				mGroupTotalUnreadCount = rc.getUnreadMessageCount();
			}else if(activity == ActivityType.DiscussionChat){
				mDiscussionRcs.add(rc);
				mDiscussionTotalUnreadCount = rc.getUnreadMessageCount();
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	protected void processSingleNotify(String ticker,boolean bSound) {
		final int size = mSingleRcs.size();
		if (mSingleRcs.size() > 0) {
			Notification notification = new Notification(R.drawable.ic_launcher, ticker,
					System.currentTimeMillis());
			notification.number = mSingleTotalUnreadCount;
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			if(bSound){
				final boolean bReduce = mRecentChatUnreadChange == null ? 
						true : mRecentChatUnreadChange.getUnreadMessageCount() == 0;
				if(IMConfigManager.getInstance().isReceiveNewMessageSoundNotify() && !bReduce){
					notification.defaults |= Notification.DEFAULT_SOUND;
				}
				if(IMConfigManager.getInstance().isReceiveNewMessageVibrateNotify() && !bReduce){
					notification.defaults |= Notification.DEFAULT_VIBRATE;
				}
			}
			
			if (mSingleRcs.size() > 1) {
				final Context app = mContext;
				notification.setLatestEventInfo(app, app
						.getString(R.string.app_name), app.getString(
						R.string.statusbar_multi_contact_notify, size,
						mSingleTotalUnreadCount), getPendingIntent(null, null, 0));
			} else {
				final Context app = mContext;
				final RecentChat recentChat = mSingleRcs.iterator().next();
				if (mSingleTotalUnreadCount > 1) {
					notification.setLatestEventInfo(
									app,
									recentChat.getName(),
									app.getString(
											R.string.statusbar_single_contact_multimsg_notify,
											mSingleTotalUnreadCount),
											getPendingIntent(recentChat.getId(), recentChat.getName(), recentChat.getActivityType()));
				} else {
					XMessage m = readLastMessage(recentChat.getId());
					final int nMessageType = m == null ? XMessage.TYPE_TEXT : m
							.getType();
					notification.setLatestEventInfo(
									app,
									app.getString(
											nMessageType == XMessage.TYPE_VOICE ? R.string.statusbar_single_contact_voice_notify
													: R.string.statusbar_single_contact_text_notify,
											recentChat.getName()),
									nMessageType == XMessage.TYPE_VOICE ? app
											.getString(R.string.voice)
											: nMessageType == XMessage.TYPE_PHOTO ? app
													.getString(R.string.photo)
													: m == null ? "" : m.getContent(),
									getPendingIntent(recentChat.getId(), recentChat.getName(), recentChat.getActivityType()));
				}
			}
			
			mNotificationManager.notify(NOTIFY_ID_SINGLECHAT, notification);
		} else {
			clearSingleNotify();
		}
	}
	
	@SuppressWarnings("deprecation")
	protected void processGroupNotify(String ticker,boolean bSound){
		final int size = mGroupRcs.size();
		if (mGroupRcs.size() > 0) {
			Notification notification = new Notification(R.drawable.ic_launcher, ticker,
					System.currentTimeMillis());
			notification.number = mGroupTotalUnreadCount;
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			if(bSound){
				final boolean bReduce = mRecentChatUnreadChange == null ? 
						true : mRecentChatUnreadChange.getUnreadMessageCount() == 0;
				if(IMConfigManager.getInstance().isReceiveNewMessageSoundNotify() && !bReduce){
					notification.defaults |= Notification.DEFAULT_SOUND;
				}
				if(IMConfigManager.getInstance().isReceiveNewMessageVibrateNotify() && !bReduce){
					notification.defaults |= Notification.DEFAULT_VIBRATE;
				}
			}
			
			if (mGroupRcs.size() > 1) {
				final Context app = mContext;
				notification.setLatestEventInfo(app, app
						.getString(R.string.app_name), app.getString(
						R.string.statusbar_multigroupnotify, size,
						mGroupTotalUnreadCount), getPendingIntent(null, null, 0));
			} else {
				final Context app = mContext;
				final RecentChat recentChat = mGroupRcs.iterator().next();
				if (mGroupTotalUnreadCount > 1) {
					notification.setLatestEventInfo(
									app,
									recentChat.getName(),
									app.getString(
											R.string.statusbar_single_contact_multimsg_notify,
											mGroupTotalUnreadCount),
											getPendingIntent(recentChat.getId(), recentChat.getName(), recentChat.getActivityType()));
				} else {
					XMessage m = readLastMessage(recentChat.getId());
					final int nMessageType = m == null ? XMessage.TYPE_TEXT : m
							.getType();
					notification.setLatestEventInfo(
									app,
									recentChat.getName(),
									nMessageType == XMessage.TYPE_VOICE ? app
											.getString(R.string.voice)
											: nMessageType == XMessage.TYPE_PHOTO ? app
													.getString(R.string.photo)
													: m == null ? "" : m
															.getContent(),
									getPendingIntent(recentChat.getId(), recentChat.getName(), recentChat.getActivityType()));
				}
			}
			
			mNotificationManager.notify(NOTIFY_ID_GROUPCHAT, notification);
		} else {
			clearGroupNotify();
		}
	}
	
	@SuppressWarnings("deprecation")
	protected void processDiscussionNotify(String ticker,boolean bSound){
		final int size = mDiscussionRcs.size();
		if (mDiscussionRcs.size() > 0) {
			Notification notification = new Notification(R.drawable.ic_launcher, ticker,
					System.currentTimeMillis());
			notification.number = mDiscussionTotalUnreadCount;
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			if(bSound){
				final boolean bReduce = mRecentChatUnreadChange == null ? 
						true : mRecentChatUnreadChange.getUnreadMessageCount() == 0;
				if(IMConfigManager.getInstance().isReceiveNewMessageSoundNotify() && !bReduce){
					notification.defaults |= Notification.DEFAULT_SOUND;
				}
				if(IMConfigManager.getInstance().isReceiveNewMessageVibrateNotify() && !bReduce){
					notification.defaults |= Notification.DEFAULT_VIBRATE;
				}
			}
			
			if (mDiscussionRcs.size() > 1) {
				final Context app = mContext;
				notification.setLatestEventInfo(app, 
						app.getString(R.string.app_name), 
						app.getString(R.string.statusbar_multidiscussionnotify, size,mDiscussionTotalUnreadCount), 
						getPendingIntent(null, null, 0));
			} else {
				final Context app = mContext;
				final RecentChat recentChat = mDiscussionRcs.iterator().next();
				if (mDiscussionTotalUnreadCount > 1) {
					notification.setLatestEventInfo(
									app,
									recentChat.getName(),
									app.getString(
											R.string.statusbar_single_contact_multimsg_notify,
											mDiscussionTotalUnreadCount),
									getPendingIntent(recentChat.getId(), recentChat.getName(), recentChat.getActivityType()));
				} else {
					XMessage m = readLastMessage(recentChat.getId());
					final int nMessageType = m == null ? XMessage.TYPE_TEXT : m
							.getType();
					notification.setLatestEventInfo(
									app,
									recentChat.getName(),
									nMessageType == XMessage.TYPE_VOICE ? app
											.getString(R.string.voice)
											: nMessageType == XMessage.TYPE_PHOTO ? app
													.getString(R.string.photo)
													: m == null ? "" : m
															.getContent(),
									getPendingIntent(recentChat.getId(), recentChat.getName(), recentChat.getActivityType()));
				}
			}
			
			mNotificationManager.notify(NOTIFY_ID_DISCUSSIONCHAT, notification);
		} else {
			clearDiscussionNotify();
		}
	}
	
	public void clearStatusBar(){
		mNotificationManager.cancel(NOTIFY_ID_SINGLECHAT);
		mNotificationManager.cancel(NOTIFY_ID_GROUPCHAT);
		mNotificationManager.cancel(NOTIFY_ID_DISCUSSIONCHAT);
	}
	
	public void clearSingleNotify(){
		mNotificationManager.cancel(NOTIFY_ID_SINGLECHAT);
	}
	
	public void clearGroupNotify(){
		mNotificationManager.cancel(NOTIFY_ID_GROUPCHAT);
	}
	
	public void clearDiscussionNotify(){
		mNotificationManager.cancel(NOTIFY_ID_DISCUSSIONCHAT);
	}
	
	protected XMessage readLastMessage(String id){
		DBReadLastMessageParam param = new DBReadLastMessageParam();
		param.mId = id;
		param.mSetMessage = true;
		
		AndroidEventManager.getInstance().runEvent(EventCode.DB_ReadLastMessage,
				param);
		
		return param.mMessageOut;
	}
	
	protected PendingIntent getPendingIntent(String id,String name,int activity){
		if(mJumpActivityClass == null){
			return null;
		}
		Intent intent = new Intent(IMKernel.getInstance().getContext(), mJumpActivityClass);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("id", id);
		intent.putExtra("name", name);
		intent.putExtra("activity", activity);
		return PendingIntent.getActivity(IMKernel.getInstance().getContext()
				, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	protected void onInitial(IMKernel kernel) {
		mContext = kernel.getContext();
		mNotificationManager = (NotificationManager)kernel.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		
		AndroidEventManager.getInstance().addEventListener(EventCode.UnreadMessageCountChanged,
				this, false);
		AndroidEventManager.getInstance().addEventListener(EventCode.IM_LoginOuted, this, false);
	}

	@Override
	protected void onRelease() {
		AndroidEventManager.getInstance().removeEventListener(EventCode.UnreadMessageCountChanged, this);
		AndroidEventManager.getInstance().removeEventListener(EventCode.IM_LoginOuted, this);
	}
}
