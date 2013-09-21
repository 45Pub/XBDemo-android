package com.xbcx.im.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.FilePaths;
import com.xbcx.core.NameObject;
import com.xbcx.core.ResIds;
import com.xbcx.core.ToastManager;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.im.IMKernel;
import com.xbcx.im.IMStatus;
import com.xbcx.im.VCardProvider;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

public class XBaseActivity extends BaseActivity implements OnEventListener{
	
	public static String sLoginActivityClassName;
	
	private   static ActivityManager 	sActivityManager;
	private   static boolean 			sBackgrounded;
	protected static long				sBackgroundTime;
	
	protected boolean mCheckProcessInfo 				= true;
	protected boolean mNotifyConnection 				= false;
	protected boolean mDestroyWhenLoginActivityLaunch 	= true;
	protected boolean mTitleShowConnectState			= false;
	
	protected ToastManager 			mToastManager;
	
	protected AndroidEventManager	mEventManager = AndroidEventManager.getInstance();
	
	private SparseArray<OnEventListener> 		mMapCodeToListener;
	private HashMap<Event, Event> 				mMapPushEvents;
	private SparseIntArray						mMapDismissProgressDialogEventCode;
	private HashMap<Event, Boolean>				mMapEventToProgressBlock;
	private SparseArray<List<Runnable>> 		mMapCodeToEventEndRunnable;
	private SparseArray<List<TriggerEvent>> 	mMapListenCodeToTriggerEvent;
	
	private HashMap<String, List<ImageView>> 	mMapUserIdToAvatarView;
	private HashMap<String, List<TextView>> 	mMapUserIdToTextView;
	
	private View		mViewPromptConnection;
	private View		mViewConnecting;
	private View		mViewNormal;
	private ImageView	mImageViewPromptConnection;
	
	private int			mDialogIdConflict;
	private int			mDialogIdPwdError;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mToastManager = ToastManager.getInstance(getApplicationContext());
		
		if(sActivityManager == null){
			sActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		}
		
		onSetParam();
		
		if(mNotifyConnection){
			addAndManageEventListener(EventCode.IM_ConnectionInterrupt);
			addAndManageEventListener(EventCode.IM_Login);
			addAndManageEventListener(EventCode.IM_LoginStart);
			if(!IMKernel.isIMConnectionAvailable()){
				addConnectionPromptView();
				mEventManager.pushEvent(EventCode.IM_Login);
			}
		}
		
		if(mTitleShowConnectState){
			addAndManageEventListener(EventCode.IM_Login);
			addAndManageEventListener(EventCode.IM_LoginStart);
			IMStatus status = IMKernel.getIMStatus();
			if(status.mIsConflict){
				if(mDialogIdConflict == 0){
					mDialogIdConflict = generateDialogId();
				}
				showDialog(mDialogIdConflict);
			}else{
				if(!status.mIsLoginSuccess){
					if(mTextViewTitle != null){
						mTextViewTitle.setText(ResIds.string.connecting);
						if(!status.mIsLogining){
							mEventManager.pushEvent(EventCode.IM_Login);
						}
					}
				}
			}
		}
		
		addAndManageEventListener(EventCode.IM_Conflict);
		addAndManageEventListener(EventCode.IM_LoginPwdError);
		addAndManageEventListener(EventCode.LoginActivityLaunched);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(mViewPromptConnection != null){
			removeConnectionPromptView();
		}
		
		if(mMapCodeToListener != null){
			final int nCount = mMapCodeToListener.size();
			for(int nIndex = 0;nIndex < nCount;++nIndex){
				int nCode = mMapCodeToListener.keyAt(nIndex);
				mEventManager.removeEventListener(nCode, mMapCodeToListener.get(nCode));
			}
			mMapCodeToListener.clear();
		}
		
		if(mMapPushEvents != null){
			for(Event e : mMapPushEvents.keySet()){
				mEventManager.removeEventListener(e.getEventCode(), this);
				mEventManager.removeEventListenerEx(e, this);
			}
			mMapPushEvents.clear();
		}
		
		if(mMapCodeToEventEndRunnable != null){
			mMapCodeToEventEndRunnable.clear();
		}
	}
	
	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mTitleLayoutId = R.id.viewTitle;
		ba.mTitleTextLayoutId = R.layout.textview_title;
	}

	protected void onSetParam(){
	}
	
	@Override
	protected String getCameraSaveFilePath() {
		return FilePaths.getCameraSaveFilePath();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if(sBackgrounded){
			sBackgrounded = false;
			mEventManager.runEvent(EventCode.AppForceground);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(mCheckProcessInfo){
			List<RunningAppProcessInfo> listAppProcessInfo = sActivityManager.getRunningAppProcesses();
			if(listAppProcessInfo != null){
				final String strPackageName = getPackageName();
				for(RunningAppProcessInfo pi : listAppProcessInfo){
					if(pi.processName.equals(strPackageName)){
						if(pi.importance == RunningAppProcessInfo.IMPORTANCE_SERVICE ||
								pi.importance == 130 ||
								pi.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND ||
								pi.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE){
							sBackgrounded = true;
							sBackgroundTime = System.currentTimeMillis();
							mEventManager.runEvent(EventCode.AppBackground);
						}
						break;
					}
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	protected void startUnConnectionAnimation(){
		if(mViewPromptConnection != null && mViewNormal.getVisibility() == View.VISIBLE){
			mImageViewPromptConnection = (ImageView)mViewPromptConnection.findViewById(ResIds.id.iv);
			mImageViewPromptConnection.setBackgroundDrawable(null);
			mImageViewPromptConnection.setBackgroundResource(ResIds.drawable.animlist_prompt_connection);
			AnimationDrawable d = (AnimationDrawable)mImageViewPromptConnection.getBackground();
			d.start();
		}
	}
	
	private void addConnectionPromptView(){
		WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		mViewPromptConnection = LayoutInflater.from(this).inflate(ResIds.layout.prompt_connection, null);
		mViewConnecting = mViewPromptConnection.findViewById(ResIds.id.viewConnecting);
		mViewNormal = mViewPromptConnection.findViewById(ResIds.id.viewNormal);
		mViewConnecting.setVisibility(View.VISIBLE);
		mViewNormal.setVisibility(View.GONE);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.WRAP_CONTENT, 
				WindowManager.LayoutParams.TYPE_APPLICATION,
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.RGBA_8888);
		lp.y = SystemUtils.dipToPixel(this,50);
		lp.gravity = Gravity.TOP;
		
		windowManager.addView(mViewPromptConnection, lp);
	}
	
	private void removeConnectionPromptView(){
		WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		windowManager.removeView(mViewPromptConnection);
		mViewPromptConnection = null;
	}
	
	protected boolean nameFilter(NameObject no,String key){
		if(no == null){
			return false;
		}
		return no.getName().contains(key);
	}
	
	protected void setAvatar(ImageView iv,String userId){
		iv.setImageBitmap(VCardProvider.getInstance().loadAvatar(userId));
		if(mMapUserIdToAvatarView == null){
			mMapUserIdToAvatarView = new HashMap<String, List<ImageView>>();
		}
		List<ImageView> ivs = mMapUserIdToAvatarView.get(userId);
		if(ivs == null){
			ivs = new LinkedList<ImageView>();
			mMapUserIdToAvatarView.put(userId, ivs);
		}
		ivs.add(iv);
		addAndManageEventListener(EventCode.DownloadAvatar);
	}
	
	protected void setName(TextView tv,String userId,String defaultName){
		if(defaultName == null){
			tv.setText(VCardProvider.getInstance().loadUserName(userId));
		}else{
			tv.setText(defaultName);
		}
		if(mMapUserIdToTextView == null){
			mMapUserIdToTextView = new HashMap<String, List<TextView>>();
		}
		List<TextView> tvs = mMapUserIdToTextView.get(userId);
		if(tvs == null){
			tvs = new LinkedList<TextView>();
			mMapUserIdToTextView.put(userId, tvs);
		}
		tvs.add(tv);
		addAndManageEventListener(EventCode.IM_LoadVCard);
	}
	
	protected Event pushEvent(int eventCode,Object...params){
		return pushEventEx(eventCode, true, false,null,params);
	}
	
	protected Event pushEventBlock(int eventCode,Object...params){
		return pushEventEx(eventCode, true, true, null, params);
	}
	
	protected Event pushEventNoProgress(int eventCode,Object...params){
		return pushEventEx(eventCode, false, false, null, params);
	}
	
	@SuppressLint("UseSparseArrays")
	protected Event pushEventEx(int eventCode,
			boolean bShowProgress,boolean bBlock,String progressMsg,
			Object... params){
		Event e = null;
		if(mMapCodeToListener.get(eventCode) != null){
			e = mEventManager.pushEvent(eventCode, params);
		}else{
			e = mEventManager.pushEventEx(eventCode,this,params);
			if(mMapPushEvents == null){
				mMapPushEvents = new HashMap<Event, Event>();
			}
			mMapPushEvents.put(e, e);
		}
		
		if(mMapEventToProgressBlock == null){
			mMapEventToProgressBlock = new HashMap<Event, Boolean>();
		}
		
		if( !mMapEventToProgressBlock.containsKey(e)){
			if(bShowProgress){
				if(bBlock){
					showProgressDialog(null,progressMsg);
				}else{
					showXProgressDialog();
				}
				mMapEventToProgressBlock.put(e, bBlock);
			}
		}
		
		return e;
	}
	
	protected void addAndManageEventListener(int eventCode){
		addAndManageEventListener(eventCode, false);
	}

	protected void addAndManageEventListener(int eventCode,boolean bDismissProgressDialog){
		if(mMapCodeToListener == null){
			mMapCodeToListener = new SparseArray<OnEventListener>();
		}
		if(mMapCodeToListener.get(eventCode) == null){
			mMapCodeToListener.put(eventCode, this);
			
			mEventManager.addEventListener(eventCode, this, false);
		}
		
		if(bDismissProgressDialog){
			if(mMapDismissProgressDialogEventCode == null){
				mMapDismissProgressDialogEventCode = new SparseIntArray();
			}
			mMapDismissProgressDialogEventCode.put(eventCode, eventCode);
		}
	}
	
	protected void removeEventListener(int eventCode){
		if(mMapCodeToListener == null){
			return;
		}
		mMapCodeToListener.remove(eventCode);
		
		mEventManager.removeEventListener(eventCode, this);
	}
	
	protected void bindEventListenerRunnable(int eventCode,Runnable run){
		if(mMapCodeToEventEndRunnable == null){
			mMapCodeToEventEndRunnable = new SparseArray<List<Runnable>>();
		}
		List<Runnable> listRunnable = mMapCodeToEventEndRunnable.get(eventCode);
		if(listRunnable == null){
			listRunnable = new LinkedList<Runnable>();
			mMapCodeToEventEndRunnable.put(eventCode, listRunnable);
		}
		listRunnable.add(run);
	}
	
	protected void unbindEventListenerRunnable(int eventCode,Runnable run) {
		if(mMapCodeToEventEndRunnable == null){
			return;
		}
		List<Runnable> listRunnable = mMapCodeToEventEndRunnable.get(eventCode);
		if(listRunnable != null){
			listRunnable.remove(run);
		}
	}
	
	protected void	bindTriggerEventCode(int listenCode,int triggerCode){
		bindTriggerEvent(listenCode, triggerCode, false);
	}
	
	protected void	bindTriggerEvent(int listenCode,int triggerCode,
			boolean bShowProgress,Object...params){
		if(mMapListenCodeToTriggerEvent == null){
			mMapListenCodeToTriggerEvent = new SparseArray<List<TriggerEvent>>();
		}
		List<TriggerEvent> tes = mMapListenCodeToTriggerEvent.get(listenCode);
		if(tes == null){
			tes = new ArrayList<TriggerEvent>();
			mMapListenCodeToTriggerEvent.put(listenCode, tes);
		}
		final TriggerEvent te = new TriggerEvent(triggerCode, params, bShowProgress);
		tes.add(te);
		addAndManageEventListener(listenCode);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onEventRunEnd(Event event) {
		final int code = event.getEventCode();
		
		if(mMapListenCodeToTriggerEvent != null){
			if(event.isSuccess()){
				final List<TriggerEvent> tes = mMapListenCodeToTriggerEvent.get(code);
				if(tes != null){
					for(TriggerEvent te : tes){
						if(te.mIsShowProgress){
							pushEvent(te.mEventCode, te.mParams);
						}else{
							mEventManager.pushEvent(te.mEventCode, te.mParams);
						}
					}
				}
			}
		}
		
		if(mMapDismissProgressDialogEventCode != null && 
				mMapDismissProgressDialogEventCode.get(code, -1) != -1){
			dismissProgressDialog();
			dismissXProgressDialog();
		}
		
		if(mMapPushEvents != null){
			mMapPushEvents.remove(event);
		}
		
		if(mMapEventToProgressBlock != null){
			Boolean block = mMapEventToProgressBlock.remove(event);
			if(block != null){
				if(block.booleanValue()){
					dismissProgressDialog();
				}else{
					dismissXProgressDialog();
				}
			}
		}
		
		if(code == EventCode.IM_Conflict){
			if(mDialogIdConflict == 0){
				mDialogIdConflict = generateDialogId();
			}
			showDialog(mDialogIdConflict);
		}else if(code == EventCode.LoginActivityLaunched){
			if(mDestroyWhenLoginActivityLaunch){
				finish();
			}
		}else if(code == EventCode.DownloadAvatar){
			if(event.isSuccess()){
				final String userId = (String)event.getParamAtIndex(0);
				if(mMapUserIdToAvatarView != null){
					List<ImageView> ivs = mMapUserIdToAvatarView.get(userId);
					if(ivs != null){
						for(ImageView iv : ivs){
							iv.setImageBitmap(
									VCardProvider.getInstance().loadAvatar(userId));
						}
					}
				}
			}
		}else if(code == EventCode.IM_LoadVCard){
			if(event.isSuccess()){
				final String userId = (String)event.getParamAtIndex(0);
				if(mMapUserIdToTextView != null){
					List<TextView> tvs = mMapUserIdToTextView.get(userId);
					if(tvs != null){
						for(TextView tv : tvs){
							tv.setText(VCardProvider.getInstance().loadUserName(userId));
						}
					}
				}
			}
		}else if(code == EventCode.IM_LoginPwdError){
			if(mDialogIdPwdError == 0){
				mDialogIdPwdError = generateDialogId();
			}
			showDialog(mDialogIdPwdError);
		}
		
		if(mNotifyConnection){
			if(code == EventCode.IM_Login){
				if(IMKernel.isIMConnectionAvailable()){
					if(mViewPromptConnection != null){
						removeConnectionPromptView();
					}
				}else{
					if(mViewPromptConnection != null){
						mViewConnecting.setVisibility(View.GONE);
						mViewNormal.setVisibility(View.VISIBLE);
					}
				}
			}else if(code == EventCode.IM_ConnectionInterrupt){
				if(mViewPromptConnection == null){
					addConnectionPromptView();
				}else{
					mViewConnecting.setVisibility(View.GONE);
					mViewNormal.setVisibility(View.VISIBLE);
				}
			}else if(code == EventCode.IM_LoginStart){
				if(mViewPromptConnection == null){
					addConnectionPromptView();
				}
				mViewConnecting.setVisibility(View.VISIBLE);
				mViewNormal.setVisibility(View.GONE);
			}
		}
		
		if(mTitleShowConnectState){
			if(mTextViewTitle != null){
				if(code == EventCode.IM_LoginStart){
					mTextViewTitle.setText(ResIds.string.connecting);
				}else if(code == EventCode.IM_Login){
					if(event.isSuccess()){
						if(TextUtils.isEmpty(mBaseAttribute.mTitleText)){
							mTextViewTitle.setText(mBaseAttribute.mTitleTextStringId);
						}else{
							mTextViewTitle.setText(mBaseAttribute.mTitleText);
						}
					}else{
						mTextViewTitle.setText(R.string.disconnect);
					}
				}else if(code == EventCode.IM_ConnectionInterrupt){
					mTextViewTitle.setText(R.string.disconnect);
				}
			}
		}
		
		if(mMapCodeToEventEndRunnable != null){
			List<Runnable> listRunnable = mMapCodeToEventEndRunnable.get(code);
			if(listRunnable != null){
				for(Runnable run : listRunnable){
					run.run();
				}
			}
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if(id == mDialogIdConflict){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(ResIds.string.dialogmessage_logout).setCancelable(false)
			.setPositiveButton(ResIds.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					onConflictSured();
				}
			});
			return builder.create();
		}else if(id == mDialogIdPwdError){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.dialogmessage_pwd_error).setCancelable(false)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					onLoginPwdErrorSured();
				}
			});
			return builder.create();
		}
		return super.onCreateDialog(id);
	}
	
	protected void onConflictSured(){
		if(sLoginActivityClassName != null){
			try{
				Intent intent = new Intent(this, Class.forName(sLoginActivityClassName));
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		mEventManager.runEvent(EventCode.LoginActivityLaunched);
	}
	
	protected void onLoginPwdErrorSured(){
		if(sLoginActivityClassName != null){
			try{
				Intent intent = new Intent(this, Class.forName(sLoginActivityClassName));
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		mEventManager.runEvent(EventCode.LoginActivityLaunched);
	}
	
	protected static interface EventRunnable{
		public void onEventRunEnd(Event event);
	}
	
	private static class TriggerEvent{
		public final int 		mEventCode;
		public final Object 	mParams[];
		public final boolean 	mIsShowProgress;
		
		public TriggerEvent(int code,Object params[],boolean bShowProgress){
			mEventCode = code;
			mParams = params;
			mIsShowProgress = bShowProgress;
		}
	}
}
