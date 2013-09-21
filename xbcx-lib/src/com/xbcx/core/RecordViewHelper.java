package com.xbcx.core;

import com.xbcx.im.IMKernel;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

public class RecordViewHelper implements View.OnTouchListener,
											MediaRecordManager.OnRecordListener{
	
	protected View 					mBtnPressTalk;
	protected PopupWindow 			mPopupWindowRecordPrompt;
	protected ImageView 			mImageViewRecordPromt;
	protected ProgressBar 			mProgressBarRecordPrepare;
	
	protected boolean 				mRecordSuccess;
	protected boolean 				mNeedStop;
	protected boolean 				mCancel;
	
	protected final int 			mLocation[] = new int[2];
	protected final Rect 			mRect = new Rect();
	
	protected MediaRecordManager 	mMediaRecordManager;
	protected boolean				mIsAddEventListener;
	
	protected OnRecordListener 		mOnRecordListener;
	
	protected Handler 				mHandler;
	
	private Runnable mRunnableDelayDismissRecordPrompt = new Runnable() {
		public void run() {
			mPopupWindowRecordPrompt.dismiss();
		}
	};
	
	public void onCreate(View btnPressTalk){
		final Context context = btnPressTalk.getContext();
		View contentView = LayoutInflater.from(context).inflate(R.layout.recordprompt, null);
		mImageViewRecordPromt = (ImageView)contentView.findViewById(R.id.imageView);
		mProgressBarRecordPrepare = (ProgressBar)contentView.findViewById(R.id.progressBar);
		
		mBtnPressTalk = btnPressTalk;
		mBtnPressTalk.setOnTouchListener(this);
		
		int nSize = SystemUtils.dipToPixel(context, 150);
		mPopupWindowRecordPrompt = new PopupWindow(contentView,nSize,nSize,false);
		
		mMediaRecordManager = MediaRecordManager.getInstance(context);
		mMediaRecordManager.open();
		
		mIsAddEventListener = false;
		
		mHandler = new Handler();
	}
	
	public void onDestroy(){
		mMediaRecordManager.close();
		mHandler.removeCallbacks(mRunnableDelayDismissRecordPrompt);
		mOnRecordListener = null;
	}
	
	public void onPause(){
		processStopRecord();
		mPopupWindowRecordPrompt.dismiss();
		if(mIsAddEventListener){
			mMediaRecordManager.removeOnRecordListener(this);
			mIsAddEventListener = false;
		}
	}
	
	public void onResume(){
		if(!mIsAddEventListener){
			mMediaRecordManager.addOnRecordListener(this);
		
			mIsAddEventListener = true;
		}
	}
	
	public void setOnRecordListener(OnRecordListener listener){
		mOnRecordListener = listener;
	}
	
	@Override
	public void onStarted(boolean bSuccess) {
		if(bSuccess){
			setRecordPromptDisplayChild(1);
			if(!mPopupWindowRecordPrompt.isShowing()){
				showPopupWindow();
			}
			
			updateRecordWave(0);
			
			if(mOnRecordListener != null){
				mOnRecordListener.onRecordStarted();
			}
		}else{
			onRecordBtnStatusChanged(false);
			
			mRecordSuccess = false;
			
			if(mOnRecordListener != null){
				mOnRecordListener.onRecordFailed(false);
			}
		}
	}
	
	protected void updateRecordWave(double decibel){
		if(decibel >= 30){
			mImageViewRecordPromt.setImageResource(R.drawable.image_talkstart_5);
		}else if(decibel >= 28){
			mImageViewRecordPromt.setImageResource(R.drawable.image_talkstart_4);
		}else if(decibel >= 26){
			mImageViewRecordPromt.setImageResource(R.drawable.image_talkstart_3);
		}else if(decibel >= 24){
			mImageViewRecordPromt.setImageResource(R.drawable.image_talkstart_2);
		}else{
			mImageViewRecordPromt.setImageResource(R.drawable.image_talkstart_1);
		}
	}

	@Override
	public void onStoped(boolean bBeyondMinTime) {
		mNeedStop = false;
		if (mRecordSuccess) {
			if (bBeyondMinTime) {
				mPopupWindowRecordPrompt.dismiss();

				if (mCancel) {
					mCancel = false;
				} else {
					if (mOnRecordListener != null) {
						mOnRecordListener.onRecordEnded(mMediaRecordManager.getRecordFilePath());
					}
				}
			}else{
				onTalkShort();
			}
		}
	}
	
	protected void onTalkShort(){
		mImageViewRecordPromt.setImageResource(R.drawable.image_talkshort);
		mHandler.postDelayed(mRunnableDelayDismissRecordPrompt, 500);
	}

	@Override
	public void onExceedMaxTime() {
		mImageViewRecordPromt.setImageResource(R.drawable.image_talklong);
		mHandler.postDelayed(mRunnableDelayDismissRecordPrompt, 500);
		mNeedStop = false;
	}

	@Override
	public void onInterrupted() {
		processStopRecord();
	}
	
	@Override
	public void onDecibelChanged(double decibel) {
		if(!mCancel){
			updateRecordWave(decibel);
		}
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		int nAction = event.getAction();
		if (nAction == MotionEvent.ACTION_MOVE){
			if (mRecordSuccess) {
				final int location[] = mLocation;
				v.getLocationOnScreen(location);
				final float fx = location[0] + event.getX();
				final float fy = location[1] + event.getY();

				mImageViewRecordPromt.getLocationOnScreen(location);
				final Rect rect = mRect;
				mImageViewRecordPromt.getGlobalVisibleRect(rect);
				rect.offsetTo(location[0], location[1]);
				if (rect.contains((int) fx, (int) fy)) {
					onChangeCancelStatus();
					mCancel = true;
				} else {
					if(mCancel){
						updateRecordWave(mMediaRecordManager.getCurrentDecibel());
						mCancel = false;
					}
				}
				return true;
			}
		}else if (nAction == MotionEvent.ACTION_DOWN) {
			if (!mPopupWindowRecordPrompt.isShowing()) {
				if (IMKernel.isIMConnectionAvailable()) {
					if (XApplication.checkExternalStorageAvailable()) {
						setRecordPromptDisplayChild(0);
						showPopupWindow();

						mMediaRecordManager.startRecord();
						mNeedStop = true;
						mRecordSuccess = true;
						
						onRecordBtnStatusChanged(true);
					}
				} else {
					mRecordSuccess = false;

					if (mOnRecordListener != null) {
						mOnRecordListener.onRecordFailed(true);
					}
				}
				return true;
			}
		} else if(nAction == MotionEvent.ACTION_UP || nAction == MotionEvent.ACTION_CANCEL){
			processStopRecord();
		}
		return false;
	}
	
	protected void onRecordBtnStatusChanged(boolean bPressDown){
		
	}
	
	protected void onChangeCancelStatus(){
		mImageViewRecordPromt.setImageResource(R.drawable.image_talkcancel);
	}
	
	protected void setRecordPromptDisplayChild(int nWhich){
		if(nWhich == 0){
			mProgressBarRecordPrepare.setVisibility(View.VISIBLE);
			mImageViewRecordPromt.setVisibility(View.GONE);
		}else{
			mProgressBarRecordPrepare.setVisibility(View.GONE);
			mImageViewRecordPromt.setVisibility(View.VISIBLE);
		}
	}
	
	protected void showPopupWindow(){
		mImageViewRecordPromt.setImageBitmap(null);
		mPopupWindowRecordPrompt.showAtLocation(mBtnPressTalk, Gravity.CENTER, 0, 0);
	}
	
	protected void processStopRecord(){
		if (mNeedStop) {
			setRecordPromptDisplayChild(1);
			if (mRecordSuccess) {
				mMediaRecordManager.stopRecord();
			} else {
				mPopupWindowRecordPrompt.dismiss();
			}
			mNeedStop = false;
			
			onRecordBtnStatusChanged(false);
		}
	}
	
	public static interface OnRecordListener{
		public void onRecordStarted();
		
		public void onRecordEnded(String strRecordPath);
		
		public void onRecordFailed(boolean bFailByNet);
	}
}
