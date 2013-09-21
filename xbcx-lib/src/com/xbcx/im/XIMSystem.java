package com.xbcx.im;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.MessageEvent;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Privacy;
import org.jivesoftware.smack.packet.PrivacyItem;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.packet.MUCUser;
import org.jivesoftware.smackx.packet.VCard;

import android.text.TextUtils;
import android.util.SparseArray;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.core.XApplication;
import com.xbcx.core.EventManager.OnEventRunner;
import com.xbcx.im.IMChatRoom;
import com.xbcx.im.IMContact;
import com.xbcx.im.IMRoomMember;
import com.xbcx.im.IMSystem;
import com.xbcx.im.MultiUserChatEx;
import com.xbcx.im.XMessage;
import com.xbcx.im.db.DBColumns;
import com.xbcx.im.db.DBReadLastMessageParam;
import com.xbcx.im.ui.IMGlobalSetting;
import com.xbcx.library.R;

public class XIMSystem extends IMSystem implements OnEventListener{
	
	protected Map<String, String> 	mMapForbiddenSendUserId = new ConcurrentHashMap<String, String>();
	protected Map<String, String> 	mMapForbiddenJoinUserId = new ConcurrentHashMap<String, String>();
	
	protected AndroidEventManager	mEventManager = AndroidEventManager.getInstance();
	
	protected boolean				mIsRegister;
	
	protected SparseArray<OnEventRunner> mMapCodeToRunners = new SparseArray<OnEventRunner>();

	@Override
	public void onCreate() {
		super.onCreate();
		
		mEventManager.addEventListener(EventCode.IM_Login, this, false);
		
		managerRegisterRunner(EventCode.IM_Login, new LoginRunner());
		managerRegisterRunner(EventCode.IM_StatusQuery, new StatusQueryRunner());
		managerRegisterRunner(EventCode.IM_SendMessage, new SendMessageRunner());
		managerRegisterRunner(EventCode.IM_JoinChatRoom, new JoinChatRoomRunner());
		managerRegisterRunner(EventCode.IM_LeaveChatRoom, new LeaveChatRoomRunner());
		managerRegisterRunner(EventCode.IM_LoadVCard, new LoadVCardRunner());
		managerRegisterRunner(EventCode.IM_GetFriendList, new GetFriendListRunner());
		managerRegisterRunner(EventCode.IM_CheckIsFriend, new CheckIsFriendRunner());
		managerRegisterRunner(EventCode.IM_AddFriendApply, new AddFriendApplyRunner());
		managerRegisterRunner(EventCode.IM_AddFriendVerify, new AddFriendVerifyRunner());
		managerRegisterRunner(EventCode.IM_AddFriendConfirm, new AddFriendConfirmRunner());
		managerRegisterRunner(EventCode.IM_DeleteFriend, new DeleteFriendRunner());
		managerRegisterRunner(EventCode.IM_GetGroupChatList, new GetGroupChatListRunner());
		managerRegisterRunner(EventCode.IM_GetGroup, new GetGroupRunner());
		managerRegisterRunner(EventCode.IM_CreateGroupChat, new CreateGroupChatRunner());
		managerRegisterRunner(EventCode.IM_DeleteGroupChat, new DeleteGroupChatRunner());
		managerRegisterRunner(EventCode.IM_QuitGroupChat, new QuitGroupChatRunner());
		managerRegisterRunner(EventCode.IM_AddGroupChatMember, new AddGroupChatMemberRunner());
		managerRegisterRunner(EventCode.IM_ChangeGroupChatName, new ChangeGroupChatNameRunner());
		managerRegisterRunner(EventCode.IM_DeleteGroupChatMember, new DeleteGroupChatMemberRunner());
		managerRegisterRunner(EventCode.IM_GetVerifyType, new GetVerifyTypeRunner());
		managerRegisterRunner(EventCode.IM_SetVerifyType, new SetVerifyTypeRunner());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		int size = mMapCodeToRunners.size();
		for(int index = 0;index < size;++index){
			final int code = mMapCodeToRunners.keyAt(index);
			mEventManager.removeEventRunner(code, mMapCodeToRunners.get(code));
		}
		
		mEventManager.removeEventListener(EventCode.IM_Login, this);
	}
	
	protected void managerRegisterRunner(int eventCode,OnEventRunner runner){
		mEventManager.registerEventRunner(eventCode, runner);
		mMapCodeToRunners.put(eventCode, runner);
	}
	
	@Override
	protected void onLoginOut() {
		super.onLoginOut();
		mEventManager.runEvent(EventCode.IM_LoginOuted);
	}

	@Override
	public void onEventRunEnd(Event event) {
		final int code = event.getEventCode();
		if(code == EventCode.IM_Login){
			handleLoginFinished(event.isSuccess());
		}
	}

	@Override
	protected void onLoginPwdError() {
		super.onLoginPwdError();
		mEventManager.runEvent(EventCode.IM_LoginPwdError);
	}

	@Override
	protected String getLoginNick() {
		return VCardProvider.getInstance().loadUserName(getUser());
	}

	@Override
	protected String getAvatarUrl() {
		return "";
	}
	
	@Override
	protected long getRoomLastMessageSendTime(String strRoomId) {
		DBReadLastMessageParam param = new DBReadLastMessageParam();
		param.mId = strRoomId;
		param.mColumnNames = new String[]{DBColumns.Message.COLUMN_SENDTIME};
		mEventManager.runEvent(EventCode.DB_ReadLastMessage, param);
		return param.getLongValue(DBColumns.Message.COLUMN_SENDTIME, 0);
	}
	
	@Override
	protected void requestLogin(){
		mEventManager.pushEvent(EventCode.IM_Login);
	}

	@Override
	protected void requestJoinRoom(IMChatRoom room) {
		if(mIsConnectionAvailable){
			mEventManager.pushEvent(EventCode.IM_JoinChatRoom,room);
		}
	}

	@Override
	protected void requestReconnect() {
		mEventManager.pushEventDelayed(EventCode.IM_Login,mReConnectIntervalMillis);
	}

	@Override
	protected void onConflict() {
		mEventManager.runEvent(EventCode.IM_Conflict);
		super.onConflict();
	}

	@Override
	protected void onHandleConnectionClosedOnError(){
		super.onHandleConnectionClosedOnError();
		
		mEventManager.pushEvent(EventCode.IM_ConnectionInterrupt, 0);
	}

	@Override
	protected void onFriendListChanged() {
		super.onFriendListChanged();
		
		List<IMContact> list = new LinkedList<IMContact>(mMapIdToContact.values());
		Collections.sort(list);
		
		mEventManager.runEvent(EventCode.IM_FriendListChanged, 
				Collections.unmodifiableCollection(mMapIdToContact.values()));
	}

	@Override
	protected void onGroupChatListChanged() {
		super.onGroupChatListChanged();
		
		mEventManager.runEvent(EventCode.IM_GroupChatListChanged, 
				Collections.unmodifiableCollection(mMapIdToGroup.values()));
	}

	@Override
	protected IMContact onCreateContactByRosterEntry(RosterEntry entry) {
		IMContact contact = new IMContact(removeSuffix(entry.getUser()), entry.getName());
		return contact;
	}
	
	@Override
	public void presenceChanged(Presence presence) {
	}

	@Override
	protected void onInitProviderManager(ProviderManager pm){
		super.onInitProviderManager(pm);
	}

	@Override
	protected void onProcessSingleChatMessage(Chat chat, Message message) {
		super.onProcessSingleChatMessage(chat, message);
	}

	@Override
	protected void onReceiveMessage(XMessage xm) {
		super.onReceiveMessage(xm);
		mEventManager.runEvent(EventCode.IM_ReceiveMessage, xm);
		mEventManager.runEvent(EventCode.DB_SaveMessage, xm);
		mEventManager.runEvent(EventCode.HandleRecentChat, xm);
	}

	@Override
	protected XMessage onCreateXMessage(String strId, int nMessageType) {
		return IMGlobalSetting.msgFactory.createXMessage(strId, nMessageType);
	}

	@Override
	protected void onProcessRoomPresence(Presence presence) {
		super.onProcessRoomPresence(presence);
		
		if(presence.getType() == Presence.Type.available){
			MUCUser mucUser = (MUCUser) presence.getExtension("x",
					"http://jabber.org/protocol/muc#user");
			MUCUser.Item item = mucUser.getItem();
			
			final String strUserId = removeSuffix(item.getJid());
			/*if(!isLocalId(strUserId)){
				DefaultPacketExtension dpe = (DefaultPacketExtension)presence.getExtension(
						ELEMENT_NAME_VCARD, NAMESPACE_VCARD);
				final String strAvatarUrl = dpe.getValue(VCARD_FILED_AVATARURL);
			}*/
			
			
			if(Role.VISITOR.getStringValue().equals(item.getRole())){
				if(!mMapForbiddenSendUserId.containsKey(strUserId)){
					mMapForbiddenSendUserId.put(strUserId, strUserId);
					onForbiddenSendListChanged();
				}
			}else{
				if(mMapForbiddenSendUserId.containsKey(strUserId)){
					mMapForbiddenSendUserId.remove(strUserId);
					onForbiddenSendListChanged();
				}
			}
		}
	}
	
	protected void onForbiddenSendListChanged(){
	}

	@Override
	protected void onRoomMemberChanged() {
		super.onRoomMemberChanged();
		MultiUserChatEx muc = mMultiUserChat.get();
		if(muc != null){
		}
	}

	@Override
	protected IMRoomMember onCreateRoomMember(Presence presence) {
		MUCUser mucUser = (MUCUser) presence.getExtension("x",
                "http://jabber.org/protocol/muc#user");
        MUCUser.Item item = mucUser.getItem();
		
		return new IMRoomMember(removeSuffix(item.getJid()), 
							StringUtils.parseResource(presence.getFrom()),
							Role.valueOf(item.getRole()).toString());
	}

	protected List<IMRoomMember> doGetRoomMember(){
		final MultiUserChat muc = mMultiUserChat.get();
		List<IMRoomMember> listMember = new ArrayList<IMRoomMember>();
		if(muc != null){
			Iterator<String> it = muc.getOccupants();
			while(it.hasNext()){
				final String strUser = it.next();
				Occupant occupant = muc.getOccupant(strUser);
				IMRoomMember member = new IMRoomMember(
						removeSuffix(occupant.getJid()), 
						occupant.getNick(),
						Role.valueOf(occupant.getRole()).toString());
				
				listMember.add(member);
			}
		}
		return listMember;
	}
	
	@Override
	protected void onBlackListChanged() {
		super.onBlackListChanged();
	}

	@Override
	public void banned(String actor, String reason) {
		super.banned(actor, reason);
		mEventManager.runEvent(EventCode.IM_OutCast, getUser(),reason);
	}
	
	protected abstract class IMEventRunner implements OnEventRunner{
		
		private	  List<PacketCollector> mListPacketCollector;
		
		@Override
		public void onEventRun(Event event) throws Exception{
			if (canExecute()) {
				execute(event);
			}else{
				if(this instanceof Delayable){
					mEventManager.addEventListener(EventCode.IM_Login, 
							new OnEventListener() {
								@Override
								public void onEventRunEnd(Event event) {
									synchronized (IMEventRunner.this) {
										IMEventRunner.this.notify();
									}
								}
							}, true);
					synchronized (this) {
						wait(TIMEOUT_IMEVENT);
					}
					if(canExecute()){
						execute(event);
					}
				}
			}
		}
		
		protected boolean canExecute(){
			return mIsConnectionAvailable;
		}
		
		protected void managePacketCollector(PacketCollector collector){
			if(mListPacketCollector == null){
				mListPacketCollector = new ArrayList<PacketCollector>();
			}
			mListPacketCollector.add(collector);
		}
		
		protected void execute(Event event) throws Exception{
			try {
				XApplication.getLogger().info(getClass().getName() + " execute");
				event.setSuccess(onExecute(event));
			} catch (XMPPException e) {
				if("No response from the server.".equals(e.getMessage())){
					onTimeout();
				}
				throw e;
			} finally {
				if(mListPacketCollector != null){
					for(PacketCollector c : mListPacketCollector){
						c.cancel();
					}
				}
				XApplication.getLogger().info(getClass().getName() + " execute:" + event.isSuccess());
			}
		}
		
		protected abstract boolean 	onExecute(Event event) throws Exception;
		
		protected void onTimeout(){
			if(mIsConnectionAvailable){
				mConnection.disconnect();
			}
		}
	}
	
	private class StatusQueryRunner implements OnEventRunner{
		@Override
		public void onEventRun(Event event) throws Exception {
			IMStatus status = (IMStatus)event.getParamAtIndex(0);
			status.mIsLogining = mIsConnecting;
			status.mIsLoginSuccess = mIsConnectionAvailable;
		}
	}
	
	private class LoginRunner extends IMEventRunner{

		@Override
		protected boolean onExecute(Event event) throws Exception {
			mEventManager.runEvent(EventCode.IM_LoginStart);
			doLogin(mIsRegister);
			return true;
		}

		@Override
		protected boolean canExecute() {
			return true;
		}

		@Override
		protected void onTimeout() {
		}
	}
	
	private class SendMessageRunner extends IMEventRunner{
		@Override
		protected boolean onExecute(Event event) throws Exception {
			XMessage xm = (XMessage)event.getParamAtIndex(0);
			
			boolean bSuccess = false;
			try{
				if(xm.getFromType() == XMessage.FROMTYPE_GROUP){
					if(isSelfInGroup(xm.getGroupId())){
						doSend(xm);
						bSuccess = true;
					}
				}else{
					doSend(xm);
					bSuccess = true;
				}
			}finally{
				xm.setSended();
				xm.setSendSuccess(bSuccess);
				xm.updateDB();
			}
			
			return bSuccess;
		}
		
	}
	
	private class JoinChatRoomRunner extends IMEventRunner{

		@Override
		public void onEventRun(Event event) throws Exception {
			try{
				super.onEventRun(event);
			}finally{
				handleJoinRoomFinished(event.isSuccess());
			}
		}

		@Override
		protected boolean onExecute(Event event) throws Exception {
			final IMChatRoom chatRoom = (IMChatRoom)event.getParamAtIndex(0);
			
			boolean bRejoin = false;
			if(mAtomicDisconnectRoom.get() != null){
				bRejoin = true;
			}
			event.addReturnParam(Boolean.valueOf(bRejoin));
			
			doJoin(chatRoom);
			
			return true;
		}
	}
	
	private class LeaveChatRoomRunner extends IMEventRunner{
		@Override
		protected boolean onExecute(Event event) throws Exception {
			doLeave();
			mAtomicDisconnectRoom.set(null);
			return true;
		}

		@Override
		protected boolean canExecute() {
			return true;
		}
	}
	
	private class LoadVCardRunner extends IMEventRunner{
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String userId = (String)event.getParamAtIndex(0);
			VCard vcard = new VCard();
			vcard.load(mConnection, addSuffixUserJid(userId));
			BaseVCard bVcard = new BaseVCard(userId);
			bVcard.addAttribute("name", vcard.getNickName());
			bVcard.addAttribute("avatarurl", vcard.getField(VCARD_FILED_AVATARURL));
			
			onSetVCardAttris(bVcard, vcard);
			
			event.addReturnParam(bVcard);
			
			return true;
		}
	}
	
	protected void onSetVCardAttris(BaseVCard bVcard,VCard vcard){
	}
	
	private class GetFriendListRunner extends IMEventRunner{
		@Override
		protected boolean onExecute(Event event) throws Exception {
			event.addReturnParam(
					Collections.unmodifiableCollection(mMapIdToContact.values()));
			return true;
		}
	}
	
	private class CheckIsFriendRunner extends IMEventRunner{
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String userId = (String)event.getParamAtIndex(0);
			return isFriend(userId);
		}
	}
	
	private class GetGroupChatListRunner extends IMEventRunner{
		@Override
		protected boolean onExecute(Event event) throws Exception {
			event.addReturnParam(
					Collections.unmodifiableCollection(mMapIdToGroup.values()));
			return true;
		}
	}
	
	private class GetGroupRunner extends IMEventRunner{
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String groupId = (String)event.getParamAtIndex(0);
			event.addReturnParam(mMapIdToGroup.get(groupId));
			return true;
		}

		@Override
		protected boolean canExecute() {
			return true;
		}
	}
	
	private class AddFriendApplyRunner extends IMEventRunner{
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String id = (String)event.getParamAtIndex(0);
			final String name = (String)event.getParamAtIndex(1);
			RosterPacket rosterPacket = new RosterPacket();
			rosterPacket.setType(IQ.Type.SET);
			rosterPacket.setFrom(mConnection.getUser());
			RosterPacket.Item item = new RosterPacket.Item(addSuffixUserJid(id), name);
			
			rosterPacket.addRosterItem(item);
			PacketCollector collector = mConnection
					.createPacketCollector(new PacketIDFilter(rosterPacket
							.getPacketID()));
			managePacketCollector(collector);
			mConnection.sendPacket(rosterPacket);
			IQ response = (IQ) collector.nextResult(SmackConfiguration
					.getPacketReplyTimeout());
			checkResultIQ(response);
			return true;
		}
	}
	
	private class AddFriendVerifyRunner extends IMEventRunner{
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String id = (String)event.getParamAtIndex(0);
			Chat chat = getOrCreateChat(id,XMessage.FROMTYPE_SINGLE);
			
			final String strVerifyText = (String)event.getParamAtIndex(1);
			Message message = new Message();
			final MessageEvent me = new MessageEvent();
			me.mAttris.addAttribute("kind", "addfriendask");
			me.setContent(strVerifyText);
			message.addExtension(me);
			message.attributes.addAttribute("nick", getLoginNick());
			chat.sendMessage(message);
			
			return true;
		}
	}
	
	private class AddFriendConfirmRunner extends IMEventRunner{
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String id = (String)event.getParamAtIndex(0);
			if(TextUtils.isEmpty(id)){
				return false;
			}
			Chat chat = getOrCreateChat(id,XMessage.FROMTYPE_SINGLE);
			Message message = new Message();
			final MessageEvent me = new MessageEvent();
			me.mAttris.addAttribute("kind", "addfriendconfirm");
			message.addExtension(me);
			message.attributes.addAttribute("nick", getLoginNick());
			chat.sendMessage(message);
			return true;
		}
	}
	
	private class DeleteFriendRunner extends IMEventRunner{
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String id = (String)event.getParamAtIndex(0);
			mRoster.removeEntry(mRoster.getEntry(addSuffixUserJid(id)));
			return true;
		}
	}
	
	private class CreateGroupChatRunner extends IMEventRunner{
		@SuppressWarnings("unchecked")
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String name = (String)event.getParamAtIndex(0);
			final Collection<String> ids = (Collection<String>)event.getParamAtIndex(1);
			final List<String> jids = new ArrayList<String>();
			for(String id : ids){
				jids.add(addSuffixUserJid(id));
			}
			event.addReturnParam(StringUtils.parseName(
					mRoster.createGroupChat(name, jids, TIMEOUT_IMEVENT)));
			return true;
		}
	}
	
	private class DeleteGroupChatRunner extends IMEventRunner{
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String groupId = (String)event.getParamAtIndex(0);
			IMGroup group = mMapIdToGroup.get(groupId);
			final String groupName = group == null ? "" : group.getName();
			mRoster.deleteGroupChat(addSuffixGroupChatJid(groupId), TIMEOUT_IMEVENT);
			
			try{
				XMessage xm = onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
				xm.setContent(getString(R.string.group_prompt_removed_group,getString(R.string.you)));
				xm.setFromType(XMessage.FROMTYPE_GROUP);
				xm.setGroupId(groupId);
				xm.setGroupName(groupName);
				xm.setSendTime(System.currentTimeMillis());
				onReceiveMessage(xm);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return true;
		}
	}
	
	private class QuitGroupChatRunner extends IMEventRunner{
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String groupId = (String)event.getParamAtIndex(0);
			IMGroup group = mMapIdToGroup.get(groupId);
			final String groupName = group == null ? "" : group.getName();
			mRoster.quitGroupChat(addSuffixGroupChatJid(groupId), TIMEOUT_IMEVENT);
			
			try{
				XMessage xm = onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
				xm.setContent(getString(R.string.group_prompt_quited_group,getString(R.string.you)));
				xm.setFromType(XMessage.FROMTYPE_GROUP);
				xm.setGroupId(groupId);
				xm.setGroupName(groupName);
				xm.setSendTime(System.currentTimeMillis());
				onReceiveMessage(xm);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return true;
		}
	}
	
	private class ChangeGroupChatNameRunner extends IMEventRunner{
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String groupId = (String)event.getParamAtIndex(0);
			final String name = (String)event.getParamAtIndex(1);
			RosterEntry re = mRoster.getEntry(addSuffixGroupChatJid(groupId));
			if(re != null && re.isGroupChat()){
				re.changeGroupName(name, TIMEOUT_IMEVENT);
				return true;
			}
			return false;
		}
	}
	
	private class AddGroupChatMemberRunner extends IMEventRunner{
		@SuppressWarnings("unchecked")
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String groupId = (String)event.getParamAtIndex(0);
			final Collection<String> ids = (Collection<String>)event.getParamAtIndex(1);
			RosterEntry re = mRoster.getEntry(addSuffixGroupChatJid(groupId));
			if(re != null && re.isGroupChat()){
				Collection<String> jids = new ArrayList<String>();
				for(String id : ids){
					jids.add(addSuffixUserJid(id));
				}
				re.addChilds(jids, TIMEOUT_IMEVENT);
				return true;
			}
			return false;
		}
	}
	
	private class DeleteGroupChatMemberRunner extends IMEventRunner{
		@SuppressWarnings("unchecked")
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String groupId = (String)event.getParamAtIndex(0);
			final Collection<String> ids = (Collection<String>)event.getParamAtIndex(1);
			RosterEntry re = mRoster.getEntry(addSuffixGroupChatJid(groupId));
			if(re != null && re.isGroupChat()){
				final Collection<String> jids = new ArrayList<String>();
				for(String id : ids){
					jids.add(addSuffixUserJid(id));
				}
				re.deleteChilds(jids, TIMEOUT_IMEVENT);
				return true;
			}
			return false;
		}
	}
	
	private class GetVerifyTypeRunner extends IMEventRunner{
		@Override
		protected boolean onExecute(Event event) throws Exception {
			event.addReturnParam(VerifyType.valueOf(mVerifyType));
			return true;
		}

		@Override
		protected boolean canExecute() {
			return true;
		}
	}
	
	private class SetVerifyTypeRunner extends IMEventRunner{
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final VerifyType verifyType = (VerifyType)event.getParamAtIndex(0);
			
			TypePrivacy privacy = new TypePrivacy();
			privacy.setType(IQ.Type.SET);
			privacy.setPrivacyType("auth");
			privacy.setListType(verifyType.getValue());
			privacy.setPrivacyList("default", new ArrayList<PrivacyItem>());
			PacketCollector collector = mConnection.createPacketCollector(
					new PacketIDFilter(privacy.getPacketID()));
			managePacketCollector(collector);
			mConnection.sendPacket(privacy);
			Privacy result = (Privacy)collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
			checkResultIQ(result);
			
			mVerifyType = verifyType.getValue();
			
			return true;
		}
	}
}
