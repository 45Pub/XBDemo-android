package com.xbcx.im;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.muc.MultiUserChat;

public class MultiUserChatEx extends MultiUserChat {

	private IMChatRoom mChatRoom;
	
	public Map<String, IMRoomMember> mMapIdToRoomMember = new ConcurrentHashMap<String, IMRoomMember>();
	
	public MultiUserChatEx(Connection connection, String room,IMChatRoom chatRoom) {
		super(connection, room);
		mChatRoom = chatRoom;
	}

	public String 	getRoomName(){
		return mChatRoom.getName();
	}
	
	public IMChatRoom getChatRoom(){
		return mChatRoom;
	}
}
