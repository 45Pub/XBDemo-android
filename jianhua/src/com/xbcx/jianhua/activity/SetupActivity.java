package com.xbcx.jianhua.activity;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.FilePaths;
import com.xbcx.core.UrlBitmapDownloadCallback;
import com.xbcx.core.XApplication;
import com.xbcx.im.RecentChatManager;
import com.xbcx.im.ui.XBaseActivity;
import com.xbcx.jianhua.BaseInfo;
import com.xbcx.jianhua.JHApplication;
import com.xbcx.jianhua.JHEventCode;
import com.xbcx.jianhua.R;
import com.xbcx.jianhua.im.JHVCardProvider;
import com.xbcx.utils.FileHelper;

public class SetupActivity extends XBaseActivity implements 
													View.OnClickListener,
													DialogInterface.OnClickListener,
													UrlBitmapDownloadCallback{
	
	private static final int MENUID_CAMERA = 1;
	private static final int MENUID_ALBUMS = 2;
	
	protected ImageView	mImageViewAvatar;
	protected TextView	mTextViewName;
	protected TextView	mTextViewSign;
	protected TextView	mTextViewCPhone;
	protected TextView	mTextViewTel;
	protected TextView	mTextViewEmail;
	
	private int			mDialogIdDeleteMsg;
	private int			mDialogIdLogout;
	
	private int			mRequestCodeRegion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mImageViewAvatar = (ImageView)findViewById(R.id.ivAvatar);
		mTextViewName = (TextView)findViewById(R.id.tvName);
		mTextViewSign = (TextView)findViewById(R.id.tvSign);
		mTextViewCPhone = (TextView)findViewById(R.id.tvCPhone);
		mTextViewTel = (TextView)findViewById(R.id.tvTel);
		mTextViewEmail = (TextView)findViewById(R.id.tvEmail);
		
		BaseInfo bi = JHApplication.getBaseInfo();
		if(bi.getRole() == BaseInfo.ROLE_INNER){
			findViewById(R.id.arrowSign).setVisibility(View.INVISIBLE);
		}else{
			findViewById(R.id.viewDepartment).setOnClickListener(this);
		}
		//setAvatar(mImageViewAvatar, IMKernel.getLocalUser());
		
		findViewById(R.id.viewHead).setOnClickListener(this);
		findViewById(R.id.viewName).setOnClickListener(this);
		findViewById(R.id.viewCPhone).setOnClickListener(this);
		findViewById(R.id.viewTel).setOnClickListener(this);
		findViewById(R.id.viewEmail).setOnClickListener(this);
		
		registerForContextMenu(findViewById(R.id.viewHead));
		
		findViewById(R.id.viewClearMsg).setOnClickListener(this);
		findViewById(R.id.btnExit).setOnClickListener(this);
		findViewById(R.id.viewPrivacy).setOnClickListener(this);
		
		updateUI(bi);
		
		addAndManageEventListener(JHEventCode.HTTP_ChangeUserInfo);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mTitleTextStringId = R.string.setup;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if(id == R.id.viewClearMsg){
			if(mDialogIdDeleteMsg == 0){
				mDialogIdDeleteMsg = generateDialogId();
			}
			showDialog(mDialogIdDeleteMsg);
		}else if(id == R.id.btnExit){
			if(mDialogIdLogout == 0){
				mDialogIdLogout = generateDialogId();
			}
			showDialog(mDialogIdLogout);
		}else if(id == R.id.viewPrivacy){
			PrivacyActivity.launch(this);
		}else if(id == R.id.viewHead){
			openContextMenu(findViewById(R.id.viewHead));
		}else if(id == R.id.viewName){
			NameActivity.launch(this,mTextViewName.getText().toString());
		}else if(id == R.id.viewDepartment){
			if(mRequestCodeRegion == 0){
				mRequestCodeRegion = generateRequestCode();
			}
			ChooseRegionActivity.launchForResult(this, mRequestCodeRegion);
		}else if(id == R.id.viewCPhone){
			CPhoneActivity.launch(this,mTextViewCPhone.getText().toString());
		}else if(id == R.id.viewTel){
			TelActivity.launch(this,mTextViewTel.getText().toString());
		}else if(id == R.id.viewEmail){
			EmailActivity.launch(this,mTextViewEmail.getText().toString());
		}
	}

	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == JHEventCode.HTTP_ChangeUserInfo){
			if(event.isSuccess()){
				final BaseInfo bi = (BaseInfo)event.getParamAtIndex(0);
				JHApplication.saveBaseInfo(bi);
				updateUI(bi);
				JHVCardProvider.getInstance().saveInfo(bi.getIMUser(), 
						bi.getName(), bi.getAvatarUrl());
			}
		}else if(code == JHEventCode.HTTP_PostFile){
			if(event.isSuccess()){
				final String path = (String)event.getParamAtIndex(1);
				final String url = (String)event.getReturnParamAtIndex(0);
				BaseInfo bi = JHApplication.getBaseInfo();
				bi.setAvatarUrl(url);
				FileHelper.copyFile(FilePaths.getUrlFileCachePath(url), path);
				pushEvent(JHEventCode.HTTP_ChangeUserInfo, bi);
			}
		}
	}
	
	protected void updateUI(BaseInfo bi){
		if(bi != null){
			mTextViewName.setText(bi.getName());
			mTextViewCPhone.setText(bi.getMobilePhone());
			mTextViewTel.setText(bi.getFixPhone());
			mTextViewEmail.setText(bi.getEmail());
			mTextViewSign.setText(bi.getDepartment());
			XApplication.setBitmap(mImageViewAvatar, bi.getAvatarUrl(), R.drawable.avatar_user, this);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(R.string.avatar)
		.add(0, MENUID_CAMERA, 0, R.string.photograph);
		menu.add(0, MENUID_ALBUMS, 0, R.string.setup_albumns);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int id = item.getItemId();
		if(id == MENUID_CAMERA){
			launchCamera(false);
		}else if(id == MENUID_ALBUMS){
			launchPictureChoose();
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	protected void onSetCropExtra(Intent intent) {
		super.onSetCropExtra(intent);
		intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1); 
        intent.putExtra("outputX", 100);
        intent.putExtra("outputY", 100); 
        intent.putExtra("return-data", false);
        intent.putExtra("noFaceDetection", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, 
				Uri.fromFile(new File(FilePaths.getPictureChooseFilePath())));
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
	}

	protected void onPictureChooseResult(Intent data){
		pushEvent(JHEventCode.HTTP_PostFile, "6",FilePaths.getPictureChooseFilePath());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(requestCode == mRequestCodeRegion){
				final String region = data.getStringExtra(ChooseRegionActivity.EXTRA_RETURN_REGION);
				BaseInfo bi = JHApplication.getBaseInfo();
				bi.setDepartment(region);
				pushEvent(JHEventCode.HTTP_ChangeUserInfo, bi);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		if(id == mDialogIdDeleteMsg){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.setup_clear)
			.setMessage(R.string.setup_prompt_delete)
			.setPositiveButton(R.string.ok, this)
			.setNegativeButton(R.string.cancel, this);
			return builder.create();
		}else if(id == mDialogIdLogout){
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which == DialogInterface.BUTTON_POSITIVE){
						JHApplication.logout();
						LoginActivity.launch(SetupActivity.this);
						finish();
					}
				}
			};
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.setup_exit)
			.setPositiveButton(R.string.sure_logout, listener)
			.setNegativeButton(R.string.cancel, listener);
			return builder.create();
		}
		return super.onCreateDialog(id, args);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if(which == DialogInterface.BUTTON_POSITIVE){
			showProgressDialog();
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					mEventManager.runEvent(EventCode.DB_DeleteMessage,this);
					RecentChatManager.getInstance().clearRecentChat();
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					dismissProgressDialog();
				}
			}.execute();
		}
	}

	@Override
	public void onBitmapDownloadSuccess(String url) {
		XApplication.setBitmap(mImageViewAvatar, url, R.drawable.avatar_user, null);
	}

}
