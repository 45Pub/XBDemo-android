package com.xbcx.jianhua.activity;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.UrlBitmapDownloadCallback;
import com.xbcx.core.XApplication;
import com.xbcx.im.IMKernel;
import com.xbcx.im.ui.XBaseActivity;
import com.xbcx.jianhua.JHEventCode;
import com.xbcx.jianhua.R;
import com.xbcx.jianhua.UserInfo;
import com.xbcx.jianhua.im.JHVCardProvider;

public class DetailUserActivity extends XBaseActivity implements 
													View.OnClickListener,
													DialogInterface.OnClickListener,
													UrlBitmapDownloadCallback{
	
	private TextView	mTextViewDepartment;
	private TextView	mTextViewMobilePhone;
	private TextView	mTextViewFixPhone;
	private TextView	mTextViewEmail;
	
	private ImageView	mImageViewAvatar;
	private TextView	mTextViewName;
	private	Button		mButton;
	private Button		mButtonDeleteFriend;
	
	private String		mId;
	private boolean		mIsConfirm;
	private boolean		mIsFriend;
	
	private int			mDialogIdVerify;
	private EditText	mEditTextVerifyMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mId = getIntent().getStringExtra("id");
		mIsConfirm = getIntent().getBooleanExtra("confirm", false);
		
		mTextViewDepartment = (TextView)findViewById(R.id.tvIntro);
		mTextViewMobilePhone = (TextView)findViewById(R.id.tvCPhone);
		mTextViewFixPhone = (TextView)findViewById(R.id.tvTelephone);
		mTextViewEmail = (TextView)findViewById(R.id.tvEmail);
		
		mImageViewAvatar = (ImageView)findViewById(R.id.ivAvatar);
		mTextViewName = (TextView)findViewById(R.id.tvName);
		mButton = (Button)findViewById(R.id.btn);
		mButtonDeleteFriend = (Button)findViewById(R.id.btnDeleteFriend);
		mButton.setOnClickListener(this);
		mButtonDeleteFriend.setOnClickListener(this);
		
		//setAvatar(mImageViewAvatar, mId);
		mTextViewName.setText(getIntent().getStringExtra("name"));
		
		onFriendChanged(IMKernel.getInstance().isFriend(mId));
		
		if(IMKernel.isLocalUser(mId)){
			mButton.setVisibility(View.GONE);
			mButtonDeleteFriend.setVisibility(View.GONE);
		}
		
		pushEvent(JHEventCode.HTTP_GetUserInfo, mId);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
		ba.mTitleTextStringId = R.string.detail_data;
	}

	public static void launch(Activity activity,String userId,String name,boolean bConfirm){
		Intent intent = new Intent(activity, DetailUserActivity.class);
		intent.putExtra("id", userId);
		intent.putExtra("name", name);
		intent.putExtra("confirm", bConfirm);
		activity.startActivity(intent);
	}
	
	protected void onFriendChanged(boolean bFriend){
		mIsFriend = bFriend;
		if(mIsFriend){
			mButton.setText(R.string.send_message);
			mButtonDeleteFriend.setVisibility(View.VISIBLE);
		}else{
			if(mIsConfirm){
				mButton.setText(R.string.detail_confirm);
			}else{
				mButton.setText(R.string.detail_add_friend);
			}
			mButtonDeleteFriend.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if(id == R.id.btn){
			if(mIsFriend){
				JHSingleChatActivity.launch(this, mId, getName());
			}else{
				if(mIsConfirm){
					pushEventBlock(EventCode.IM_AddFriendConfirm, mId);
				}else{
					pushEventBlock(EventCode.IM_AddFriendApply, mId,getName());
				}
			}
		}else if(id == R.id.btnDeleteFriend){
			pushEventBlock(EventCode.IM_DeleteFriend, mId);
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == EventCode.IM_AddFriendApply){
			if(event.isSuccess()){
				onFriendChanged(true);
			}else{
				Exception e = event.getFailException();
				if(e != null && e instanceof XMPPException){
					final XMPPError error = ((XMPPException)e).getXMPPError();
					if(error != null){
						if(error.getCode() == 405){
							mToastManager.show(R.string.toast_cannot_add_friend);
						}else if(error.getCode() == 407){
							if(mDialogIdVerify == 0){
								mDialogIdVerify = generateDialogId();
							}
							showDialog(mDialogIdVerify);
						}
					}else{
						mToastManager.show(R.string.toast_disconnect);
					}
				}else{
					mToastManager.show(R.string.toast_disconnect);
				}
			}
		}else if(code == EventCode.IM_AddFriendVerify){
			if(!event.isSuccess()){
				mToastManager.show(R.string.toast_send_verify_fail);
			}
		}else if(code == EventCode.IM_AddFriendConfirm){
			if(event.isSuccess()){
				mIsConfirm = false;
				onFriendChanged(true);
			}else{
				mToastManager.show(R.string.toast_pass_verify_fail);
			}
		}else if(code == EventCode.IM_DeleteFriend){
			if(event.isSuccess()){
				onFriendChanged(false);
			}else{
				mToastManager.show(R.string.toast_delete_friend_fail);
			}
		}else if(code == JHEventCode.HTTP_GetUserInfo){
			if(event.isSuccess()){
				final UserInfo bi = (UserInfo)event.getReturnParamAtIndex(0);
				mTextViewDepartment.setText(bi.getDepartment());
				mTextViewMobilePhone.setText(bi.getMobilePhone());
				mTextViewFixPhone.setText(bi.getFixPhone());
				mTextViewEmail.setText(bi.getEmail());
				XApplication.setBitmap(mImageViewAvatar, 
						bi.getAvatarUrl(), R.drawable.avatar_user, this);
				JHVCardProvider.getInstance().saveInfo(bi.getId(),
						bi.getName(),bi.getAvatarUrl());
			}
		}
	}

	protected String getName(){
		return mTextViewName.getText().toString();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if(id == mDialogIdVerify){
			EditText editText = new EditText(this);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.add_friend_verify_dialog_title)
			.setMessage(R.string.add_friend_verify_dialog_message)
			.setPositiveButton(R.string.cancel, this)
			.setNegativeButton(R.string.send, this)
			.setView(editText);
			mEditTextVerifyMessage = editText;
			return builder.create();
		}
		return super.onCreateDialog(id);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if(which == DialogInterface.BUTTON_NEGATIVE){
			EditText editText = mEditTextVerifyMessage;
			final String strText = editText.getEditableText().toString();
			pushEventBlock(EventCode.IM_AddFriendVerify, mId,strText);
				
			editText.setText("");
		}
	}

	@Override
	public void onBitmapDownloadSuccess(String url) {
		XApplication.setBitmap(mImageViewAvatar, url, R.drawable.avatar_user, null);
	}
}
