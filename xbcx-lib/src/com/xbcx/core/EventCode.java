package com.xbcx.core;

public class EventCode{
	
	protected static int CODE_INC = 0;
	
	//public static final int AddEventListener			= CODE_INC++;
	
	public static final int LoginActivityLaunched		= CODE_INC++;
	
	/**
	 * params[0]:FileItem
     */
	public static final int DB_SaveToFolder				= CODE_INC++;
	
	/**
	 * params[0]:List(FileItem)
     */
	public static final int DB_ReadFolder				= CODE_INC++;
	
	/**
	 * params[0]:Collection(FileItem)
     */
	public static final int DB_DeleteFileItem			= CODE_INC++;
	
	/**
	 * params[0]:GCMessage
     */
	public static final int DB_SaveMessage				= CODE_INC++;
	
	/**
	 * params[0]:DBReadMessageParam
     */
	public static final int DB_ReadMessage				= CODE_INC++;
	
	/**
	 * params[0]:otherSideId(不传将删除所有消息)
	 * </br>params[1]:msgId(不传将删除otherSideId对应的消息)
     */
	public static final int DB_DeleteMessage			= CODE_INC++;
	
	/**
	 * params[0]:DBReadLastMessageParam
     */
	public static final int DB_ReadLastMessage			= CODE_INC++;
	
	/**
	 * params[0]:DBReadMessageCountParam
     */
	public static final int DB_ReadMessageCount			= CODE_INC++;
	
	/**
	 * params[0]:List(RecentChat)
     */
	public static final int DB_ReadRecentChat			= CODE_INC++;
	
	/**
	 * params[0]:RecentChat
     */
	public static final int DB_SaveRecentChat			= CODE_INC++;
	
	/**
	 * params[0]:id
     */
	public static final int DB_DeleteRecentChat			= CODE_INC++;
	
	/**
	 * params[0]:userId
	 * </br>reParams[0]:BaseVCard
	 * </br>reParams[1]:updateTime
     */
	public static final int DB_ReadVCard				= CODE_INC++;
	
	/**
	 * params[0]:BaseVCard
     */
	public static final int DB_SaveVCard				= CODE_INC++;
	
	/**
	 * params[0]:msg(String)
     */
	public static final int DB_SaveCommonUseMsg			= CODE_INC++;
	
	/**
	 * reParams[0]:List(String)
     */
	public static final int DB_ReadCommonUseMsg			= CODE_INC++;
	
	/**
	 * params[0]:msg(String)
     */
	public static final int DB_DeleteCommonUseMsg		= CODE_INC++;
	
	/**
	 * params[0]:String(url)
	 * params[1]:String(filepath)
     */
	public static final int Http_Download				= CODE_INC++;
	
	/**
	 * params[0]:String(url)
	 * params[1]:String(filepath)
     */
	public static final int HTTP_DownloadBitmap			= CODE_INC++;
	
	/**
	 * params[0]:type
	 * </br>params[1]:upfile
	 * </br>params[2]:ProgressRunnable
	 * </br>params[3]:Handler
	 * </br>params[4]:AtomicCancel
	 * </br>reParams[0]:url
	 * </br>reParams[1]:thumbUrl
     */
	public static final int HTTP_PostFile				= CODE_INC++;
	
	public static final int AppBackground				= CODE_INC++;
	
	public static final int AppForceground				= CODE_INC++;
	
	/**
	 * params[0]:Object
     */
	public static final int HandleRecentChat			= CODE_INC++;
	
	/**
	 * params[0]:RecentChat(may be null)
     */
	public static final int UnreadMessageCountChanged	= CODE_INC++;
	
	/**
	 * params[0]:List(RecentChat)
     */
	public static final int RecentChatChanged			= CODE_INC++;
	
	/**
	 * params[0]:imuser
	 * params[1]:url
     */
	public static final int DownloadAvatar				= CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int DownloadChatPhoto			= CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int DownloadChatThumbPhoto 		= CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int DownloadChatPhotoPercentChanged = CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int DownloadChatThumbPhotoPercentChanged = CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int UploadChatPhoto 			= CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int UploadChatPhotoPercentChanged = CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int DownloadChatVoice			= CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int UploadChatVoice				= CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int UploadChatVideo				= CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int UploadChatVideoPercentChanged = CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int DownloadChatVideo			= CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int DownloadChatVideoThumb		= CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int DownloadChatVideoPerChanged = CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int DownloadChatVideoThumbPerChanged = CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int UploadChatFile				= CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int UploadChatFilePerChanged	= CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int DownloadChatFile			= CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int DownloadChatFilePerChanged	= CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int DownloadLocation			= CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int VoicePlayStarted			= CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int VoicePlayErrored			= CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int VoicePlayCompletioned		= CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int VoicePlayStoped				= CODE_INC++;
	
	/**
     */
	public static final int IM_LoginStart				= CODE_INC++;
	
	public static final int IM_Login					= CODE_INC++;
	
	public static final int IM_LoginPwdError			= CODE_INC++;
	
	public static final int IM_LoginOuted				= CODE_INC++;
	
	public static final int IM_Conflict					= CODE_INC++;
	
	/**
	 * params[0]:IMStatus
     */
	public static final int IM_StatusQuery				= CODE_INC++;
	
	public static final int IM_ConnectionInterrupt		= CODE_INC++;
	
	/**
	 * reParams() return XMessage
	 */
	public static final int IM_ReceiveMessage			= CODE_INC++;
	
	/**
	 * params[0]:XMessage
     */
	public static final int IM_SendMessage				= CODE_INC++;
	
	/**
	 * params[0]:user
	 * <p>reParams[0]:BaseVCard
     */
	public static final int IM_LoadVCard				= CODE_INC++;
	
	/**
	 * params[0]:BaseVCard
     */
	public static final int IM_SaveVCard				= CODE_INC++;
	
	public static final int IM_UserInfoChanged			= CODE_INC++;
	
	/**
	 * params[0]:IMChatRoom
	 * </br>reParams[0]:boolean(isReJoin)
     */
	public static final int IM_JoinChatRoom				= CODE_INC++;
	
	public static final int IM_LeaveChatRoom			= CODE_INC++;
	
	/**在房间中被踢
	 * </br>params[0]:userId
	 * </br>params[1]:reason
     */
	public static final int IM_OutCast					= CODE_INC++;
	
	/**
	 * params[0]:userId
	 * </br>params[1]:userName
     */
	public static final int IM_AddFriendApply			= CODE_INC++;
	
	/**
	 * params[0]:userId
	 * </br>params[1]:Message
     */
	public static final int IM_AddFriendVerify			= CODE_INC++;
	
	/**
	 * params[0]:userId
     */
	public static final int IM_AddFriendConfirm			= CODE_INC++;
	
	/**
	 * params[0]:userId
     */
	public static final int IM_DeleteFriend				= CODE_INC++;
	
	/**
	 * reParams[0]Collection(IMContact)
     */
	public static final int IM_GetFriendList			= CODE_INC++;
	
	/**
	 * params[0]:userId
     */
	public static final int IM_CheckIsFriend			= CODE_INC++;
	
	/**
	 * params[0]:Collection(IMContact)
     */
	public static final int IM_FriendListChanged		= CODE_INC++;
	
	/**
	 * reParams[0]Collection(IMGroup)
     */
	public static final int IM_GetGroupChatList			= CODE_INC++;
	
	/**
	 * params[0]:groupId
	 * reParams[0]:IMGroup
     */
	public static final int IM_GetGroup					= CODE_INC++;
	
	/**
	 * params[0]:Collection(IMGroup)
     */
	public static final int IM_GroupChatListChanged		= CODE_INC++;
	
	/**
	 * params[0]:name
	 * </br>params[1]:Collection(userId)
	 * </br>reParams[0]:groupId
     */
	public static final int IM_CreateGroupChat			= CODE_INC++;
	
	/**
	 * params[0]:groupId
     */
	public static final int IM_DeleteGroupChat			= CODE_INC++;
	
	/**
	 * params[0]:groupId
     */
	public static final int IM_QuitGroupChat			= CODE_INC++;
	
	/**
	 * params[0]:groupId
	 * </br>params[1]:name
     */
	public static final int IM_ChangeGroupChatName		= CODE_INC++;
	
	/**
	 * params[0]:groupId
	 * </br>params[1]:Collection(userId)
     */
	public static final int IM_AddGroupChatMember		= CODE_INC++;
	
	/**
	 * params[0]:groupId
	 * </br>params[1]:Collection(userId)
     */
	public static final int IM_DeleteGroupChatMember	= CODE_INC++;
	
	/**
	 * params[0]:VerifyType
     */
	public static final int IM_SetVerifyType			= CODE_INC++;
	
	/**
	 * reParams[0]:VerifyType
     */
	public static final int IM_GetVerifyType			= CODE_INC++;
}
