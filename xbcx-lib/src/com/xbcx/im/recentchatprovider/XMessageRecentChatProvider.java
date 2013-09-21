package com.xbcx.im.recentchatprovider;

import android.text.TextUtils;

import com.xbcx.core.XApplication;
import com.xbcx.im.IMKernel;
import com.xbcx.im.IMLocalID;
import com.xbcx.im.RecentChat;
import com.xbcx.im.RecentChatProvider;
import com.xbcx.im.XMessage;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.im.ui.LocalAvatar;
import com.xbcx.library.R;

public class XMessageRecentChatProvider implements RecentChatProvider {
	
	@Override
	public void handleRecentChat(RecentChat rc, Object obj) {
		XMessage xm = (XMessage)obj;
		if(IMLocalID.ID_FriendVerify.equals(xm.getOtherSideId())){
			rc.setName(IMKernel.getInstance().getContext().getString(
					R.string.friend_verify_notice));
			rc.setActivityType(ActivityType.FriendVerify);
			rc.setContent(IMKernel.getInstance().getContext().getString(
					R.string.apply_add_you_friend, xm.getUserName()));
		}else{
			String name = null;
			if(xm.isFromGroup()){
				name = xm.getGroupName();
			}else{
				name = xm.getUserName();
				if(TextUtils.isEmpty(name)){
					name = xm.getUserId();
				}
			}
			if(!TextUtils.isEmpty(name)){
				rc.setName(name);
			}
			
			rc.setContent(getContent(xm));
			if(xm.getFromType() == XMessage.FROMTYPE_SINGLE){
				rc.setActivityType(ActivityType.SingleChat);
			}else if(xm.getFromType() == XMessage.FROMTYPE_GROUP){
				rc.setActivityType(ActivityType.GroupChat);
			}else if(xm.getFromType() == XMessage.FROMTYPE_DISCUSSION){
				rc.setActivityType(ActivityType.DiscussionChat);
			}
		}
		
		rc.setLocalAvatar(getLocalAvatar(xm));
	}

	@Override
	public String getId(Object obj) {
		XMessage xm = (XMessage)obj;
		return xm.getOtherSideId();
	}
	
	@Override
	public boolean isUnread(Object obj) {
		XMessage xm = (XMessage)obj;
		if(!xm.isFromSelf()){
			return !xm.isReaded();
		}
		return false;
	}

	public String getContent(XMessage xm) {
		final int msgType = xm.getType();
		if(msgType == XMessage.TYPE_VOICE){
			return XApplication.getApplication().getString(R.string.voice);
		}else if(msgType == XMessage.TYPE_PHOTO){
			return XApplication.getApplication().getString(R.string.picture);
		}else if(msgType == XMessage.TYPE_VIDEO){
			return XApplication.getApplication().getString(R.string.video);
		}else if(msgType == XMessage.TYPE_FILE){
			return XApplication.getApplication().getString(R.string.file);
		}else{
			return xm.getContent();
		}
	}
	
	public int getLocalAvatar(XMessage xm) {
		if(IMLocalID.ID_FriendVerify.equals(xm.getOtherSideId())){
			return LocalAvatar.FriendVerify;
		}else{
			final int fromType = xm.getFromType();
			if(fromType == XMessage.FROMTYPE_GROUP){
				return LocalAvatar.Group;
			}else if(fromType == XMessage.FROMTYPE_DISCUSSION){
				return LocalAvatar.Discussion;
			}
			return 0;
		}
	}

	@Override
	public long getTime(Object obj) {
		XMessage xm = (XMessage)obj;
		return xm.getSendTime();
	}
}
