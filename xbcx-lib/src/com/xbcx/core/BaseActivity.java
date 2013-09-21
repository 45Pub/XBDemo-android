package com.xbcx.core;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.xbcx.adapter.MenuItemAdapter;
import com.xbcx.library.R;
import com.xbcx.utils.FileHelper;
import com.xbcx.utils.OpenFileUtils;
import com.xbcx.utils.SystemUtils;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public abstract class BaseActivity extends FragmentActivity{
	
	protected static final int MENUID_PHOTO_CAMERA	= 1;
	protected static final int MENUID_PHOTO_FILE	= 2;
	
	protected BaseUIFactory 	mBaseUIFactory;

	protected BaseAttribute     mBaseAttribute;
	
	protected RelativeLayout 	mRelativeLayoutTitle;
	protected View 				mButtonBack;
	protected TextView 			mTextViewTitle;
	
	protected int				mRequestCodeLaunchCamera;
	protected int 				mRequestCodeLaunchChoosePicture;
	protected int				mRequestCodeLaunchChooseVideo;
	protected int				mRequestCodeChooseFile;
	protected boolean			mIsCameraVideo;
	protected boolean			mIsCameraCrop;
	
	protected ProgressDialog 	mProgressDialog;
	protected View				mViewXProgressDialog;
	protected boolean			mIsXProgressDialogShowing;
	protected int				mXProgressDialogShowCount;
	
	private	  int				mChoosePhotoDialogId;
	private   boolean			mIsChoosePhotoCrop;
	
	private Object				mTag;
	
	private	int					mRequestCodeInc = 0;
	private int					mDialogIdInc 	= 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		onInitAttribute(mBaseAttribute = new BaseAttribute());
		if(mBaseAttribute.mActivityLayoutId != 0){
			setContentView(mBaseAttribute.mActivityLayoutId);
		}else if(mBaseAttribute.mSetContentView){
			String strClassName = this.getClass().getName();
			int nIndex = strClassName.lastIndexOf(".");
			if(nIndex != -1){
				final String cName = strClassName.substring(nIndex + 1);
				String strResourceName = "activity_" + cName.replaceFirst("Activity", "");
				strResourceName = strResourceName.toLowerCase(Locale.getDefault());
				final int nLayoutId = getResources().getIdentifier(strResourceName, 
						"layout", getPackageName());
				if(nLayoutId != 0){
					setContentView(nLayoutId);
				}
			}
		}
		
		if(savedInstanceState != null){
			if(savedInstanceState.containsKey("launchcamera")){
				mRequestCodeLaunchCamera = savedInstanceState.getInt("launchcamera");
			}
			if(savedInstanceState.containsKey("launchchoosepicture")){
				mRequestCodeLaunchChoosePicture = savedInstanceState.getInt("launchchoosepicture");
			}
			if(savedInstanceState.containsKey("launchchoosevideo")){
				mRequestCodeLaunchChooseVideo = savedInstanceState.getInt("launchchoosevideo");
			}
			if(savedInstanceState.containsKey("launchchoosefile")){
				mRequestCodeChooseFile = savedInstanceState.getInt("launchchoosefile");
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		while(mIsXProgressDialogShowing){
			dismissXProgressDialog();
		}
		mTag = null;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(mIsXProgressDialogShowing){
			mViewXProgressDialog.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(mIsXProgressDialogShowing){
			mViewXProgressDialog.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(mRequestCodeLaunchCamera != 0){
			outState.putInt("launchcamera", mRequestCodeLaunchCamera);
		}
		if(mRequestCodeLaunchChoosePicture != 0){
			outState.putInt("launchchoosepicture", mRequestCodeLaunchChoosePicture);
		}
		if(mRequestCodeLaunchChooseVideo != 0){
			outState.putInt("launchchoosevideo", mRequestCodeLaunchChooseVideo);
		}
		if(mRequestCodeChooseFile != 0){
			outState.putInt("launchchoosefile", mRequestCodeChooseFile);
		}
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		if(mBaseAttribute.mHasTitle){
			if(mBaseUIFactory == null){
				Class<? extends BaseUIFactory> c = BaseUIProvider.getBaseUIFactoryClass();
				if(c == null){
					mBaseUIFactory = new SimpleBaseUIFactory(this);
				}else{
					try{
						Constructor<? extends BaseUIFactory> constructor = 
								c.getDeclaredConstructor(Context.class);
						mBaseUIFactory = (BaseUIFactory)constructor.newInstance(this);
					}catch(Exception e){
						e.printStackTrace();
						mBaseUIFactory = new SimpleBaseUIFactory(this);
					}
				}
			}
			
			mRelativeLayoutTitle = (RelativeLayout)findViewById(mBaseAttribute.mTitleLayoutId);
			if(mBaseAttribute.mAddBackButton){
				mButtonBack = onCreateBackButton();
				if(mButtonBack != null){
					mButtonBack.setOnClickListener(mOnClickListener);
					mRelativeLayoutTitle.addView(mButtonBack,onCreateBackButtonLayoutParams());
				}
			}
			
			if(mBaseAttribute.mAddTitleText){
				if(mBaseAttribute.mTitleText == null){
					addTextInTitle(mBaseAttribute.mTitleTextStringId);
				}else{
					addTextInTitle(mBaseAttribute.mTitleText);
				}
			}
		}
	}
	
	protected void choosePhoto(){
		choosePhoto(true);
	}
	
	@SuppressWarnings("deprecation")
	protected void choosePhoto(boolean bCrop){
		mIsChoosePhotoCrop = bCrop;
		if(mChoosePhotoDialogId == 0){
			mChoosePhotoDialogId = generateDialogId();
		}
		showDialog(mChoosePhotoDialogId);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		final Context context = getParent() == null ? this : getParent();
		if(id == mChoosePhotoDialogId){
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			MenuItemAdapter adapter = new MenuItemAdapter(context);
			adapter.addItem(new MenuItemAdapter.MenuItem(MENUID_PHOTO_CAMERA, R.string.photograph));
			adapter.addItem(new MenuItemAdapter.MenuItem(MENUID_PHOTO_FILE, R.string.choose_from_albums));
			builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which == 0){
						launchCamera(false,mIsChoosePhotoCrop);
					}else if(which == 1){
						launchPictureChoose(mIsChoosePhotoCrop);
					}
				}
			});
			return builder.create();
		}
		return super.onCreateDialog(id);
	}

	protected void launchCamera(boolean bVideo){
		launchCamera(bVideo, true);
	}
	
	protected void launchCamera(boolean bVideo,boolean bCrop){
		mIsCameraVideo = bVideo;
		if(mRequestCodeLaunchCamera == 0){
			mRequestCodeLaunchCamera = generateRequestCode();
		}
		if(bVideo){
			onlaunchVideoCapture();
		}else{
			mIsCameraCrop = bCrop;
			try{
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				FileHelper.checkOrCreateDirectory(getCameraSaveFilePath());
				intent.putExtra(MediaStore.EXTRA_OUTPUT, 
						Uri.fromFile(new File(getCameraSaveFilePath())));
				startActivityForResult(intent, mRequestCodeLaunchCamera);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	protected void onlaunchVideoCapture(){
		try{
			Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
			startActivityForResult(intent, mRequestCodeLaunchCamera);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	protected void launchPictureChoose(){
		launchPictureChoose(true);
	}
	
	protected void launchPictureChoose(boolean bCrop){
		if(mRequestCodeLaunchChoosePicture == 0){
			mRequestCodeLaunchChoosePicture = generateRequestCode();
		}
		try{
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			FileHelper.checkOrCreateDirectory(FilePaths.getPictureChooseFilePath());
			FileHelper.deleteFile(FilePaths.getPictureChooseFilePath());
			if(bCrop){
				onSetCropExtra(intent);
			}
			startActivityForResult(intent,mRequestCodeLaunchChoosePicture);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	protected void launchVideoChoose(){
		if(mRequestCodeLaunchChooseVideo == 0){
			mRequestCodeLaunchChooseVideo = generateRequestCode();
		}
		try{
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("video/*");
			startActivityForResult(intent,mRequestCodeLaunchChooseVideo);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	protected void launchFileChoose(){
		if(mRequestCodeChooseFile == 0){
			mRequestCodeChooseFile = generateRequestCode();
		}
		try{
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("application/*");
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			startActivityForResult(intent, mRequestCodeChooseFile);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(requestCode == mRequestCodeLaunchCamera){
				onCameraResult(data);
			}else if(requestCode == mRequestCodeLaunchChoosePicture){
				onPictureChooseResult(data);
			}else if(requestCode == mRequestCodeLaunchChooseVideo){
				onVideoChooseResult(data);
			}else if(requestCode == mRequestCodeChooseFile){
				onFileChooseResult(data);
			}
		}
	}
	
	protected void onCameraResult(Intent data){
		if(mIsCameraVideo){
			onVideoChooseResult(data);
		}else{
			if(mIsCameraCrop){
				try {
					Intent intent = new Intent("com.android.camera.action.CROP");
					intent.setDataAndType(Uri.fromFile(new File(getCameraSaveFilePath())), 
							"image/*");
					FileHelper.deleteFile(FilePaths.getPictureChooseFilePath());
					onSetCropExtra(intent);
					if (mRequestCodeLaunchChoosePicture == 0) {
						mRequestCodeLaunchChoosePicture = generateRequestCode();
					}
					startActivityForResult(intent, mRequestCodeLaunchChoosePicture);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				onPictureChoosed(getCameraSaveFilePath(), null);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	protected void onPictureChooseResult(Intent data){
		if(data != null){
			Uri uri = data.getData();
			if(uri != null){
				Cursor cursor = managedQuery(uri, 
						new String[]{MediaStore.Images.ImageColumns.DISPLAY_NAME,
									MediaStore.Images.ImageColumns.DATA}, 
						null, null, null);
				if(cursor != null && cursor.moveToFirst()){
					String displayName = cursor.getString(
							cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
					onPictureChoosed(cursor.getString(
							cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)), 
							displayName);
				}
			}else{
				File file = new File(FilePaths.getPictureChooseFilePath());
				if(file.exists()){
					onPictureChoosed(FilePaths.getPictureChooseFilePath(), null);
				}else{
					Object obj = data.getParcelableExtra("data");
					if(obj != null && obj instanceof Bitmap){
						final Bitmap bmp = (Bitmap)obj;
						FileHelper.saveBitmapToFile(
								FilePaths.getPictureChooseFilePath(), bmp);
						onPictureChoosed(FilePaths.getPictureChooseFilePath(), null);
					}
				}
			}
		}
	}
	
	protected void onPictureChoosed(String filePath,String displayName){
		
	}
	
	@SuppressWarnings("deprecation")
	protected void onVideoChooseResult(Intent data){
		try{
			Uri videoUri = data.getData();
			Cursor cursor = managedQuery(videoUri, null, null, null, null);
			if(cursor != null && cursor.moveToFirst()){
				final String videoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
				final long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
				onVideoChoose(videoPath,duration);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	protected void onFileChooseResult(Intent data){
		try{
			Uri uri = data.getData();
			if(uri != null){
				String path = null;
				if("content".equals(uri.getScheme())){
					Cursor cursor = managedQuery(uri, new String[]{MediaStore.MediaColumns.DATA},
							null, null, null);
					if(cursor != null && cursor.moveToFirst()){
						path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
					}else{
						return;
					}
				}else{
					File file = new File(URI.create(uri.toString()));
					path = file.getAbsolutePath();
				}
				if(!TextUtils.isEmpty(path)){
					onFileChoose(path);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	protected void onVideoChoose(String videoPath,long duration){
	}
	
	protected void onFileChoose(String path){
		
	}
	
	protected void onSetCropExtra(Intent intent){
		intent.putExtra("crop", "true");
	}
	
	protected void 		openFile(String filePath,String ext){
		if(ext == null){
			ext = FileHelper.getFileExt(filePath, "");
		}
		if(!TextUtils.isEmpty(ext)){
			ext = "." + ext.toLowerCase(Locale.getDefault());
			try{
				String arrays[] = getResources().getStringArray(R.array.fileEndingAudio);
				if(SystemUtils.isArrayContain(arrays, ext)){
					startActivity(OpenFileUtils.getAudioFileIntent(filePath));
				}else if(SystemUtils.isArrayContain(
						getResources().getStringArray(R.array.fileEndingExcel), ext)){
					startActivity(OpenFileUtils.getExcelFileIntent(filePath));
				}else if(SystemUtils.isArrayContain(
						getResources().getStringArray(R.array.fileEndingImage), ext)){
					startActivity(OpenFileUtils.getImageFileIntent(filePath));
				}else if(SystemUtils.isArrayContain(
						getResources().getStringArray(R.array.fileEndingPackage), ext)){
					startActivity(OpenFileUtils.getApkFileIntent(filePath));
				}else if(SystemUtils.isArrayContain(
						getResources().getStringArray(R.array.fileEndingPdf), ext)){
					startActivity(OpenFileUtils.getPdfFileIntent(filePath));
				}else if(SystemUtils.isArrayContain(
						getResources().getStringArray(R.array.fileEndingPPT), ext)){
					startActivity(OpenFileUtils.getPPTFileIntent(filePath));
				}else if(SystemUtils.isArrayContain(
						getResources().getStringArray(R.array.fileEndingText), ext)){
					startActivity(OpenFileUtils.getTextFileIntent(filePath));
				}else if(SystemUtils.isArrayContain(
						getResources().getStringArray(R.array.fileEndingVideo), ext)){
					startActivity(OpenFileUtils.getVideoFileIntent(filePath));
				}else if(SystemUtils.isArrayContain(
						getResources().getStringArray(R.array.fileEndingWebText), ext)){
					startActivity(OpenFileUtils.getHtmlFileIntent(filePath));
				}else if(SystemUtils.isArrayContain(
						getResources().getStringArray(R.array.fileEndingWord), ext)){
					startActivity(OpenFileUtils.getWordFileIntent(filePath));
				}else{
					ToastManager.getInstance(this).show(R.string.toast_cannot_open_file);
				}
			}catch(Exception e){
				e.printStackTrace();
				ToastManager.getInstance(this).show(R.string.toast_cannot_open_file);
			}
		}else{
			ToastManager.getInstance(this).show(R.string.toast_cannot_open_file);
		}
	}
	
	protected String 	getCameraSaveFilePath(){
		return FilePaths.getCameraSaveFilePath();
	}
	
	protected void 		setTag(Object object){
		mTag = object;
	}
	
	protected Object 	getTag(){
		return mTag;
	}
	
	protected int		generateRequestCode(){
		return ++mRequestCodeInc;
	}
	
	protected int		generateDialogId(){
		return ++mDialogIdInc;
	}
	
	protected void showProgressDialog(){
		showProgressDialog(null, null);
	}
	
	protected void showProgressDialog(String strTitle,int nStringId){
		showProgressDialog(strTitle, getString(nStringId));
	}
	
	protected void showProgressDialog(String strTitle,String strMessage){
		if(mProgressDialog == null){
			mProgressDialog = ProgressDialog.show(this, strTitle, strMessage, true, false);
		}
	}
	
	protected void dismissProgressDialog(){
		try{
			if(mProgressDialog != null){
				mProgressDialog.dismiss();
			}
		}catch(Exception e){
		}
		mProgressDialog = null;
	}
	
	protected void showXProgressDialog(){
		++mXProgressDialogShowCount;
		if(mIsXProgressDialogShowing){
			return;
		}
		final Context context = getParent() == null ? this : getParent();
		FrameLayout layout = new FrameLayout(context);
		layout.setBackgroundColor(0x90000000);
		layout.setBackgroundResource(R.drawable.loading_bg);
		ProgressBar pb = onCreateXProgressBar();
		if(pb == null){
			pb = new ProgressBar(context);
			pb.setIndeterminate(true);
		}
		int pbSize = FrameLayout.LayoutParams.WRAP_CONTENT;
		FrameLayout.LayoutParams lpPb = new FrameLayout.LayoutParams(pbSize, pbSize);
		lpPb.gravity = Gravity.CENTER;
		layout.addView(pb, lpPb);
	    int size = SystemUtils.dipToPixel(this, 70);
	    WindowManager windowManager = getParent() == null ? getWindowManager() : getParent().getWindowManager();
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
				size,
				size, 
				WindowManager.LayoutParams.TYPE_APPLICATION,
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.RGBA_8888);
		lp.gravity = Gravity.CENTER;
		
		windowManager.addView(layout, lp);
		mViewXProgressDialog = layout;
		mIsXProgressDialogShowing = true;
	}
	
	protected ProgressBar onCreateXProgressBar(){
		ProgressBar pb = new ProgressBar(this);
		pb.setIndeterminate(true);
		return pb;
	}
	
	protected void dismissXProgressDialog(){
		if(mIsXProgressDialogShowing){
			if(--mXProgressDialogShowCount == 0){
				WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
				windowManager.removeView(mViewXProgressDialog);
				mViewXProgressDialog = null;
				mIsXProgressDialogShowing = false;
			}
		}
	}
	
	protected void showDatePicker(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		showDatePicker(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH), 
				cal.get(Calendar.DAY_OF_MONTH));
	}
	
	protected void showDatePicker(int year,int month,int day){
		DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, monthOfYear);
				cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				onDateChooseResult(cal);
			}
		};
		DatePickerDialog dialog = new DatePickerDialog(this,listener, 
				year, 
				month,
				day);
		dialog.show();
	}
	
	protected void onDateChooseResult(Calendar cal){
		
	}
	
	protected View addImageButtonInTitleRight(int resId){
		if(mBaseUIFactory != null){
			return addViewInTitleRight(
					mBaseUIFactory.createTitleRightImageButton(resId),
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					mBaseUIFactory.getTitleRightImageButtonTopMargin(),
					mBaseUIFactory.getTitleRightImageButtonRightMargin());
		}
		return null;
	}
	
	protected View addTextButtonInTitleRight(int textId){
		if(mBaseUIFactory != null){
			return addViewInTitleRight(
					mBaseUIFactory.createTitleRightTextButton(textId),
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					mBaseUIFactory.getTitleRightTextButtonTopMargin(),
					mBaseUIFactory.getTitleRightTextButtonRightMargin());
		}
		return null;
	}
	
	protected View addViewInTitleRight(View v,int width,int height,
			int nTopMargin,int nRightMargin){
		final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				width,
				height);
		lp.topMargin = nTopMargin;
		lp.rightMargin = nRightMargin;
		lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mRelativeLayoutTitle.addView(v, lp);
		v.setOnClickListener(mOnClickListener);
		return v;
	}
	
	protected void 	addTextInTitle(int nResId){
		mTextViewTitle = onCreateTitleTextView(nResId);
		mRelativeLayoutTitle.addView(mTextViewTitle,onCreateTitleTextViewLayoutParams());
	}
	
	protected void 	addTextInTitle(String strText){
		mTextViewTitle = onCreateTitleTextView(strText);
		mRelativeLayoutTitle.addView(mTextViewTitle,onCreateTitleTextViewLayoutParams());
	}
	

	protected void 		onInitAttribute(BaseAttribute ba){
		if(getIntent().hasExtra("hastitle")){
			ba.mHasTitle = getIntent().getBooleanExtra("hastitle", true);
		}
	}
	
	protected View 		onCreateBackButton(){
		if(mBaseUIFactory != null){
			return mBaseUIFactory.createTitleBackButton();
		}
		return null;
	}
	
	protected TextView	onCreateTitleTextView(int nResId){
		final TextView textView = (TextView)LayoutInflater.from(this)
				.inflate(mBaseAttribute.mTitleTextLayoutId, null);
		try{
			textView.setText(nResId);
		}catch(Exception e){
		}
		
		return textView;
	}
	
	protected TextView	onCreateTitleTextView(String strText){
		final TextView textView = (TextView)LayoutInflater.from(this)
				.inflate(mBaseAttribute.mTitleTextLayoutId, null);
		textView.setText(strText);
		return textView;
	}
	
	protected RelativeLayout.LayoutParams onCreateTitleTextViewLayoutParams(){
		return new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
	}
	
	protected RelativeLayout.LayoutParams onCreateBackButtonLayoutParams(){
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		lp.leftMargin = mBaseUIFactory == null ? 0 : mBaseUIFactory.getTitleBackButtonLeftMargin();
		lp.topMargin = mBaseUIFactory == null ? 0 : mBaseUIFactory.getTitleBackButtonTopMargin();
		return lp;
	}
	
	protected void onTitleRightButtonClicked(View v){
		
	}
	
	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(v == mButtonBack){
				onBackPressed();
			}else{
				onTitleRightButtonClicked(v);
			}
		}
	};

	protected static class BaseAttribute{
		public boolean  mSetContentView = true;
		public int		mActivityLayoutId;
		
		public boolean  mHasTitle = true;
		public int		mTitleLayoutId;
		
		public boolean 	mAddTitleText = true;
		public int		mTitleTextLayoutId;
		public int		mTitleTextStringId;
		public String	mTitleText;
		
		public boolean 	mAddBackButton = false;
	}
	
	protected static class TabIndicatorMoveRunnable implements Runnable{
		
		protected final View 		mView;
		protected final int			mViewWidth;
		protected final int 		mTabWidth;
		protected final Scroller 	mScroller;
		
		public TabIndicatorMoveRunnable(View view,int nViewWidth,int nTabWidth){
			mView = view;
			mViewWidth = nViewWidth;
			mTabWidth = nTabWidth;
			mScroller = new Scroller(mView.getContext());
		}
		
		public void onTabChanged(int nTabIndex){
			final int nTargetX = mTabWidth * nTabIndex + 
					(mTabWidth - mViewWidth) / 2;
			final int nCurPadding = mView.getPaddingLeft();
			mScroller.startScroll(nCurPadding, 0, nTargetX - nCurPadding, 0, 500);
			mView.post(this);
		}

		@Override
		public void run() {
			if(mScroller.computeScrollOffset()){
				mView.setPadding(mScroller.getCurrX(), 0, 0, 0);
				mView.post(this);
			}
		}
	}
}
