package com.xbcx.im;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.PrivacyList;
import org.jivesoftware.smack.PrivacyListManager;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.MessageEvent;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Privacy;
import org.jivesoftware.smack.packet.PrivacyItem;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.packet.StreamError;
import org.jivesoftware.smack.packet.Message.Body;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.provider.MessageEventProvider;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.muc.UserStatusListener;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.jivesoftware.smackx.packet.MUCUser;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;

import com.xbcx.core.XApplication;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.text.TextUtils;

public abstract class IMSystem extends Service implements ConnectionListener,
												ChatManagerListener,
												RosterListener,
												UserStatusListener,
												ParticipantStatusListener{

	public static final String BODY_TYPE_VOICE 			= "vflink";
	public static final String BODY_TYPE_PHOTO			= "pflink";
	public static final String BODY_TYPE_VIDEO			= "videolink";
	public static final String BODY_TYPE_FILE			= "filelink";
	public static final String BODY_TYPE_LOCATION		= "locationlink";
	
	protected static final String ELEMENT_NAME_VCARD 		= "x";
	protected static final String NAMESPACE_VCARD 			= "vcard-temp:x:update";
	
	protected static final String VCARD_FILED_AVATARURL 	= "DESC";
	protected static final String VCARD_FIELD_ADMIN			= "ADMIN";
	
	protected static final int	TIMEOUT_IMEVENT = 20000;
	
	protected static String	GROUP_FLAG = "broadcast";
	
	protected Context							mContext;
	
	protected String							mServer;
	
	protected XMPPConnection 					mConnection;
	protected Roster							mRoster;
	
	protected Map<String,String> 				mMapIdBlackList = new ConcurrentHashMap<String, String>();
	
	protected AtomicReference<MultiUserChatEx> 	mMultiUserChat = new AtomicReference<MultiUserChatEx>();
	protected AtomicReference<IMChatRoom> 		mAtomicDisconnectRoom = new AtomicReference<IMChatRoom>();
	
	protected Map<String,IMContact> 	mMapIdToContact = new ConcurrentHashMap<String, IMContact>();
	
	protected Map<String,IMGroup>		mMapIdToGroup	= new ConcurrentHashMap<String, IMGroup>();
	
	protected String					mVerifyType;
	
	protected IMLoginInfo				mLoginInfo;
	
	protected AtomicLong 				mAtomicReceiveRoomMessageMinSendTime = new AtomicLong();
	
	protected boolean 	mIsReConnect 	   			= false;
	protected int		mReConnectIntervalMillis 	= 1000;
	
	protected boolean	mIsNetworkMonitoring	= false;
	protected boolean	mIsInitiativeDisConnect = false;
	protected boolean	mIsConnectionAvailable	= false;
	protected boolean	mIsConnecting			= false;
	
	private WeakHashMap<String, String> mMapUserIdToChatThreadId = new WeakHashMap<String, String>();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		
		onInitProviderManager(ProviderManager.getInstance());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mIsConnectionAvailable){
			doLoginOut();
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null){
			try{
				if(intent.hasExtra("imlogininfo")){
					mLoginInfo = (IMLoginInfo)intent.getSerializableExtra("imlogininfo");
					mServer = mLoginInfo.getServer();
				}
				if(intent.hasExtra("reconnect")){
					mIsReConnect = intent.getBooleanExtra("reconnect", false);
				}
				final boolean bLogin = intent.getBooleanExtra("login", false);
				if(bLogin){
					requestLogin();
				}
			}catch(Exception e){
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	protected abstract String getLoginNick();
	
	protected abstract String getAvatarUrl();
	
	protected abstract void requestLogin();
	
	protected boolean isLocalId(String strUserId){
		if(strUserId == null){
			return false;
		}
		if(mLoginInfo != null){
			if(strUserId.equals(mLoginInfo.getUser())){
				return true;
			}
		}
		return false;
	}
	
	protected String	getUser(){
		return mLoginInfo == null ? "" : mLoginInfo.getUser();
	}
	
	protected void doLogin() throws Exception{
		doLogin(false);
	}
	
	protected void doLogin(boolean bRegister) throws Exception{
		if(mIsConnectionAvailable){
			return;
		}
		
		if(mIsConnecting){
			return;
		}
		
		mIsConnecting = true;
		try {
			ConnectionConfiguration cc = new ConnectionConfiguration(
					mLoginInfo.getIP(), mLoginInfo.getPort());
			mConnection = new XMPPConnection(cc);
			
			addLogger();

			PrivacyListManager.getInstanceFor(mConnection);

			mConnection.getChatManager().addChatListener(this);

			mConnection.connect();
			configConnectionFeatures(mConnection);
			mConnection.addConnectionListener(this);

			final String strUsername = mLoginInfo.getUser();
			final String strPassword = mLoginInfo.getPwd();
			try{
				mConnection.login(strUsername, strPassword, "android");
			}catch(XMPPException e){
				if(bRegister){
					try{
						doRegister(strUsername, strPassword);
					}catch(XMPPException e1){
						XMPPError error = e1.getXMPPError();
						if(error != null && error.getCode() == 409){
							mIsReConnect = false;
							onLoginPwdError();
						}
						throw e1;
					}
				}
				throw e;
			}

			mRoster = mConnection.getRoster();
			mRoster.addRosterListener(this);

			Presence presence = new Presence(Presence.Type.available);
			onInterceptLoginPresence(presence);

			mConnection.sendPacket(presence);

			updateContactsByRoster();
			
			onLoginGet();
			
			if(mIsInitiativeDisConnect){
				mConnection.removeConnectionListener(this);
				mConnection.disconnect();
			}else{
				mIsConnectionAvailable = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			mConnection.removeConnectionListener(this);
			if(mConnection.isAuthenticated()){
				mConnection.disconnect();
			}
			throw e;
		} finally {
			mIsConnecting = false;
		}
	}
	
	protected void addLogger(){
		mConnection.addPacketListener(new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				XApplication.getLogger().info("receive:" + packet.toXML());
			}
		}, new PacketFilter() {
			
			@Override
			public boolean accept(Packet arg0) {
				return true;
			}
		});
		mConnection.addPacketSendingListener(new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				XApplication.getLogger().info("send:" + packet.toXML());
			}
		}, new PacketFilter() {
			@Override
			public boolean accept(Packet packet) {
				return true;
			}
		});
	}
	
	protected void onInterceptLoginPresence(Presence presence){
		presence.addExtension(new PacketExtension() {
			public String toXML() {
				return new StringBuffer().append("<ver>")
						.append(SystemUtils.getVersionName(mContext))
						.append("</ver>").toString();
			}

			public String getNamespace() {
				return null;
			}

			public String getElementName() {
				return null;
			}
		});
		presence.addExtension(new PacketExtension() {
			public String toXML() {
				return new StringBuffer("<device>android</device>").toString();
			}

			public String getNamespace() {
				return null;
			}

			public String getElementName() {
				return null;
			}
		});
	}
	
	protected void onLoginPwdError(){
		
	}
	
	protected void loadBlackList() throws Exception {
		PrivacyListManager plm = PrivacyListManager.getInstanceFor(mConnection);
		if (plm != null) {
			String listName = "default";
			Privacy request = new Privacy();
			request.setPrivacyList(listName, new ArrayList<PrivacyItem>());
			Privacy answer = plm.getRequest(request);
			PrivacyList privacyList = new PrivacyList(false, true, listName,
					answer.getPrivacyList(listName));
			for (String strName : answer.getPrivacyListAttributeNames(listName)) {
				privacyList.mAttributeHelper.addAttribute(strName,
						answer.getPrivacyListAttributeValue(listName, strName));
			}

			synchronized (mMapIdBlackList) {
				mMapIdBlackList.clear();
				for (PrivacyItem item : privacyList.getItems()) {
					if (PrivacyItem.Type.jid.equals(item.getType())) {
						final String strId = removeSuffix(item.getValue());
						mMapIdBlackList.put(strId, strId);
					}
				}
			}
			mVerifyType = privacyList.mAttributeHelper.getAttributeValue("type");
		}
	}
	
	protected void onLoginGet() throws Exception{
	}
	
	protected void handleLoginFinished(boolean bSuccess){
		if(bSuccess){
			final IMChatRoom room = mAtomicDisconnectRoom.get();
			if(room != null){
				requestJoinRoom(room);
			}
		}else{
			if(mIsReConnect){
				if(SystemUtils.isNetworkAvailable(mContext)){
					requestReconnect();
				}else{
					startNetworkMonitor();
				}
			}
		}
	}
	
	protected void doLoginOut(){
		mIsInitiativeDisConnect = true;
		
		mIsConnectionAvailable = false;
		
		mIsReConnect = false;
		
		mConnection.removeConnectionListener(this);
		mConnection.disconnect();
		
		onLoginOut();
	}
	
	protected void onLoginOut(){
		
	}
	
	protected void doRegister(String user, String pwd) throws XMPPException{
		Registration reg = new Registration();
		reg.setType(IQ.Type.SET);
		reg.setTo(mConnection.getServiceName());
		reg.setUsername(user);
		reg.setPassword(pwd);
		reg.addAttribute("android", "geolo_createUser_android");
		PacketFilter packetFilter = new AndFilter(
				new PacketIDFilter(reg.getPacketID()), 
				new PacketTypeFilter(IQ.class));
		
		PacketCollector packetCollerctor = null;
		IQ result = null;
		try{
			packetCollerctor = mConnection.createPacketCollector(packetFilter);
			mConnection.sendPacket(reg);
			result = (IQ) packetCollerctor.nextResult(SmackConfiguration.getPacketReplyTimeout());
		}finally{
			packetCollerctor.cancel();
		}
		
		checkResultIQ(result);
	}
	
	protected abstract void requestJoinRoom(IMChatRoom room);
	
	protected abstract void requestReconnect();
	
	protected void doLeave(){
		synchronized (mMultiUserChat) {
			final MultiUserChat multiUserChat = mMultiUserChat.get();
			if(multiUserChat != null){
				removeMultiUserChatListener(multiUserChat);
				multiUserChat.leave();
				mMultiUserChat.set(null);
			}
		}
	}
	
	protected void doJoin(IMChatRoom room) throws XMPPException{
		final String strGroupId = room.getId();
		
		doLeave();
		
		final MultiUserChatEx multiUserChat = new MultiUserChatEx(mConnection, 
				addSuffixRoomJid(strGroupId),room);
		
		mMultiUserChat.set(multiUserChat);
		try{
			addMultiUserChatListener(multiUserChat);
			multiUserChat.join(getLoginNick(),
					null, null, TIMEOUT_IMEVENT);
		}catch(XMPPException e){
			mMultiUserChat.set(null);
			removeMultiUserChatListener(multiUserChat);
			throw e;
		}
	}
	
	protected void handleJoinRoomFinished(boolean bSuccess){
		if(bSuccess){
			mAtomicDisconnectRoom.set(null);
		}else{
			final IMChatRoom chatRoom = mAtomicDisconnectRoom.get();
			if(chatRoom != null){
				requestJoinRoom(chatRoom);
			}
		}
	}
	
	protected void doSend(XMessage xm) throws XMPPException{
		final int nFromType = xm.getFromType();
		if (nFromType == XMessage.FROMTYPE_CHATROOM) {
			final MultiUserChat multiUserChat = mMultiUserChat.get();
			if (multiUserChat != null) {
				Message message = multiUserChat.createMessage();
				onSendInit(message, xm);
				multiUserChat.sendMessage(message);
			}
		} else {
			final String toId = xm.getOtherSideId();
			Chat chat = getOrCreateChat(toId,xm.getFromType());
			Message message = new Message();
			onSendInit(message, xm);
			if(nFromType == XMessage.FROMTYPE_SINGLE){
				message.attributes.addAttribute("nick", getLoginNick());
			}else{
				message.attributes.addAttribute("nick", xm.getGroupName());
				message.getMessageBody(null).attributes.addAttribute("name", getLoginNick());
			}
			chat.sendMessage(message);
		}
	}
	
	protected void onSendInit(Message message,XMessage xm){
		final int messageType = xm.getType();
		if(messageType == XMessage.TYPE_VOICE){
			Body body = message.addBody(null, xm.getVoiceDownloadUrl());
			body.attributes.addAttribute("type", BODY_TYPE_VOICE);
			body.attributes.addAttribute("size", String.valueOf(xm.getVoiceFrameCount()));
		}else if(messageType == XMessage.TYPE_PHOTO){
			Body body = message.addBody(null, xm.getThumbPhotoDownloadUrl());
			body.attributes.addAttribute("type", BODY_TYPE_PHOTO);
			body.attributes.addAttribute("displayname", xm.getDisplayName());
			File file = new File(xm.getPhotoFilePath());
			MessageDetailExtension detail = new MessageDetailExtension(file.length(),
					xm.getPhotoDownloadUrl());
			message.addExtension(detail);
		}else if(messageType == XMessage.TYPE_VIDEO){
			Body body  = message.addBody(null, xm.getVideoDownloadUrl());
			body.attributes.addAttribute("type", BODY_TYPE_VIDEO);
			body.attributes.addAttribute("size", String.valueOf(xm.getVideoSeconds()));
			body.attributes.addAttribute("thumb", xm.getVideoThumbDownloadUrl());
			body.attributes.addAttribute("displayname", xm.getDisplayName());
		}else if(messageType == XMessage.TYPE_FILE){
			Body body = message.addBody(null, xm.getOfflineFileDownloadUrl());
			body.attributes.addAttribute("type", BODY_TYPE_FILE);
			body.attributes.addAttribute("size", String.valueOf(xm.getFileSize()));
			body.attributes.addAttribute("displayname", xm.getDisplayName());
		}else if(messageType == XMessage.TYPE_LOCATION){
			Body body = message.addBody(null, xm.getContent());
			body.attributes.addAttribute("type", BODY_TYPE_LOCATION);
			String s[] = xm.getLocation();
			if(s != null && s.length > 1){
				body.attributes.addAttribute("lat", s[0]);
				body.attributes.addAttribute("lng", s[1]);
			}
		}else{
			message.setBody(xm.getContent());
		}
	}
	
	protected void doAddBlackList(List<String> listUserId) throws XMPPException{
		List<PrivacyItem> listItem = new ArrayList<PrivacyItem>();
		for (String strUserId : listUserId) {
			PrivacyItem item = new PrivacyItem("jid", false, 0);
			item.setValue(addSuffixUserJid(strUserId));
			listItem.add(item);
		}
		if (listItem.size() > 0) {
			TypePrivacy packet = new TypePrivacy();
			packet.setFrom(mConnection.getUser());
			packet.setType(IQ.Type.SET);
			packet.setPrivacyType("addblack");
			packet.setPrivacyList("default", listItem);

			PacketCollector collector = mConnection.createPacketCollector(
					new PacketIDFilter(packet.getPacketID()));
			try{
				mConnection.sendPacket(packet);
				Privacy result = (Privacy) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
				checkResultIQ(result);
			}finally{
				collector.cancel();
			}
			
			synchronized (mMapIdBlackList) {
				for(String strUserId : listUserId){
					mMapIdBlackList.put(strUserId, strUserId);
				}
			}
			
			onBlackListChanged();
			
			List<String> listDelete = new ArrayList<String>();
			for(String strUserId : listUserId){
				if(isFriend(strUserId)){
					listDelete.add(strUserId);
				}
			}
			if(listDelete.size() > 0){
				entriesDeleted(listDelete);
			}
		}
	}
	
	protected void onBlackListChanged(){
	}
	
	protected void doDeleteBlackList(List<String> listUserId) throws XMPPException{
		List<PrivacyItem> listItem = new ArrayList<PrivacyItem>();
		for (String strUserId : listUserId) {
			PrivacyItem item = new PrivacyItem("jid", false, 0);
			item.setValue(addSuffixUserJid(strUserId));
			listItem.add(item);
		}
		if (listItem.size() > 0) {
			TypePrivacy packet = new TypePrivacy();
			packet.setType(IQ.Type.SET);
			packet.setPrivacyType("delblack");
			packet.setPrivacyList("default", listItem);

			PacketCollector collector = mConnection.createPacketCollector(
					new PacketIDFilter(packet.getPacketID()));
			try{
				mConnection.sendPacket(packet);
				Privacy result = (Privacy) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
				checkResultIQ(result);
			}finally{
				collector.cancel();
			}
			
			synchronized (mMapIdBlackList) {
				for(String strUserId : listUserId){
					mMapIdBlackList.remove(strUserId);
				}
			}
			
			onBlackListChanged();
		}
	}
	
	protected void checkResultIQ(IQ result) throws XMPPException{
		if (result == null) {
			throw new XMPPException("No response from the server.");
		} else if (result.getType() == IQ.Type.ERROR) {
			throw new XMPPException(result.getError());
		}
	}
	
	protected void configConnectionFeatures(XMPPConnection xmppConnection) {
		ServiceDiscoveryManager.setIdentityName("Android_IM");
		ServiceDiscoveryManager.setIdentityType("phone");
		ServiceDiscoveryManager.setNonCapsCaching(false);
		ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(xmppConnection);
		if (sdm == null) {
			sdm = new ServiceDiscoveryManager(xmppConnection);
		}
		sdm.addFeature("http://jabber.org/protocol/disco#info");
		sdm.addFeature("http://jabber.org/protocol/caps");
		sdm.addFeature("urn:xmpp:avatar:metadata");
		sdm.addFeature("urn:xmpp:avatar:metadata+notify");
		sdm.addFeature("urn:xmpp:avatar:data");
		sdm.addFeature("http://jabber.org/protocol/nick");
		sdm.addFeature("http://jabber.org/protocol/nick+notify");
		sdm.addFeature("http://jabber.org/protocol/xhtml-im");
		sdm.addFeature("http://jabber.org/protocol/muc");
		sdm.addFeature("http://jabber.org/protocol/commands");
		sdm.addFeature("http://jabber.org/protocol/si/profile/file-transfer");
		sdm.addFeature("http://jabber.org/protocol/si");
		sdm.addFeature("http://jabber.org/protocol/bytestreams");
		sdm.addFeature("http://jabber.org/protocol/ibb");
		sdm.addFeature("http://jabber.org/protocol/feature-neg");
		sdm.addFeature("jabber:iq:privacy");
		sdm.addFeature("vcard-temp");
	}
	
	@Override
	public void connectionClosed() {
		XApplication.getLogger().info("connectionClosed");
		if(!mIsInitiativeDisConnect){
			mIsConnectionAvailable = false;
			onHandleConnectionClosedOnError();
		}
	}

	@Override
	public void connectionClosedOnError(Exception e) {
		e.printStackTrace();
		XApplication.getLogger().info("connectionClosedOnError");
		mIsConnectionAvailable = false;
		
		boolean bConflict = false;
		if(e instanceof XMPPException){
			XMPPException xe = (XMPPException)e;
			StreamError streamError = xe.getStreamError();
			if(streamError != null){
				if("conflict".equals(streamError.getCode())){
					bConflict = true;
					onConflict();
				}
			}
		}
		
		if(!bConflict){
			onHandleConnectionClosedOnError();
		}
	}
	
	protected void onConflict(){
		stopSelf();
	}

	protected void onHandleConnectionClosedOnError(){
		requestLogin();
		
		synchronized (mMultiUserChat) {
			MultiUserChatEx multiUserChat = mMultiUserChat.get();
			if(multiUserChat != null){
				mAtomicDisconnectRoom.set(multiUserChat.getChatRoom());
				mMultiUserChat.set(null);
			}
		}
	}
	
	@Override
	public void reconnectingIn(int seconds) {
	}

	@Override
	public void reconnectionSuccessful() {
	}

	@Override
	public void reconnectionFailed(Exception e) {
	}
	
	@Override
	public void chatCreated(Chat chat, boolean createdLocally) {
		mMapUserIdToChatThreadId.put(removeSuffix(chat.getParticipant()), chat.getThreadID());
		if(!createdLocally){
			chat.addMessageListener(mMessageListenerSingleChat);
		}
	}
	
	protected void updateContactsByRoster(){
		Collection<RosterEntry> listEntry = mRoster.getEntries();
		synchronized (mMapIdToContact) {
			mMapIdToContact.clear();
			mMapIdToGroup.clear();
			for(RosterEntry entry : listEntry){
				onAddRosterEntry(entry);
			}
		}
		onFriendListChanged();
		onGroupChatListChanged();
	}
	
	protected void onAddRosterEntry(RosterEntry entry){
		if(entry.isGroupChat()){
			IMGroup group = new IMGroup(removeSuffix(entry.getUser()),entry.getName());
			for(RosterEntry child : entry.getChilds()){
				final String childId = removeSuffix(child.getUser());
				group.addMember(new IMContact(childId, child.getName()));
				group.setRole(childId, child.getGroupAdmin());
			}
			mMapIdToGroup.put(group.getId(), group);
		}else{
			if(!isLocalId(removeSuffix(entry.getUser()))){
				IMContact contact = onCreateContactByRosterEntry(entry);
				mMapIdToContact.put(contact.getId(), contact);
			}
		}
	}
	
	protected boolean isFriend(String strIMUser){
		return mMapIdToContact.containsKey(strIMUser);
	}
	
	protected boolean isSelfInGroup(String groupId){
		return mMapIdToGroup.containsKey(groupId);
	}
	
	@Override
	public void entriesAdded(Collection<String> addresses) {
		for(String strJid : addresses){
			RosterEntry entry = mRoster.getEntry(strJid);
			onAddRosterEntry(entry);
		}
		onFriendListChanged();
		onGroupChatListChanged();
	}

	@Override
	public void entriesUpdated(Collection<String> addresses) {
		synchronized (mMapIdToContact) {
			for(String strJid : addresses){
				RosterEntry entry = mRoster.getEntry(strJid);
				if(entry.isGroupChat()){
					IMGroup group = new IMGroup(removeSuffix(entry.getUser()),entry.getName());
					for(RosterEntry child : entry.getChilds()){
						final String childId = removeSuffix(child.getUser());
						group.addMember(new IMContact(childId, child.getName()));
						group.setRole(childId, child.getGroupAdmin());
					}
					mMapIdToGroup.put(group.getId(), group);
				}else{
					IMContact contact = onCreateContactByRosterEntry(entry);
					if(!TextUtils.isEmpty(entry.getName()) ||
							!mMapIdToContact.containsKey(contact.getId())){
						mMapIdToContact.put(contact.getId(), contact);
					}
				}
			}
		}
		onFriendListChanged();
		onGroupChatListChanged();
	}

	@Override
	public void entriesDeleted(Collection<String> addresses) {
		synchronized (mMapIdToContact) {
			for(String jid : addresses){
				if(jid.contains(GROUP_FLAG)){
					mMapIdToGroup.remove(removeSuffix(jid));
				}else{
					mMapIdToContact.remove(removeSuffix(jid));
				}
			}
		}
		onFriendListChanged();
		onGroupChatListChanged();
	}
	
	protected void onFriendListChanged(){
	}
	
	protected void onGroupChatListChanged(){
	}

	protected abstract IMContact onCreateContactByRosterEntry(RosterEntry entry);
	
	@Override
	public void presenceChanged(Presence presence) {
	}

	protected void onInitProviderManager(ProviderManager pm){
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());
        pm.addIQProvider("offline","http://jabber.org/protocol/offline", new OfflineMessageRequest.Provider());
        pm.addIQProvider("vCard","vcard-temp", new org.jivesoftware.smackx.provider.VCardProvider());
        pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin", new MUCAdminProvider());
        pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());
        pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
        
        pm.addExtensionProvider("event", "jabber:client", new MessageEventProvider());
 
        pm.addExtensionProvider("offline","http://jabber.org/protocol/offline", new OfflineMessageInfo.Provider());
        pm.addExtensionProvider("x", "jabber:x:delay", new DelayInformationProvider());
        pm.addExtensionProvider("detail", "jabber:client", new MessageDetailProvider());
        pm.addExtensionProvider("detail", "", new MessageDetailProvider());

        ChatStateExtension.Provider chatState = new ChatStateExtension.Provider();   
        pm.addExtensionProvider("active", "http://jabber.org/protocol/chatstates", chatState);
        pm.addExtensionProvider("composing", "http://jabber.org/protocol/chatstates", chatState);
        pm.addExtensionProvider("paused", "http://jabber.org/protocol/chatstates", chatState);
        pm.addExtensionProvider("inactive", "http://jabber.org/protocol/chatstates", chatState);
        pm.addExtensionProvider("gone", "http://jabber.org/protocol/chatstates", chatState);
        pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user", new MUCUserProvider());
	}
	
	protected Chat getOrCreateChat(String strUserId,int fromType){
		String strChatThreadId = mMapUserIdToChatThreadId.get(strUserId);
		Chat chat = mConnection.getChatManager().getThreadChat(strChatThreadId);
		if(chat == null){
			chat = mConnection.getChatManager().createChat(
					fromType == XMessage.FROMTYPE_GROUP ? 
							addSuffixGroupChatJid(strUserId) : addSuffixUserJid(strUserId), 
					mMessageListenerSingleChat);
		}
		return chat;
	}
	
	protected void addMultiUserChatListener(MultiUserChat multiUserChat){
		multiUserChat.addMessageListener(mPacketListenerMultiUserChat);
		multiUserChat.addParticipantListener(mPacketListenerMultiParticipant);
		multiUserChat.addPresenceInterceptor(mPacketInterceptorMultiPresence);
		multiUserChat.addParticipantStatusListener(this);
		multiUserChat.addUserStatusListener(this);
	}
	
	protected void removeMultiUserChatListener(MultiUserChat multiUserChat){
		multiUserChat.removeMessageListener(mPacketListenerMultiUserChat);
		multiUserChat.removeParticipantListener(mPacketListenerMultiParticipant);
		multiUserChat.removePresenceInterceptor(mPacketInterceptorMultiPresence);
		multiUserChat.removeUserStatusListener(this);
		multiUserChat.removeParticipantStatusListener(this);
	}
	
	protected void startNetworkMonitor(){
		if(!mIsNetworkMonitoring){
			mIsNetworkMonitoring = true;
			IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
			mContext.registerReceiver(mBroadcastReceiverNetworkMonitor, intentFilter);
		}
	}
	
	protected BroadcastReceiver mBroadcastReceiverNetworkMonitor = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(SystemUtils.isNetworkAvailable(context)){
				requestLogin();
				stopNetworkMonitor();
			}
		}
	};
	
	protected void stopNetworkMonitor(){
		mContext.unregisterReceiver(mBroadcastReceiverNetworkMonitor);
		mIsNetworkMonitoring = false;
	}
	
	protected String addSuffixUserJid(String strUserId){
		return strUserId + "@" + mServer;
	}
	
	protected String	addSuffixRoomJid(String groupId){
		return groupId + "@conference." + mServer;
	}
	
	protected String 	addSuffixGroupChatJid(String groupId){
		return groupId + "@" + GROUP_FLAG + "." + mServer;
	}
	
	protected String	removeSuffix(String strJid){
		int nIndex = strJid.lastIndexOf("@");
		if(nIndex != -1){
			return strJid.substring(0, nIndex);
		}
		return strJid;
	}
	
	private MessageListener mMessageListenerSingleChat = new MessageListener() {
		@Override
		public void processMessage(Chat chat, Message message) {
			try{
				onProcessSingleChatMessage(chat, message);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};
	
	private PacketListener mPacketListenerMultiUserChat = new PacketListener() {
		@Override
		public void processPacket(Packet packet) {
			try{
				onProcessMultiChatMessage((Message)packet);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};
	
	protected void onProcessSingleChatMessage(Chat chat,Message message){
		if(message.getType().equals(Message.Type.error)){
			return;
		}
		
		Body body = message.getMessageBody(null);
		if (body != null) {
			onProcessSingleChatBody(chat, message, body);
		}
		
		PacketExtension pe = message.getExtension("event", "jabber:client");
		if(pe != null && pe instanceof MessageEvent){
			final MessageEvent event = (MessageEvent)pe;
			onProcessSingleChatEvent(chat, message, event);
		}
	}
	
	protected void onProcessSingleChatBody(Chat chat,Message message,Body body){
		XMessage xm = onCreateXMessage(onCreateReceiveXMessageId(message), 
				parseMessageType(body));
		if(chat.getParticipant().contains(GROUP_FLAG)){
			xm.setFromType(XMessage.FROMTYPE_GROUP);
			xm.setGroupId(removeSuffix(chat.getParticipant()));
			xm.setGroupName(message.attributes.getAttributeValue("nick"));
			xm.setUserId(removeSuffix(body.attributes.getAttributeValue("sponsor")));
			xm.setUserName(body.attributes.getAttributeValue("name"));
		}else{
			final String strUserId = removeSuffix(chat.getParticipant());
			
			xm.setFromType(XMessage.FROMTYPE_SINGLE);
			xm.setUserId(strUserId);
			xm.setUserName(message.attributes.getAttributeValue("nick"));
		}
		onSetMessageCommonValue(xm, message);
		
		onReceiveMessage(xm);
	}
	
	protected void onProcessSingleChatEvent(Chat chat,Message message,MessageEvent event){
		final String strKind = event.mAttris.getAttributeValue("kind");
		
		if("addfriendask".equals(strKind)){
			final String strUserId = removeSuffix(chat.getParticipant());
		
			XMessage xm = onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_TEXT);
			xm.setFromType(XMessage.FROMTYPE_GROUP);
			xm.setGroupId(IMLocalID.ID_FriendVerify);
			xm.setUserId(strUserId);
			xm.setUserName(message.attributes.getAttributeValue("nick"));
			xm.setFromSelf(false);
			xm.setContent(event.getContent());
			xm.setSendTime(parseMessageSendTime(message));
			onReceiveMessage(xm);
		}else if("addfriendconfirm".equals(strKind)){
			onProcessAddFriendConfirmKindMessage(chat, message, event);
		}else if("addfriend".equals(strKind)){
			onProcessAddFriendKindMessage(chat, message, event);
		}else{
			XMessage xm = null;
			if ("creategroup".equals(strKind)) {
				xm = onProcessCreateGroupMessage(chat, message, event);
			} else if ("removegroup".equals(strKind)) {
				final String strName = event.mAttris.getAttributeValue("name");
				xm = onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
				xm.setContent(getString(R.string.group_prompt_removed_group,strName));
			} else if ("quitgroup".equals(strKind)) {
				xm = onProcessQuitGroupMessage(chat, message, event);
			} else if ("rename".equals(strKind)) {
				final String strJidModifier = event.mAttris.getAttributeValue("sponsor");
				final String strIdModifier = StringUtils.parseName(strJidModifier);
				xm = onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
				if(isLocalId(strIdModifier)){
					xm.setContent(getString(R.string.group_prompt_you_changed_group_name));
				}else{
					xm.setContent(event.mAttris.getAttributeValue("name") + 
							getString(R.string.group_prompt_changed_group_name));
				}
				xm.setReaded(true);
			} else if ("addmember".equals(strKind)) {
				final String strUserJidInviter = event.mAttris.getAttributeValue("sponsor");
				final String strUserIdInviter = StringUtils.parseName(strUserJidInviter);
				boolean bNotify = false;
				StringBuffer sb = new StringBuffer();
				if (isLocalId(strUserIdInviter)) {
					sb.append(getString(R.string.group_prompt_you_invite));
					boolean bFirst = true;
					for (MessageEvent.Member member : event.getMembers()) {
						final String strJid = member.mAttris.getAttributeValue("jid");
						if("1".equals(member.mAttris.getAttributeValue("new"))){
							if (!isLocalId(StringUtils.parseName(strJid))) {
								if(bFirst){
									sb.append(member.mAttris.getAttributeValue("name"));
									bFirst = false;
								}else{
									sb.append("、").append(member.mAttris.getAttributeValue("name"));
								}
							}
						}
					}
				} else {
					int nAddPos = 0;
					boolean bFirst = true;
					boolean bFoundInviter = false;
					for (MessageEvent.Member member : event.getMembers()) {
						final String strJid = member.mAttris.getAttributeValue("jid");
						if (strUserJidInviter.equals(strJid)) {
							String strInviterName = member.mAttris.getAttributeValue("name");
							if(strInviterName == null){
								strInviterName = message.attributes.getAttributeValue("nick");
								if(strInviterName == null){
									strInviterName = "";
								}
							}
							bFoundInviter = true;
							sb.insert(0, strInviterName + " " + getString(R.string.invite));
							nAddPos = sb.length();
						}else if("1".equals(member.mAttris.getAttributeValue("new"))){
							if (isLocalId(StringUtils.parseName(strJid))) {
								bNotify = true;
								final int nPos = bFoundInviter ? nAddPos : 0;
								if(bFirst){
									sb.insert(nPos, getString(R.string.you));
									bFirst = false;
								}else{
									sb.insert(nPos, getString(R.string.you) + "、");
								}
							} else{
								if(bFirst){
									sb.append(member.mAttris.getAttributeValue("name"));
									bFirst = false;
								}else{
									sb.append("、").append(member.mAttris.getAttributeValue("name"));
								}
							}
						}
					}
				}
				sb.append(" " + getString(R.string.group_prompt_added_group));
				xm = onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
				xm.setContent(sb.toString());
				xm.setReaded(!bNotify);
			} else if ("kicked".equals(strKind)) {
				final String strName = event.mAttris.getAttributeValue("name");
				xm = onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
				xm.setContent(getString(R.string.group_prompt_you_been_removed,
						strName));
			} else if ("removemember".equals(strKind)) {
				final String strUserJidInviter = event.mAttris.getAttributeValue("sponsor");
				StringBuffer sb = new StringBuffer();
				if (isLocalId(strUserJidInviter)) {
					sb.append(getString(R.string.group_prompt_you_had));
					boolean bFirst = true;
					for (MessageEvent.Member member : event.getMembers()) {
						if(bFirst){
							sb.append(member.mAttris.getAttributeValue("name"));
							bFirst = false;
						}else{
							sb.append("、").append(member.mAttris.getAttributeValue("name"));
						}
					}
				} else {
					sb.append(event.mAttris.getAttributeValue("name"))
					.append(" " + getString(R.string.group_prompt_had));
					boolean bFirst = true;
					for (MessageEvent.Member member : event.getMembers()) {
						if(bFirst){
							sb.append(member.mAttris.getAttributeValue("name"));
							bFirst = false;
						}else{
							sb.append("、").append(member.mAttris.getAttributeValue("name"));
						}
					}
				}
				
				sb.append(" " + getString(R.string.group_prompt_removed_member));
				xm = onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
				xm.setContent(sb.toString());
				xm.setReaded(true);
			} else{
				xm = onProcessUnHandleKindGroupMessage(strKind, chat, message, event);
			}
			
			if(xm != null){
				xm.setFromType(XMessage.FROMTYPE_GROUP);
				xm.setGroupId(removeSuffix(chat.getParticipant()));
				xm.setGroupName(message.attributes.getAttributeValue("nick"));
				xm.setSendTime(parseMessageSendTime(message));
				
				onGroupPromptMessageBuildEnd(xm,strKind,chat,message,event);
				
				onReceiveMessage(xm);
			}
		}
	}
	
	protected void onProcessAddFriendConfirmKindMessage(Chat chat,Message message,MessageEvent event){
		final String strUserId = removeSuffix(chat.getParticipant());
		
		if(!isFriend(strUserId)){
			mRoster.reload();
		}
		
		XMessage xm = onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
		xm.setUserId(strUserId);
		String name = message.attributes.getAttributeValue("nick");
		if(TextUtils.isEmpty(name)){
			name = VCardProvider.getInstance().loadUserName(strUserId);
		}
		xm.setUserName(name);
		xm.setFromType(XMessage.FROMTYPE_SINGLE);
		xm.setSendTime(parseMessageSendTime(message));
		
		xm.setContent(getString(R.string.add_friend_confirm,xm.getUserName()));
		
		onReceiveMessage(xm);
	}
	
	protected void onProcessAddFriendKindMessage(Chat chat,Message message,MessageEvent event){
		final String strUserId = removeSuffix(chat.getParticipant());
		
		if(!isFriend(strUserId)){
			mRoster.reload();
		}
		
		XMessage xm = onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
		xm.setUserId(strUserId);
		String name = message.attributes.getAttributeValue("nick");
		if(TextUtils.isEmpty(name)){
			name = VCardProvider.getInstance().loadUserName(strUserId);
		}
		xm.setUserName(name);
		xm.setFromType(XMessage.FROMTYPE_SINGLE);
		xm.setSendTime(parseMessageSendTime(message));
		
		xm.setContent(getString(R.string.add_you_friend,xm.getUserName()));
		
		onReceiveMessage(xm);
	}
	
	protected XMessage onProcessCreateGroupMessage(Chat chat,Message message,MessageEvent event){
		final String strUserJidInviter = event.mAttris.getAttributeValue("sponsor");
		final String strUserIdInviter = StringUtils.parseName(strUserJidInviter);
		
		StringBuffer sb = new StringBuffer();
		boolean bLocal = false;
		if (isLocalId(strUserIdInviter)) {
			sb.append(getString(R.string.group_prompt_you_invite));
			boolean bFirst = true;
			for (MessageEvent.Member member : event.getMembers()) {
				final String strId = removeSuffix(member.mAttris.getAttributeValue("jid"));
				if (!isLocalId(strId)) {
					if(bFirst){
						sb.append(member.mAttris.getAttributeValue("name"));
						bFirst = false;
					}else{
						sb.append("、").append(member.mAttris.getAttributeValue("name"));
					}
				}
			}
			bLocal = true;
		} else {
			for (MessageEvent.Member member : event.getMembers()) {
				final String strJid = member.mAttris.getAttributeValue("jid");
				if (strUserJidInviter.equals(strJid)) {
					sb.insert(0, member.mAttris.getAttributeValue("name") + 
							" " + getString(R.string.group_prompt_invite_you));
				} else if (!isLocalId(removeSuffix(strJid))) {
					sb.append("、").append(member.mAttris.getAttributeValue("name"));
				}
			}
		}
		
		sb.append(" " + getString(R.string.group_prompt_added_group));
		XMessage im = onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
		im.setContent(sb.toString());
		if(bLocal){
			im.setReaded(true);
		}
		return im;
	}
	
	protected XMessage onProcessQuitGroupMessage(Chat chat,Message message,MessageEvent event){
		final String strNick = event.mAttris.getAttributeValue("name");
		XMessage im = onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
		im.setContent(getString(R.string.group_prompt_quited_group,strNick));
		im.setReaded(true);
		return im;
	}
	
	protected XMessage onProcessUnHandleKindGroupMessage(String kind,Chat chat,Message message,MessageEvent event){
		return null;
	}
	
	protected void onGroupPromptMessageBuildEnd(XMessage xm,String kind,Chat chat,Message message,MessageEvent event){
	}
	
	protected void onProcessMultiChatMessage(Message message){
		if(message.getType().equals(Message.Type.error)){
			return;
		}
		
		Body body = message.getMessageBody(null);
		if(body != null){
			MultiUserChat muc = mMultiUserChat.get();
			final String strFrom = message.getFrom();
			final Occupant occupant = muc.getOccupant(strFrom);
			String strUserId = null;
			if (occupant != null) {
				strUserId = removeSuffix(occupant.getJid());
				if(isLocalId(strUserId)){
					return;
				}
			}

			final String strNickname = StringUtils.parseResource(strFrom);
			
			if (!TextUtils.isEmpty(strNickname)) {
				if (strUserId == null) {
					PacketExtension pe = message.getExtension("x", "jabber:x:delay");
					if (pe instanceof DelayInformation) {
						DelayInformation di = (DelayInformation) pe;
						strUserId = removeSuffix(di.getFrom());
						if(isLocalId(strUserId)){
							return;
						}
					}
				}
			}
			
			long lSendTime = parseMessageSendTime(message);
			if(lSendTime < mAtomicReceiveRoomMessageMinSendTime.get()){
				return;
			}
			
			XMessage xm = onCreateXMessage(onCreateReceiveXMessageId(message), 
					parseMessageType(body));
			xm.setFromType(XMessage.FROMTYPE_CHATROOM);
			xm.setGroupId(removeSuffix(strFrom));
			onSetMessageCommonValue(xm, message);
			if (strUserId != null) {
				xm.setUserId(strUserId);
				xm.setUserName(strNickname);
			}
			
			onReceiveMessage(xm);
		}
	}
	
	protected String	onCreateReceiveXMessageId(Message message){
		return XMessage.buildMessageId();
	}
	
	protected abstract XMessage onCreateXMessage(String strId,int nMessageType);
	
	protected void	onReceiveMessage(XMessage xm){
		
	}
	
	protected int parseMessageType(Body body){
		int nType = XMessage.TYPE_TEXT;
		if(body.attributes != null){
			final String strBodyType = body.attributes.getAttributeValue("type");
			if(BODY_TYPE_VOICE.equals(strBodyType)){
				nType = XMessage.TYPE_VOICE;
			}else if(BODY_TYPE_PHOTO.equals(strBodyType)){
				nType = XMessage.TYPE_PHOTO;
			}else if(BODY_TYPE_VIDEO.equals(strBodyType)){
				nType = XMessage.TYPE_VIDEO;
			}else if(BODY_TYPE_FILE.equals(strBodyType)){
				nType = XMessage.TYPE_FILE;
			}else if(BODY_TYPE_LOCATION.equals(strBodyType)){
				nType = XMessage.TYPE_LOCATION;
			}
		}
		
		return nType;
	}
	
	protected void onSetMessageCommonValue(XMessage xm,Message m){
		Body body = m.getMessageBody(null);
		xm.setFromSelf(false);
		xm.setContent(body.getMessage());
		xm.setSendTime(parseMessageSendTime(m));
		
		onSetXMessageUrl(xm, m,body);
		
		final String displayname = body.attributes.getAttributeValue("displayname");
		if(!TextUtils.isEmpty(displayname)){
			xm.setDisplayName(displayname);
		}
	}
	
	protected void onSetXMessageUrl(XMessage xm,Message m,Body body){
		final int nType = xm.getType();
		if(nType == XMessage.TYPE_VOICE){
			final String strSize = body.attributes.getAttributeValue("size");
			xm.setVoiceFrameCount(Integer.parseInt(strSize));
		}else if(nType == XMessage.TYPE_PHOTO){
			MessageDetailExtension de = (MessageDetailExtension)m.getExtension("detail", "jabber:client");
			xm.setPhotoDownloadUrl(de.getContent());
		}else if(nType == XMessage.TYPE_VIDEO){
			xm.setVideoThumbDownloadUrl(body.attributes.getAttributeValue("thumb"));
			xm.setVideoDownloadUrl(body.getMessage());
			xm.setVideoSeconds(Integer.parseInt(body.attributes.getAttributeValue("size")));
		}else if(nType == XMessage.TYPE_FILE){
			xm.setOfflineFileDownloadUrl(body.getMessage());
			xm.setFileSize(Long.parseLong(body.attributes.getAttributeValue("size")));
		}else if(nType == XMessage.TYPE_LOCATION){
			xm.setLocation(Double.parseDouble(body.attributes.getAttributeValue("lat")), 
					Double.parseDouble(body.attributes.getAttributeValue("lng")));
		}
	}
	
	protected long parseMessageSendTime(Message message){
		PacketExtension pe = message.getExtension("x","jabber:x:delay");
		if(pe != null && pe instanceof DelayInformation){
			DelayInformation di = (DelayInformation)pe;
			return di.getStamp().getTime();
		}
		return new Date().getTime();
	}
	
	private PacketListener mPacketListenerMultiParticipant = new PacketListener() {
		@Override
		public void processPacket(Packet packet) {
			onProcessRoomPresence((Presence)packet);
		}
	};
	
	private PacketInterceptor mPacketInterceptorMultiPresence = new PacketInterceptor() {
		@Override
		public void interceptPacket(Packet packet) {
			onInterceptJoinRoomPresence((Presence)packet);
		}
	};
	
	protected void onProcessRoomPresence(Presence presence){
		MultiUserChatEx muc = mMultiUserChat.get();
		if(muc != null){
			if(presence.getType() == Presence.Type.available){
				IMRoomMember member = onCreateRoomMember(presence);
				if(member != null){
					muc.mMapIdToRoomMember.put(member.getId(),member);
				}
			}else if(presence.getType() == Presence.Type.unavailable){
				MUCUser mucUser = (MUCUser) presence.getExtension("x",
		                "http://jabber.org/protocol/muc#user");
				muc.mMapIdToRoomMember.remove(removeSuffix(mucUser.getItem().getJid()));
			}
			onRoomMemberChanged();
		}
	}
	
	protected IMRoomMember onCreateRoomMember(Presence presence){
		MUCUser mucUser = (MUCUser) presence.getExtension("x",
                "http://jabber.org/protocol/muc#user");
        MUCUser.Item item = mucUser.getItem();
		return new IMRoomMember(removeSuffix(item.getJid()), 
				StringUtils.parseResource(presence.getFrom()), 
				item.getRole());
	}
	
	protected void onRoomMemberChanged(){
		
	}
	
	protected void onInterceptJoinRoomPresence(Presence presence){
		onInterceptJoinRoomPresenceMessageTime(presence);
		
		DefaultPacketExtension packetExtension = 
					new DefaultPacketExtension(ELEMENT_NAME_VCARD, NAMESPACE_VCARD);
		
		onInterceptJoinRoomPresenceVCardExtension(packetExtension);
		
		presence.addExtension(packetExtension);
	}
	
	protected void onInterceptJoinRoomPresenceMessageTime(Presence presence){
		final String strRoomJid = StringUtils.parseBareAddress(presence.getTo());
		final String strRoomId = removeSuffix(strRoomJid);
		final long lLastSendTime = getRoomLastMessageSendTime(strRoomId);
		mAtomicReceiveRoomMessageMinSendTime.set(lLastSendTime);
		if(lLastSendTime != 0){
			final String strTime = Packet.XEP_0082_UTC_FORMAT.format(new Date(lLastSendTime));
			presence.addExtension(new PacketExtension() {
				@Override
				public String toXML() {
					return new StringBuffer("<item roomid=\"").append(strRoomId).append("\" ")
							.append("since=\"").append(strTime).append("\"/>").toString();
				}
				@Override
				public String getNamespace() {
					return null;
				}
				@Override
				public String getElementName() {
					return null;
				}
			});
		}
	}
	
	protected abstract long getRoomLastMessageSendTime(String strRoomId);
	
	protected void onInterceptJoinRoomPresenceVCardExtension(DefaultPacketExtension packetExtension){
		final String strAvatarUrl = getAvatarUrl();
		packetExtension.setValue(VCARD_FILED_AVATARURL, strAvatarUrl == null ? "" : strAvatarUrl);
	}

	@Override
	public void kicked(String actor, String reason) {
	}

	@Override
	public void voiceGranted() {
	}

	@Override
	public void voiceRevoked() {
	}

	@Override
	public void banned(String actor, String reason) {
		doLeave();
	}

	@Override
	public void membershipGranted() {
	}

	@Override
	public void membershipRevoked() {
	}

	@Override
	public void moderatorGranted() {
	}

	@Override
	public void moderatorRevoked() {
	}

	@Override
	public void ownershipGranted() {
	}

	@Override
	public void ownershipRevoked() {
	}

	@Override
	public void adminGranted() {
	}

	@Override
	public void adminRevoked() {
	}

	@Override
	public void joined(String participant) {
	}

	@Override
	public void left(String participant) {
	}

	@Override
	public void kicked(String participant, String actor, String reason) {
	}

	@Override
	public void voiceGranted(String participant) {
	}

	@Override
	public void voiceRevoked(String participant) {
	}

	@Override
	public void banned(String participant, String actor, String reason) {
	}

	@Override
	public void membershipGranted(String participant) {
	}

	@Override
	public void membershipRevoked(String participant) {
	}

	@Override
	public void moderatorGranted(String participant) {
	}

	@Override
	public void moderatorRevoked(String participant) {
	}

	@Override
	public void ownershipGranted(String participant) {
	}

	@Override
	public void ownershipRevoked(String participant) {
	}

	@Override
	public void adminGranted(String participant) {
	}

	@Override
	public void adminRevoked(String participant) {
	}

	@Override
	public void nicknameChanged(String participant, String newNickname) {
	}
}
