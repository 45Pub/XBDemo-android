package com.xbcx.jianhua;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.BaseUIProvider;
import com.xbcx.core.SharedPreferenceDefine;
import com.xbcx.core.XApplication;
import com.xbcx.im.IMKernel;
import com.xbcx.im.IMLoginInfo;
import com.xbcx.im.IMMessage;
import com.xbcx.im.RecentChatManager;
import com.xbcx.im.VCardProvider;
import com.xbcx.im.folder.FolderManager;
import com.xbcx.im.messageprocessor.FileMessageUploadProcessor;
import com.xbcx.im.messageprocessor.PhotoMessageUploadProcessor;
import com.xbcx.im.messageprocessor.VideoMessageUploadProcessor;
import com.xbcx.im.messageprocessor.VoiceMessageUploadProcessor;
import com.xbcx.im.recentchatprovider.XMessageRecentChatProvider;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.im.ui.EditViewQQExpressionProvider;
import com.xbcx.im.ui.IMGlobalSetting;
import com.xbcx.im.ui.LocalAvatar;
import com.xbcx.im.ui.SendPlugin;
import com.xbcx.im.ui.StatusBarManager;
import com.xbcx.im.ui.TypeSendPlugin;
import com.xbcx.jianhua.activity.DetailUserActivity;
import com.xbcx.jianhua.activity.JHFriendVerifyChatActivity;
import com.xbcx.jianhua.activity.JHGroupChatActivity;
import com.xbcx.jianhua.activity.JHSingleChatActivity;
import com.xbcx.jianhua.activity.JHViewPictureActivity;
import com.xbcx.jianhua.activity.MainActivity;
import com.xbcx.jianhua.activity.WorkDocActivity;
import com.xbcx.jianhua.httprunner.ChangeUserInfoRunner;
import com.xbcx.jianhua.httprunner.GetOrgRunner;
import com.xbcx.jianhua.httprunner.GetUserInfoRunner;
import com.xbcx.jianhua.httprunner.LoginRunner;
import com.xbcx.jianhua.httprunner.PostFileRunner;
import com.xbcx.jianhua.httprunner.RegisterRunner;
import com.xbcx.jianhua.httprunner.SearchOrgRunner;
import com.xbcx.jianhua.im.JHFileMessageUploadProcessor;
import com.xbcx.jianhua.im.JHFolderManager;
import com.xbcx.jianhua.im.JHIMSystem;
import com.xbcx.jianhua.im.JHPhotoMessageUploadProcessor;
import com.xbcx.jianhua.im.JHVCardProvider;
import com.xbcx.jianhua.im.JHVideoMessageUploadProcessor;
import com.xbcx.jianhua.im.JHVoiceMessageUploadProcessor;

public class JHApplication extends XApplication {
	
	//postfile(//0私聊语音,1私聊图片,2私聊视频以及视频缩略图，3群聊语音,4群聊图片,5群聊视频及缩略图 6个人头像 7 其他文件)
	
	public static final String KEY_HTTP = "quyd75403l!@#~|}{][=";
	
	public static final String KEY_EMAIL = "email";

	@Override
	public void onCreate() {
		super.onCreate();
		
		final IMKernel kernel = IMKernel.getInstance();
		kernel.registerIMSystem(JHIMSystem.class);
		kernel.registerModule(VCardProvider.class.getName(), JHVCardProvider.class, false);
		kernel.registerModule(FolderManager.class.getName(), JHFolderManager.class, false);
		kernel.registerModule(StatusBarManager.class.getName(), StatusBarManager.class, false);
		kernel.registerModule(VoiceMessageUploadProcessor.class.getName(), JHVoiceMessageUploadProcessor.class, false);
		kernel.registerModule(PhotoMessageUploadProcessor.class.getName(), JHPhotoMessageUploadProcessor.class, false);
		kernel.registerModule(VideoMessageUploadProcessor.class.getName(), JHVideoMessageUploadProcessor.class, false);
		kernel.registerModule(FileMessageUploadProcessor.class.getName(), JHFileMessageUploadProcessor.class, false);
		
		final String url = URLUtils.PostFile;
		kernel.setUploadFileUrl(url);
		kernel.setUploadPhotoUrl(url);
		kernel.setUploadVideoThumbUrl(url);
		kernel.setUploadVideoUrl(url);
		kernel.setUploadVoiceUrl(url);
		
		kernel.initial(this);
		
		BaseUIProvider.setBaseUIFactoryClass(JHBaseUIFactory.class);
		
		StatusBarManager.getInstance().setJumpActivityClass(MainActivity.class);
		
		IMGlobalSetting.photoSendPreview = true;
		IMGlobalSetting.photoSendPreviewActivityClass = JHViewPictureActivity.class;
		IMGlobalSetting.editViewExpProviders.add(EditViewQQExpressionProvider.class);
		IMGlobalSetting.mapEditViewBtnIdToSendPlugin.put(R.id.btnPhoto, new TypeSendPlugin(SendPlugin.SENDTYPE_PHOTO_ALL));
		IMGlobalSetting.mapEditViewBtnIdToSendPlugin.put(R.id.btnCamera, new TypeSendPlugin(SendPlugin.SENDTYPE_VIDEO_ALL));
		IMGlobalSetting.mapEditViewBtnIdToSendPlugin.put(R.id.btnFile, new TypeSendPlugin(SendPlugin.SENDTYPE_FILE));
		
		LocalAvatar.registerAvatarResId(LocalAvatar.Group, R.drawable.avatar_discussion);
		LocalAvatar.registerAvatarResId(LocalAvatar.FriendVerify, R.drawable.avatar_request);
		
		RecentChatManager.getInstance().registerRecentChatProvider(
				IMMessage.class.getName(),new XMessageRecentChatProvider());
		
		ActivityType.registerActivityClassName(ActivityType.SingleChat, JHSingleChatActivity.class.getName());
		ActivityType.registerActivityClassName(ActivityType.GroupChat, JHGroupChatActivity.class.getName());
		ActivityType.registerActivityClassName(ActivityType.FriendVerify, JHFriendVerifyChatActivity.class.getName());
		ActivityType.registerActivityClassName(ActivityType.UserDetailActivity, DetailUserActivity.class.getName());
		ActivityType.registerActivityClassName(ActivityType.ChooseFileActivity, WorkDocActivity.class.getName());
		
		initHttpRunner();
		
		final SharedPreferences sp = getSharedPreferences(SharedPreferenceDefine.SP_IM, 0);
		String user = sp.getString(SharedPreferenceDefine.KEY_USER, null);
		String pwd = sp.getString(SharedPreferenceDefine.KEY_PWD, null);
		if(!TextUtils.isEmpty(user) && 
				!TextUtils.isEmpty(pwd)){
			kernel.loginUserId(createLoginInfo(user, pwd), true);
		}
	}
	
	protected static IMLoginInfo createLoginInfo(String imUser,String imPwd){
		return new IMLoginInfo(imUser, imPwd, "imdemo.com", "112.124.60.201", 31004);
	}
	
	public static String getUser(){
		return getApplication().getSharedPreferences(SharedPreferenceDefine.SP_IM, 0)
		.getString(KEY_EMAIL, null);
	}
	
	public static void saveBaseInfo(BaseInfo bi){
		final SharedPreferences sp = getApplication()
				.getSharedPreferences(SharedPreferenceDefine.SP_IM, 0);
		sp.edit().putString(SharedPreferenceDefine.KEY_USER, bi.getIMUser())
		.putString(SharedPreferenceDefine.KEY_PWD,bi.getIMPwd())
		.putString(KEY_EMAIL, bi.getUser())
		.commit();
		
		JHVCardProvider.getInstance().saveInfo(bi.getIMUser(), bi.getName(), bi.getAvatarUrl());
		
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try{
			fos = getApplication().openFileOutput("login", 0);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(bi);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(oos != null){
				try{
					oos.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	public static BaseInfo	getBaseInfo(){
		ObjectInputStream ois = null;
		try{
			FileInputStream fis = getApplication().openFileInput("login");
			ois = new ObjectInputStream(fis);
			return (BaseInfo)ois.readObject();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(ois != null){
				try{
					ois.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public static void login(String imUser,String imPwd){
		IMKernel.getInstance().loginUserId(createLoginInfo(imUser, imPwd), true);
	}

	public static void logout(){
		getApplication().getSharedPreferences(SharedPreferenceDefine.SP_IM, 0)
		.edit().remove(SharedPreferenceDefine.KEY_PWD)
		.commit();
		IMKernel.getInstance().logout();
	}
	
	private void initHttpRunner(){
		AndroidEventManager eventManager = AndroidEventManager.getInstance();
		eventManager.registerEventRunner(JHEventCode.HTTP_Login, new LoginRunner());
		eventManager.registerEventRunner(JHEventCode.HTTP_Register, new RegisterRunner());
		eventManager.registerEventRunner(JHEventCode.HTTP_GetOrg, new GetOrgRunner());
		eventManager.registerEventRunner(JHEventCode.HTTP_SearchOrg, new SearchOrgRunner());
		eventManager.registerEventRunner(JHEventCode.HTTP_GetUserInfo, new GetUserInfoRunner());
		eventManager.registerEventRunner(JHEventCode.HTTP_ChangeUserInfo, new ChangeUserInfoRunner());
		eventManager.registerEventRunner(JHEventCode.HTTP_PostFile, new PostFileRunner());
	}
}
