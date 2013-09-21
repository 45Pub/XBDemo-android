package com.xbcx.im.ui;

import com.xbcx.core.EventCode;
import com.xbcx.core.XApplication;
import com.xbcx.im.XMessage;
import com.xbcx.im.folder.FileItem;
import com.xbcx.library.R;
import com.xbcx.utils.HttpUtils;
import com.xbcx.utils.HttpUtils.ProgressRunnable;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ViewPictureActivity extends XBaseActivity implements View.OnClickListener{

	private static final String EXTRA_DOWNLOADURL 	= "downloadurl";
	private static final String EXTRA_FILESAVEPATH 	= "filesavepath";
	
	private ImageView 	mImageView;
	private ImageView 	mImageViewDefault;
	private ProgressBar	mProgressBar;
	
	private String		mDownloadUrl;
	private String		mFileSavePath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mImageView = (ImageView)findViewById(R.id.ivPhoto);
		mImageViewDefault = (ImageView)findViewById(R.id.ivDefault);
		mProgressBar = (ProgressBar)findViewById(R.id.pb);
		mProgressBar.setMax(100);
		
		mDownloadUrl = getIntent().getStringExtra(EXTRA_DOWNLOADURL);
		mFileSavePath = getIntent().getStringExtra(EXTRA_FILESAVEPATH);
		
		Bitmap bmp = null;
		try{
			bmp = BitmapFactory.decodeFile(mFileSavePath);
		}catch(OutOfMemoryError e){
			e.printStackTrace();
			BitmapFactory.Options op = new BitmapFactory.Options();
			op.inSampleSize = 2;
			try{
				bmp = BitmapFactory.decodeFile(mFileSavePath, op);
			}catch(OutOfMemoryError e1){
				e1.printStackTrace();
				finish();
				return;
			}
		}
		if(bmp == null){
			requestDownload();
		}else{
			mImageViewDefault.setVisibility(View.GONE);
			mImageView.setImageBitmap(bmp);
			mProgressBar.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	public static void launch(Activity activity,
			XMessage xm){
		Intent intent = new Intent(activity, ViewPictureActivity.class);
		intent.putExtra(EXTRA_DOWNLOADURL, xm.getPhotoDownloadUrl());
		intent.putExtra(EXTRA_FILESAVEPATH, xm.getPhotoFilePath());
		intent.putExtra("fromself", xm.isFromSelf());
		intent.putExtra("name", xm.getDisplayName());
		intent.putExtra("time", xm.getSendTime());
		activity.startActivity(intent);
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mHasTitle = false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	private void requestDownload(){
		mImageViewDefault.setImageResource(R.drawable.chat_img);
		mProgressBar.setVisibility(View.VISIBLE);
		
		new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				ProgressRunnable runnable = new ProgressRunnable() {
					@Override
					public void run() {
						mProgressBar.setProgress(getPercentage());
					}
				};
				return HttpUtils.doDownload(mDownloadUrl, mFileSavePath,
							runnable, 
							XApplication.getMainThreadHandler(),
							null);
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if(result.booleanValue()){
					mEventManager.pushEvent(EventCode.DB_SaveToFolder, 
							new FileItem(mFileSavePath, 
									getIntent().getStringExtra("name"), 
									FileItem.FILETYPE_PIC,
									getIntent().getBooleanExtra("fromself", false), 
									getIntent().getLongExtra("time", System.currentTimeMillis())));
					Bitmap bmp;
					try{
						bmp = BitmapFactory.decodeFile(mFileSavePath);
					}catch(OutOfMemoryError e){
						e.printStackTrace();
						BitmapFactory.Options op = new BitmapFactory.Options();
						op.inSampleSize = 2;
						try{
							bmp = BitmapFactory.decodeFile(mFileSavePath, op);
						}catch(OutOfMemoryError e1){
							e1.printStackTrace();
							finish();
							return;
						}
					}
					if(bmp == null){
						mImageViewDefault.setImageResource(R.drawable.chat_img_wrong);
						mProgressBar.setVisibility(View.GONE);
						mImageViewDefault.setOnClickListener(ViewPictureActivity.this);
					}else{
						mImageViewDefault.setVisibility(View.GONE);
						mImageView.setImageBitmap(bmp);
						mProgressBar.setVisibility(View.GONE);
					}
				}else{
					mImageViewDefault.setImageResource(R.drawable.chat_img_wrong);
					mProgressBar.setVisibility(View.GONE);
					mImageViewDefault.setOnClickListener(ViewPictureActivity.this);
				}
			}
			
		}.execute();
	}

	@Override
	public void onClick(View v) {
		final int nId = v.getId();
		if(nId == R.id.ivDefault){
			mImageViewDefault.setOnClickListener(null);
			requestDownload();
		}
	}

}
