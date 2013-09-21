package com.xbcx.im;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.SharedPreferenceDefine;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.im.db.DatabaseManager;
import com.xbcx.im.db.IMDatabaseManager;
import com.xbcx.im.db.PublicDatabaseManager;
import com.xbcx.im.messageprocessor.FileMessageDownloadProcessor;
import com.xbcx.im.messageprocessor.FileMessageUploadProcessor;
import com.xbcx.im.messageprocessor.LocationMessageDownloadProcessor;
import com.xbcx.im.messageprocessor.PhotoMessageDownloadProcessor;
import com.xbcx.im.messageprocessor.PhotoMessageUploadProcessor;
import com.xbcx.im.messageprocessor.VideoMessageDownloadProcessor;
import com.xbcx.im.messageprocessor.VideoMessageUploadProcessor;
import com.xbcx.im.messageprocessor.VoiceMessageDownloadProcessor;
import com.xbcx.im.messageprocessor.VoiceMessageUploadProcessor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class IMKernel implements OnEventListener{

	public static IMKernel getInstance(){
		if(sInstance == null){
			sInstance = new IMKernel();
		}
		return sInstance;
	}
	
	private static IMKernel sInstance;
	
	protected AndroidEventManager		mEventManager = AndroidEventManager.getInstance();
	
	protected Map<String, ModuleInfo> 	mMapModuleNameToModuleClass = new HashMap<String, ModuleInfo>();
	
	@SuppressWarnings("rawtypes")
	protected Class								mIMSystemClass;
	protected Class<? extends DatabaseManager>	mPublicDatabaseManagerClass;
	protected Class<? extends DatabaseManager> 	mIMDatabaseManagerClass;
	protected IMModule							mIMDatabaseManager;
	
	protected Context					mContext;
	
	protected String					mUserId;
	protected String					mLastUserId;
	protected boolean					mIsConflict;
	
	protected List<IMModule> 			mPublicModules = new LinkedList<IMModule>();
	protected List<IMModule>			mUserModules = new LinkedList<IMModule>();
	
	protected String					mUploadVoiceUrl;
	protected String					mUploadVideoUrl;
	protected String					mUploadVideoThumbUrl;
	protected String					mUploadPhotoUrl;
	protected String					mUploadFileUrl;
	
	protected IMKernel(){
		mPublicDatabaseManagerClass = PublicDatabaseManager.class;
		mIMDatabaseManagerClass = IMDatabaseManager.class;
		//registerModule(PublicDatabaseManager.class.getName(), PublicDatabaseManager.class, false);
		registerModule(IMConfigManager.class.getName(), IMConfigManager.class, false);
		registerModule(VCardProvider.class.getName(), VCardProvider.class, false);
		registerModule(FileMessageDownloadProcessor.class.getName(), FileMessageDownloadProcessor.class, false);
		registerModule(FileMessageUploadProcessor.class.getName(), FileMessageUploadProcessor.class, false);
		registerModule(PhotoMessageDownloadProcessor.class.getName(), PhotoMessageDownloadProcessor.class, false);
		registerModule(PhotoMessageUploadProcessor.class.getName(), PhotoMessageUploadProcessor.class, false);
		registerModule(VideoMessageDownloadProcessor.class.getName(), VideoMessageDownloadProcessor.class, false);
		registerModule(VideoMessageUploadProcessor.class.getName(), VideoMessageUploadProcessor.class, false);
		registerModule(VoiceMessageDownloadProcessor.class.getName(),VoiceMessageDownloadProcessor.class, false);
		registerModule(VoiceMessageUploadProcessor.class.getName(), VoiceMessageUploadProcessor.class, false);
		registerModule(LocationMessageDownloadProcessor.class.getName(), LocationMessageDownloadProcessor.class, false);
		
		registerModule(IMFilePathManager.class.getName(), IMFilePathManagerExt.class, true);
		//registerModule(IMDatabaseManager.class.getName(), IMDatabaseManager.class, true);
		registerModule(RecentChatManager.class.getName(), RecentChatManager.class, true);
		
		registerIMSystem(XIMSystem.class);
		
		mEventManager.addEventListener(EventCode.IM_Conflict, this, false);
	}
	
	public void initial(Context context){
		mContext = context.getApplicationContext();
		
		try{
			IMModule dm = null;
			Constructor<? extends DatabaseManager> c = mPublicDatabaseManagerClass.getDeclaredConstructor((Class[])null);
			c.setAccessible(true);
			dm = (DatabaseManager)c.newInstance();
			dm.initial(this);
			
			c = mIMDatabaseManagerClass.getDeclaredConstructor((Class[])null);
			c.setAccessible(true);
			mIMDatabaseManager = c.newInstance();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		for(ModuleInfo mi : mMapModuleNameToModuleClass.values()){
			try{
				IMModule module = null;
				Constructor<? extends IMModule> c = mi.mModuleClass.getDeclaredConstructor((Class[])null);
				c.setAccessible(true);
				module = (IMModule)c.newInstance();
				if(mi.mIsUserModule){
					mUserModules.add(module);
				}else{
					mPublicModules.add(module);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		for(IMModule module : mPublicModules){
			module.initial(this);
		}
	}
	
	public void registerModule(String moduleName,Class<? extends IMModule> moduleClass,boolean bUserModule){
		final ModuleInfo mi = mMapModuleNameToModuleClass.get(moduleName);
		if(mi != null){
			if(!mi.mModuleClass.isAssignableFrom(moduleClass)){
				throw new IllegalArgumentException("replace module:" + moduleName + " must be extends " + moduleName);
			}else if(mi.mIsUserModule != bUserModule){
				throw new IllegalArgumentException("replace module:" + moduleName + " must be userModule:" + mi.mIsUserModule);
			}
		}
		mMapModuleNameToModuleClass.put(moduleName, new ModuleInfo(moduleClass, bUserModule));
	}
	
	@SuppressWarnings("rawtypes")
	public void registerIMSystem(Class imClass){
		mIMSystemClass = imClass;
	}
	
	public static boolean canLogin(Context context){
		final SharedPreferences sp = context.getSharedPreferences(SharedPreferenceDefine.SP_IM, 0);
		String user = sp.getString(SharedPreferenceDefine.KEY_USER, null);
		String pwd = sp.getString(SharedPreferenceDefine.KEY_PWD, null);
		if(!TextUtils.isEmpty(user) &&
				!TextUtils.isEmpty(pwd)){
			return true;
		}
		return false;
	}
	
	public static boolean isIMConnectionAvailable(){
		IMStatus status = new IMStatus();
		getInstance().mEventManager.runEvent(EventCode.IM_StatusQuery,status);
		return status.mIsLoginSuccess;
	}
	
	public static boolean isSendingMessage(String msgId){
		return false;
	}
	
	public static IMStatus getIMStatus(){
		IMStatus status = new IMStatus();
		getInstance().mEventManager.runEvent(EventCode.IM_StatusQuery,status);
		status.mIsConflict = getInstance().mIsConflict;
		return status;
	}
	
	public void	setUploadPhotoUrl(String url){
		mUploadPhotoUrl = url;
	}
	
	public void setUploadVoiceUrl(String url){
		mUploadVoiceUrl = url;
	}
	
	public void	setUploadVideoThumbUrl(String url){
		mUploadVideoThumbUrl = url;
	}
	
	public void setUploadVideoUrl(String url){
		mUploadVideoUrl = url;
	}
	
	public void setUploadFileUrl(String url){
		mUploadFileUrl = url;
	}
	
	public String getUploadPhotoUrl(){
		return mUploadPhotoUrl;
	}
	
	public String getUploadVoiceUrl(){
		return mUploadVoiceUrl;
	}
	
	public String getUploadVideoThumbUrl(){
		return mUploadVideoThumbUrl;
	}
	
	public String getUploadVideoUrl(){
		return mUploadVideoUrl;
	}
	
	public String getUploadFileUrl(){
		return mUploadFileUrl;
	}
	
	public static String getLocalUser(){
		return getInstance().getUserId();
	}
	
	public static boolean isLocalUser(String user){
		if(user == null){
			return false;
		}
		return user.equals(getLocalUser());
	}
	
	public Context	getContext(){
		return mContext;
	}
	
	public String	getUserId(){
		return mUserId;
	}
	
	public boolean	isFriend(String userId){
		Event e = mEventManager.runEvent(EventCode.IM_CheckIsFriend, userId);
		return e.isSuccess();
	}
	
	public boolean	isSelfInGroup(String groupId){
		return getGroup(groupId) != null;
	}
	
	public IMGroup	getGroup(String groupId){
		Event e = mEventManager.runEvent(EventCode.IM_GetGroup, groupId);
		if(e.isSuccess()){
			return (IMGroup)e.getReturnParamAtIndex(0);
		}
		return null;
	}
	
	public VerifyType getVerifyType(){
		Event e = mEventManager.runEvent(EventCode.IM_GetVerifyType);
		VerifyType type = (VerifyType)e.getReturnParamAtIndex(0);
		if(type == null){
			type = VerifyType.TYPE_NONE;
		}
		return type;
	}
	
	public void		loginUserId(IMLoginInfo li,boolean bReconnect){
		mUserId = li.getUser();
		if(mUserId != null){
			if(!mUserId.equals(mLastUserId)){
				mIMDatabaseManager.initial(this);
				for(IMModule module : mUserModules){
					module.initial(this);
				}
				mLastUserId = mUserId;
			}
			mIsConflict = false;
			Intent intent = new Intent(mContext, mIMSystemClass);
			intent.putExtra("imlogininfo", li);
			intent.putExtra("reconnect", bReconnect);
			intent.putExtra("login", true);
			mContext.startService(intent);
		}
	}
	
	public void		logout(){
		Intent intent = new Intent(mContext, mIMSystemClass);
		mContext.stopService(intent);
	}
	
	@Override
	public void onEventRunEnd(Event event) {
		final int code = event.getEventCode();
		if(code == EventCode.IM_Conflict){
			mIsConflict = true;
			mContext.getSharedPreferences(SharedPreferenceDefine.SP_IM, 0)
			.edit().remove(SharedPreferenceDefine.KEY_PWD).commit();
		}
	}
	
	protected static class ModuleInfo{
		public Class<? extends IMModule> 	mModuleClass;
		
		public boolean 						mIsUserModule;
		
		public ModuleInfo(Class<? extends IMModule> c,boolean bUserModule){
			mModuleClass = c;
			mIsUserModule = bUserModule;
		}
	}
}
