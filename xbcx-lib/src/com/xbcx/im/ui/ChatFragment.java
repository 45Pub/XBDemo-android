package com.xbcx.im.ui;

import com.xbcx.view.PulldownableListView;
import com.xbcx.view.XChatEditView;

import android.support.v4.app.Fragment;
import android.widget.AbsListView;
import android.widget.AdapterView;

public abstract class ChatFragment extends Fragment implements 
											XChatEditView.OnEditListener,
											AbsListView.OnScrollListener,
											PulldownableListView.OnPullDownListener,
											IMMessageViewProvider.OnViewClickListener,
											AdapterView.OnItemLongClickListener{
	/*protected static final int MENUID_DELETEMESSAGE = 1;
	protected static final int MENUID_COPYMESSAGE	= 2;
	
	private   ChatAttribute mChatAttribute;
	private	  WakeLock		mWakeLock;
	
	protected boolean		mIsReaded;
	
	protected XChatListView		mListView;
	protected IMMessageAdapter 	mMessageAdapter;
	protected XChatEditView 	mEditView;
	protected int				mLastReadPosition;
	
	private	  int				mChoosePhotoDialogId;
	private	  int				mChooseVideoDialogId;
	
	private   int				mRequestCodeChooseFile;
	private   int				mRequestCodePhotoSendPreview;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ChatBackgroundProvider.setBackground(getActivity().getWindow().getDecorView());
		
		onInitChatAttribute(mChatAttribute = new ChatAttribute());
		
		onInit();
		
		registerForContextMenu(mListView);
		mListView.setOnItemLongClickListener(this);
		
		if(mChatAttribute.mKeepScreenOn){
			PowerManager pm = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,this.getClass().getName());
			mWakeLock.acquire();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		mListView.setAdapter(null);
		mMessageAdapter.clear();
		mMessageAdapter.clearIMMessageViewProvider();
		
		VoiceMessageUploadProcessor.getInstance().clearWaitUpload();
		VoiceMessageDownloadProcessor.getInstance().clearWaitDownload();
		
		if(mWakeLock != null){
			mWakeLock.release();
			mWakeLock = null;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mIsReaded = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		mIsReaded = false;
	}

	protected void onInit(){
		mEditView = (XChatEditView)findViewById(R.id.chatEditView);
		if(mEditView != null){
			int pluginId = getResources().getIdentifier("btnPhoto", "id", getPackageName());
			if(pluginId != 0){
				mEditView.addSendPlugin(pluginId);
			}
			pluginId = getResources().getIdentifier("btnCamera", "id", getPackageName());
			if(pluginId != 0){
				mEditView.addSendPlugin(pluginId);
			}
			pluginId = getResources().getIdentifier("btnFile", "id", getPackageName());
			if(pluginId != 0){
				mEditView.addSendPlugin(pluginId);
			}
			mEditView.setOnEditListener(this);
		}
		
		mListView = (XChatListView)findViewById(mChatAttribute.mIdListView);
		mMessageAdapter = new IMMessageAdapter(this);
		onAddMessageViewProvider();
		mMessageAdapter.setDefaultIMMessageViewProvider(new TextViewLeftProvider(this));
		mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		mListView.setEditView(mEditView);
		mListView.setOnScrollListener(this);
		mListView.setOnPullDownListener(this);
		mListView.setAdapter(mMessageAdapter);
		
		if(!TextUtils.isEmpty(mChatAttribute.mFromId)){
			addAndManageEventListener(EventCode.IM_ReceiveMessage);
		}
		
		addAndManageEventListener(EventCode.DB_DeleteMessage);
		addAndManageEventListener(EventCode.UploadChatVoice);
		addAndManageEventListener(EventCode.UploadChatPhoto);
		addAndManageEventListener(EventCode.UploadChatVideo);
		addAndManageEventListener(EventCode.DownloadChatVoice);
		addAndManageEventListener(EventCode.DownloadChatVideoThumb);
		addAndManageEventListener(EventCode.DownloadChatVideo);
		addAndManageEventListener(EventCode.VoicePlayStarted);
		addAndManageEventListener(EventCode.VoicePlayErrored);
		addAndManageEventListener(EventCode.VoicePlayCompletioned);
		addAndManageEventListener(EventCode.VoicePlayStoped);
		addAndManageEventListener(EventCode.UploadChatPhotoPercentChanged);
		addAndManageEventListener(EventCode.DownloadChatThumbPhoto);
		addAndManageEventListener(EventCode.DownloadChatThumbPhotoPercentChanged);
		addAndManageEventListener(EventCode.UploadChatVideoPercentChanged);
		addAndManageEventListener(EventCode.DownloadChatVideoPerChanged);
		addAndManageEventListener(EventCode.DownloadChatVideoThumbPerChanged);
		addAndManageEventListener(EventCode.UploadChatFilePerChanged);
		addAndManageEventListener(EventCode.UploadChatFile);
		addAndManageEventListener(EventCode.DownloadChatFile);
		addAndManageEventListener(EventCode.DownloadChatFilePerChanged);
		addAndManageEventListener(EventCode.IM_SendMessage);
	}

	protected void onInitChatAttribute(ChatAttribute attr){
	}
	
	protected void onAddMessageViewProvider(){
		IMMessageViewProviderFactory factory = IMMessageViewProvider.getIMMessageViewProviderFactory();
		if(factory != null){
			List<IMMessageViewProvider> providers = factory.createIMMessageViewProviders(this, this);
			if(providers != null){
				for(IMMessageViewProvider provider : providers){
					mMessageAdapter.addIMMessageViewProvider(provider);
				}
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
			AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
					AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
			AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
					AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	protected int loadOnePage(){
		if(mLastReadPosition >= 0){
			List<XMessage> listMessage = new LinkedList<XMessage>();
			onLoadOnePageMessage(listMessage, mLastReadPosition);
			List<XMessage> listTemp = addGroupTimeMessage(listMessage);
			
			mMessageAdapter.addAllItem(0, listTemp);
			
			onOnePageLoaded(listMessage.size());
			
			return listTemp.size();
		}else{
			mListView.setCanRun(false);
		}
		return 0;
	}
	
	protected void onLoadOnePageMessage(List<XMessage> listMessage,int nPosition){
	}
	
	protected List<XMessage> addGroupTimeMessage(List<XMessage> listMessage){
		List<XMessage> listTemp = new ArrayList<XMessage>();
		XMessage lastMessage = null;
		for(XMessage m : listMessage){
			XMessage hm = (XMessage)m;
			XMessage timeMessage = checkOrCreateTimeMessage(hm,lastMessage);
			if(timeMessage != null){
				listTemp.add(timeMessage);
			}
			listTemp.add(m);
			lastMessage = hm;
		}
		return listTemp;
	}
	
	protected void onOnePageLoaded(int nCount){
		mLastReadPosition -= nCount;
		if(mLastReadPosition < 0){
			mListView.setCanRun(false);
		}
	}
	
	@Override
	public void onRecordFail(boolean bFailByNet) {
		if(bFailByNet){
			startUnConnectionAnimation();
		}else{
			mToastManager.show(R.string.prompt_record_fail);
		}
	}
	
	@Override
	public boolean onSendCheck() {
		if(IMKernel.isIMConnectionAvailable()){
			return true;
		}else{
			startUnConnectionAnimation();
			return false;
		}
	}
	
	@Override
	public void onSendText(CharSequence s) {
		IMMessage message = new IMMessage(XMessage.buildMessageId(), XMessage.TYPE_TEXT);
		message.setContent(String.valueOf(s));
		
		onNewMessageEdited(message,true);
		saveAndSendMessage(message);
	}

	@Override
	public void onSendVoice(String strPathName) {
		IMMessage message = new IMMessage(XMessage.buildMessageId(), XMessage.TYPE_VOICE);
		message.setVoiceFrameCount(AmrParse.parseFrameCount(strPathName));
		
		onNewMessageEdited(message, true);
		
		FileHelper.copyFile(
				message.getVoiceFilePath(),
				strPathName);
		
		saveAndSendMessage(message);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onSendPlugin(int viewId) {
		if(viewId == R.id.btnPhoto){
			if(mChoosePhotoDialogId == 0){
				mChoosePhotoDialogId = generateDialogId();
			}
			showDialog(mChoosePhotoDialogId);
		}else if(viewId == R.id.btnCamera){
			if(mChooseVideoDialogId == 0){
				mChooseVideoDialogId = generateDialogId();
			}
			showDialog(mChooseVideoDialogId);
		}else if(viewId == R.id.btnFile){
			String name = ActivityType.getActivityClassName(
					ActivityType.ChooseFileActivity);
			if(!TextUtils.isEmpty(name)){
				try{
					Intent intent = new Intent(this, Class.forName(name));
					intent.putExtra("choose", true);
					if(mRequestCodeChooseFile == 0){
						mRequestCodeChooseFile = generateRequestCode();
					}
					startActivityForResult(intent, mRequestCodeChooseFile);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(requestCode == mRequestCodeChooseFile){
				onFileChooseResult(data);
			}else if(requestCode == mRequestCodePhotoSendPreview){
				Object tag = getTag();
				if(tag != null && tag instanceof Intent){
					onProcessPictureChooseData((Intent)tag);
				}
			}
		}
	}
	
	@Override
	protected void onlaunchVideoCapture() {
		if(IMGlobalSetting.videoCaptureActivityClass == null){
			super.onlaunchVideoCapture();
		}else{
			try{
				Intent intent = new Intent(this, IMGlobalSetting.videoCaptureActivityClass);
				startActivityForResult(intent, mRequestCodeLaunchCamera);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onCameraResult(Intent data){
		if(mIsCameraVideo){
			super.onCameraResult(data);
		}else{
			if(IMGlobalSetting.photoCrop){
				super.onCameraResult(data);
			}else{
				if(FileHelper.isFileExists(getCameraSaveFilePath())){
					final int rotate = getPictureRotateAngle(FilePaths.getCameraSaveFilePath());
					BitmapFactory.Options op = new BitmapFactory.Options();
					op.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(FilePaths.getCameraSaveFilePath(), op);
					if(op.outWidth > 512 || op.outHeight > 512){
						if(op.outWidth > op.outHeight){
							op.inSampleSize = SystemUtils.nextPowerOf2(op.outWidth / 512);
						}else{
							op.inSampleSize = SystemUtils.nextPowerOf2(op.outHeight / 512);
						}
						op.inJustDecodeBounds = false;
						Bitmap bmp = BitmapFactory.decodeFile(FilePaths.getCameraSaveFilePath(), op);
						if(rotate != 0){
							try{
								Matrix matrix = new Matrix();
								matrix.preRotate(rotate);
								bmp = Bitmap.createBitmap(bmp, 
										0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
							}catch(OutOfMemoryError e){
							}
						}
						if(bmp != null){
							FileHelper.saveBitmapToFile(FilePaths.getCameraSaveFilePath(), bmp);
						}
					}else{
						if(rotate != 0){
							try{
								final Bitmap bmpOld = BitmapFactory.decodeFile(FilePaths.getCameraSaveFilePath());
								Matrix matrix = new Matrix();
								matrix.preRotate(rotate);
								final Bitmap bmpNew = Bitmap.createBitmap(bmpOld, 
										0, 0, bmpOld.getWidth(), bmpOld.getHeight(), matrix, true);
								FileHelper.saveBitmapToFile(FilePaths.getCameraSaveFilePath(), bmpNew);
							}catch(OutOfMemoryError e){
							}
						}
					}
					
					sendPhoto(FilePaths.getCameraSaveFilePath(),null);
				}else{
					XApplication.getLogger().warning("file not exists");
					if(data != null){
						XApplication.getLogger().warning(data.getDataString());
					}
				}
			}
		}
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	protected void onFileChooseResult(Intent data){
		if(data != null){
			try{
				ArrayList<FileItem> fileItems = (ArrayList<FileItem>)
						data.getSerializableExtra("fileitems");
				for(FileItem fileItem : fileItems){
					if(fileItem.getFileType() == FileItem.FILETYPE_PIC){
						IMMessage m = new IMMessage(XMessage.buildMessageId(), 
								XMessage.TYPE_PHOTO);
						onNewMessageEdited(m, true);
						m.setDisplayName(fileItem.getName());
						FileHelper.copyFile(m.getPhotoFilePath(), fileItem.getPath());
						saveAndSendMessage(m);
					}else if(fileItem.getFileType() == FileItem.FILETYPE_VIDEO){
						final String videoPath = fileItem.getPath();
						Cursor cursor = managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, 
								new String[]{MediaStore.Video.Media.DURATION},
								MediaStore.Video.Media.DATA + "='" + videoPath + "'",
								null, null);
						long duration = 0;
						if(cursor != null && cursor.moveToFirst()){
							duration = cursor.getLong(cursor.getColumnIndex(
									MediaStore.Video.Media.DURATION));
						}
						
						sendVideo(videoPath, duration);
					}else{
						IMMessage m = new IMMessage(XMessage.buildMessageId(), 
								XMessage.TYPE_FILE);
						onNewMessageEdited(m, true);
						m.setDisplayName(fileItem.getName());
						m.setFileSize(fileItem.getFileSize());
						FileHelper.copyFile(m.getFilePath(), fileItem.getPath());
						saveAndSendMessage(m);
					}
				}
				
				mEditView.hideAllPullUpView(true);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPictureChooseResult(Intent data) {
		if(data != null){
			if(IMGlobalSetting.photoCrop){
				sendPhoto(FilePaths.getPictureChooseFilePath(), null);
			}else{
				if(IMGlobalSetting.photoSendPreview && 
						IMGlobalSetting.photoSendPreviewActivityClass != null){
					setTag(data);
					if(mRequestCodePhotoSendPreview == 0){
						mRequestCodePhotoSendPreview = generateRequestCode();
					}
					Uri uri = data.getData();
					String filePath = null;
					if(uri != null){
						Cursor cursor = managedQuery(uri, 
								new String[]{MediaStore.Images.ImageColumns.DATA}, 
								null, null, null);
						if(cursor != null && cursor.moveToFirst()){
							filePath = cursor.getString(
									cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
						}
					}else{
						Object obj = data.getParcelableExtra("data");
						if(obj != null && obj instanceof Bitmap){
							final Bitmap bmp = (Bitmap)obj;
							filePath = FilePaths.getPictureChooseFilePath();
							FileHelper.saveBitmapToFile(filePath, bmp);
						}else{
							if(FileHelper.isFileExists(FilePaths.getPictureChooseFilePath())){
								filePath = FilePaths.getPictureChooseFilePath();
							}else{
								XApplication.getLogger().warning("choose picture return error");
							}
							
						}
					}
					if(!TextUtils.isEmpty(filePath)){
						Intent intent = new Intent(this, IMGlobalSetting.photoSendPreviewActivityClass);
						intent.putExtra("path", filePath);
						startActivityForResult(intent, mRequestCodePhotoSendPreview);
					}
				}else{
					onProcessPictureChooseData(data);
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	protected void onProcessPictureChooseData(Intent data){
		Uri uri = data.getData();
		if(uri != null){
			Cursor cursor = managedQuery(uri, 
					new String[]{MediaStore.Images.ImageColumns.DISPLAY_NAME,
								MediaStore.Images.ImageColumns.DATA}, 
					null, null, null);
			if(cursor != null && cursor.moveToFirst()){
				String displayName = cursor.getString(
						cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
				sendPhoto(cursor.getString(
						cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)), 
						displayName);
			}
		}else{
			Object obj = data.getParcelableExtra("data");
			if(obj != null && obj instanceof Bitmap){
				final Bitmap bmp = (Bitmap)obj;
				FileHelper.saveBitmapToFile(FilePaths.getPictureChooseFilePath(), bmp);
				sendPhoto(FilePaths.getPictureChooseFilePath(), null);
			}else{
				if(FileHelper.isFileExists(FilePaths.getPictureChooseFilePath())){
					sendPhoto(FilePaths.getPictureChooseFilePath(), null);
				}else{
					XApplication.getLogger().warning("preview picture return error");
				}
			}
		}
	}
	
	protected void sendPhoto(String filePath,String displayName){
		BitmapFactory.Options op = new BitmapFactory.Options();
		op.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, op);
		if(op.outWidth < 0){
			mToastManager.show(R.string.toast_cannot_send_photo);
			return;
		}
		
		IMMessage m = new IMMessage(XMessage.buildMessageId(), XMessage.TYPE_PHOTO);
		
		if(!TextUtils.isEmpty(displayName)){
			m.setDisplayName(displayName);
		}
		
		onNewMessageEdited(m, true);
		final String choosePicPath = filePath;
		int rotate = 0;
		
		final String strPhotoPath = m.getPhotoFilePath();
		if(rotate == 0){
			FileHelper.copyFile(strPhotoPath, choosePicPath);
		}else{
			final Bitmap bmpOld = BitmapFactory.decodeFile(choosePicPath);
			Matrix matrix = new Matrix();
			matrix.preRotate(rotate);
			final Bitmap bmpNew = Bitmap.createBitmap(bmpOld, 
					0, 0, bmpOld.getWidth(), bmpOld.getHeight(), matrix, true);
			FileHelper.saveBitmapToFile(strPhotoPath, bmpNew);
		}
		
		saveAndSendMessage(m);
		
		if(mEditView != null){
			mEditView.hideAllPullUpView(true);
		}
	}
	
	protected int  getPictureRotateAngle(String filePath){
		int rotate = 0;
		try{
			ExifInterface ei = new ExifInterface(filePath);
			int ori = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			if(ori == ExifInterface.ORIENTATION_ROTATE_180){
				rotate = 180;
			}else if(ori == ExifInterface.ORIENTATION_ROTATE_270){
				rotate = 270;
			}else if(ori == ExifInterface.ORIENTATION_ROTATE_90){
				rotate = 90;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return rotate;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onVideoChooseResult(Intent data) {
		final String url = data.getDataString();
		if(url != null){
			if(url.contains("content")){
				super.onVideoChooseResult(data);
			}else{
				long duration = 0;
				Cursor cursor = managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
						new String[]{MediaStore.Video.Media.DURATION},
						MediaStore.Video.Media.DATA + "='" + url + "'", 
						null, null);
				if(cursor != null && cursor.moveToFirst()){
					duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
				}
				onVideoChoose(url, duration);
			}
		}
	}

	@Override
	protected void onVideoChoose(String videoPath,long duration) {
		super.onVideoChoose(videoPath,duration);
		sendVideo(videoPath, duration);
		
		if(mEditView != null){
			mEditView.hideAllPullUpView(true);
		}
	}
	
	protected XMessage sendVideo(String videoPath,long duration){
		IMMessage m = new IMMessage(XMessage.buildMessageId(), XMessage.TYPE_VIDEO);
		onNewMessageEdited(m, true);
		m.setVideoFilePath(videoPath);
		m.setVideoSeconds((int)duration / 1000);
		
		final Bitmap bmp = SystemUtils.getVideoThumbnail(videoPath);
		if(bmp != null){
			FileHelper.saveBitmapToFile(m.getVideoThumbFilePath(), bmp);
		}
		
		saveAndSendMessage(m);
		
		return m;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if(id == mChoosePhotoDialogId){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			MenuItemAdapter adapter = new MenuItemAdapter(this);
			adapter.addItem(new MenuItemAdapter.MenuItem(MENUID_PHOTO_CAMERA, R.string.photograph));
			adapter.addItem(new MenuItemAdapter.MenuItem(MENUID_PHOTO_FILE, R.string.choose_from_albums));
			builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which == 0){
						launchCamera(false);
					}else if(which == 1){
						launchPictureChoose();
					}
				}
			});
			return builder.create();
		}else if(id == mChooseVideoDialogId){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			MenuItemAdapter adapter = new MenuItemAdapter(this);
			adapter.addItem(new MenuItemAdapter.MenuItem(MENUID_VIDEO_CAMERA, R.string.shoot_video));
			adapter.addItem(new MenuItemAdapter.MenuItem(MENUID_VIDEO_FILE, R.string.choose_from_albums));
			builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which == 0){
						launchCamera(true);
					}else if(which == 1){
						launchVideoChoose();
					}
				}
			});
			return builder.create();
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onSetCropExtra(Intent intent) {
		if(IMGlobalSetting.photoCrop){
			super.onSetCropExtra(intent);
			intent.putExtra("return-data", false);
	        intent.putExtra("noFaceDetection", true);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, 
					Uri.fromFile(new File(FilePaths.getPictureChooseFilePath())));
			intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		}else{
			intent.putExtra(MediaStore.EXTRA_OUTPUT, 
					Uri.fromFile(new File(FilePaths.getPictureChooseFilePath())));
			intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		}
	}
	
	protected void onNewMessageEdited(XMessage m,boolean bScrollToBottom){
		onInitMessage(m);
		
		if(bScrollToBottom){
			mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		}
		
		XMessage timeMessage = checkOrCreateTimeMessage(m);
		if(timeMessage != null){
			mMessageAdapter.addItem(timeMessage);
		}
		mMessageAdapter.addItem(m);
	}
	
	protected XMessage checkOrCreateTimeMessage(XMessage m){
		final int nItemCount = mMessageAdapter.getCount();
		XMessage lastMessage = nItemCount > 0 ? 
				(XMessage)mMessageAdapter.getItem(nItemCount - 1) : null;
		return checkOrCreateTimeMessage(m, lastMessage);
	}
	
	protected XMessage checkOrCreateTimeMessage(XMessage m,XMessage lastMessage){
		long sendTimeLast = lastMessage == null ? 0 : lastMessage.getSendTime();
		if (m.getSendTime() - sendTimeLast >= 120000) {
			return IMMessage.createTimeMessage(m.getSendTime());
		}
		return null;
	}
	
	protected void saveAndSendMessage(XMessage m){
		mEventManager.runEvent(EventCode.DB_SaveMessage, m);
		mEventManager.pushEvent(EventCode.HandleRecentChat, m);
		
		onSendMessage(m);
	}
	
	protected void onSendMessage(XMessage m){
		if(IMKernel.isIMConnectionAvailable()){
			final int nType = m.getType();
			if(nType == XMessage.TYPE_VOICE){
				VoiceMessageUploadProcessor.getInstance().requestUpload(m);
				redrawMessage(m);
			}else if(nType == XMessage.TYPE_PHOTO){
				PhotoMessageUploadProcessor.getInstance().requestUpload(m);
				redrawMessage(m);
			}else if(nType == XMessage.TYPE_VIDEO){
				VideoMessageUploadProcessor.getInstance().requestUpload(m);
				redrawMessage(m);
			}else if(nType == XMessage.TYPE_FILE){
				FileMessageUploadProcessor.getInstance().requestUpload(m);
				redrawMessage(m);
			}else{
				mEventManager.pushEvent(EventCode.IM_SendMessage,m);
			}
		}
	}
	
	protected void onInitMessage(XMessage m){
		m.setFromSelf(true);
		m.setSendTime(System.currentTimeMillis());
		
		if(!IMKernel.isIMConnectionAvailable()){
			m.setSended();
			m.setSendSuccess(false);
		}
	}
	
	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int nCode = event.getEventCode();
		if(nCode == EventCode.IM_ReceiveMessage){
			XMessage message = (XMessage)event.getParamAtIndex(0);
			if(MessageFilter.accept(mChatAttribute.mFromId, message)){
				onReceiveMessage(message);
			}
		}else if(nCode == EventCode.UploadChatVoice ||
				nCode == EventCode.UploadChatVideo ||
				nCode == EventCode.UploadChatPhoto ||
				nCode == EventCode.UploadChatFile){
			mMessageAdapter.notifyDataSetChanged();
		}else if(nCode == EventCode.DownloadChatVoice ||
				nCode == EventCode.DownloadChatVideo ||
				nCode == EventCode.DownloadChatVideoThumb ||
				nCode == EventCode.DownloadChatFile){
			mMessageAdapter.notifyDataSetChanged();
		}else if(nCode == EventCode.VoicePlayStarted ||
				nCode == EventCode.VoicePlayErrored ||
				nCode == EventCode.VoicePlayCompletioned ||
				nCode == EventCode.VoicePlayStoped){
			redrawMessage((XMessage)event.getParamAtIndex(0));
		}else if(nCode == EventCode.UploadChatPhotoPercentChanged ||
				nCode == EventCode.DownloadChatThumbPhoto ||
				nCode == EventCode.DownloadChatThumbPhotoPercentChanged ||
				nCode == EventCode.UploadChatVideoPercentChanged ||
				nCode == EventCode.DownloadChatVideoPerChanged ||
				nCode == EventCode.DownloadChatVideoThumbPerChanged ||
				nCode == EventCode.UploadChatFilePerChanged ||
				nCode == EventCode.DownloadChatFilePerChanged){
			redrawMessage((XMessage)event.getParamAtIndex(0));
		}else if(nCode == EventCode.IM_SendMessage){
			redrawMessage(null);
		}else if(nCode == EventCode.DB_DeleteMessage){
			final String id = (String)event.getParamAtIndex(0);
			if(mChatAttribute.mFromId.equals(id)){
				if(event.getParamAtIndex(1) == null){
					mMessageAdapter.clear();
				}
			}
		}
	}
	
	protected void onReceiveMessage(XMessage m){
		XMessage timeM = checkOrCreateTimeMessage(m);
		if(timeM != null){
			mMessageAdapter.addItem(timeM);
		}
		mMessageAdapter.addItem(m);
		
		if(!m.isReaded()){
			m.setReaded(mIsReaded);
		}
		
		if(m.getType() == XMessage.TYPE_PHOTO){
			PhotoMessageDownloadProcessor.getInstance().requestDownload(m, true);
		}else if(m.getType() == XMessage.TYPE_VOICE){
			VoiceMessageDownloadProcessor.getInstance().requestDownload(m);
		}else if(m.getType() == XMessage.TYPE_VIDEO){
			VideoMessageDownloadProcessor.getInstance().requestDownload(m, true);
		}
	}
	
	@Override
	public void onStartRun(PulldownableListView view) {
		if(mLastReadPosition >= 0){
			final int nLoadCount = loadOnePage();
			
			mListView.setSelection(nLoadCount - 1 + mListView.getHeaderViewsCount());
			
			mListView.endRun();
		}
	}
	
	@Override
	public void onViewClicked(XMessage message, int nViewId) {
		XMessage m = (XMessage)message;
		if (nViewId == R.id.viewContent) {
			final int nType = m.getType();
			if (nType == XMessage.TYPE_VOICE) {
				onVoiceContentViewClicked(m);
			}else if(nType == XMessage.TYPE_PHOTO){
				onPhotoContentViewClicked(m);
			}else if(nType == XMessage.TYPE_VIDEO){
				onVideoContentViewClicked(m);
			}
		}else if(nViewId == R.id.ivAvatar){
			onAvatarClicked(m);
		}else if(nViewId == R.id.ivWarning){
			onWarningViewClicked(m);
		}
		else if(nViewId == R.id.btn){
			if(message.isFromSelf()){
				final int type = message.getType();
				if(type == XMessage.TYPE_FILE){
					if(message.isFileUploading()){
						FileMessageUploadProcessor.getInstance().stopUpload(message);
					}else if(message.isFileDownloading()){
						FileMessageDownloadProcessor.getInstance().stopDownload(message, false);
					}else{
						FileMessageUploadProcessor.getInstance().requestUpload(message);
					}
				}else if(type == XMessage.TYPE_VIDEO){
					if(message.isVideoUploading()){
						VideoMessageUploadProcessor.getInstance().stopUpload(message);
					}else if(message.isVideoDownloading()){
						VideoMessageDownloadProcessor.getInstance().stopDownload(message, false);
					}else{
						VideoMessageUploadProcessor.getInstance().requestUpload(message);
					}
				}
			}else{
				final int type = message.getType();
				if(type == XMessage.TYPE_FILE){
					if(message.isFileDownloading()){
						FileMessageDownloadProcessor.getInstance().stopDownload(message, false);
					}else{
						FileMessageDownloadProcessor.getInstance().requestDownload(message, false);
					}
				}else if(type == XMessage.TYPE_VIDEO){
					if(message.isVideoDownloading()){
						VideoMessageDownloadProcessor.getInstance().stopDownload(message, false);
					}else{
						VideoMessageDownloadProcessor.getInstance().requestDownload(message, false);
					}
				}
			}
		}
	}
	
	protected void onVoiceContentViewClicked(XMessage m){
		if (m.isFromSelf()) {
			if (!m.isVoiceUploading()) {
				if (m.isUploadSuccess()) {
					if (m.isVoiceFileExists()) {
						if(VoicePlayProcessor.getInstance().isPlaying(m)){
							VoicePlayProcessor.getInstance().stop();
						}else{
							VoicePlayProcessor.getInstance().play(m);
						}
					} else {
						if (XApplication.checkExternalStorageAvailable()) {
							VoiceMessageDownloadProcessor.getInstance().requestDownload(m);
						}
					}
				} else {
					VoiceMessageUploadProcessor.getInstance().requestUpload(m);
					redrawMessage(m);
				}
			}
		} else if (!VoiceMessageDownloadProcessor.getInstance().isDownloading(m)) {
			if (m.isVoiceFileExists()) {
				if(VoicePlayProcessor.getInstance().isPlaying(m)){
					VoicePlayProcessor.getInstance().stop();
				}else{
					VoicePlayProcessor.getInstance().play(m);
				}
			} else {
				if (XApplication.checkExternalStorageAvailable()) {
					VoiceMessageDownloadProcessor.getInstance().requestDownload(m);
					redrawMessage(m);
				}
			}
		}
	}
	
	protected void onPhotoContentViewClicked(XMessage m){
		if(m.isFromSelf()){
			if(!PhotoMessageUploadProcessor.getInstance().isUploading(m)){
				if(m.isUploadSuccess()){
					if(m.isPhotoFileExists()){
						viewDetailPhoto(m);
					}else{
						if(m.isThumbPhotoFileExists()){
							viewDetailPhoto(m);
						}else{
							if(!m.isThumbPhotoDownloading()){
								PhotoMessageDownloadProcessor.getInstance().requestDownload(m, true);
							}
						}
					}
				}else{
					PhotoMessageUploadProcessor.getInstance().requestUpload(m);
				}
			}
		}else{
			if(!m.isThumbPhotoDownloading()){
				if(m.isThumbPhotoFileExists()){
					BitmapFactory.Options op = new BitmapFactory.Options();
					SystemUtils.computeSampleSize(op,m.getThumbPhotoFilePath(),100, 100 * 100);
					Bitmap bmp = BitmapFactory.decodeFile(m.getThumbPhotoFilePath(), op);
					if(bmp == null){
						FileHelper.deleteFile(m.getThumbPhotoFilePath());
						PhotoMessageDownloadProcessor.getInstance().requestDownload(m, true);
					}else{
						viewDetailPhoto(m);
					}
				}else{
					PhotoMessageDownloadProcessor.getInstance().requestDownload(m, true);
					redrawMessage(m);
				}
			}
		}
	}
	
	protected void onVideoContentViewClicked(XMessage xm){
		if(xm.isFromSelf()){
			if(!xm.isVideoUploading()){
				if(xm.isUploadSuccess()){
					viewVideo(xm);
				}else{
					VideoMessageUploadProcessor.getInstance().requestUpload(xm);
					redrawMessage(xm);
				}
			}
		}else{
			if(!xm.isVideoThumbDownloading()){
				if(xm.isVideoThumbFileExists()){
					if(!xm.isVideoDownloading()){
						if(xm.isVideoFileExists()){
							viewVideo(xm);
						}else{
							VideoMessageDownloadProcessor.getInstance().requestDownload(xm, false);
							redrawMessage(xm);
						}
					}
				}else{
					VideoMessageDownloadProcessor.getInstance().requestDownload(xm, true);
					redrawMessage(xm);
				}
			}
		}
	}
	
	protected void onAvatarClicked(XMessage m){
		if(m.isFromSelf()){
			ActivityType.launchChatActivity(this, ActivityType.SelfDetailActivity, null, null);
		}else{
			ActivityType.launchChatActivity(this, ActivityType.UserDetailActivity,
					m.getUserId(), m.getUserName());
		}
	}
	
	protected void onWarningViewClicked(XMessage m){
		if(!m.isSendSuccess()){
			onSendMessage(m);
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Object object = parent.getItemAtPosition(position);
		if(object != null && object instanceof XMessage){
			final XMessage xm = (XMessage)object;
			if(xm.getType() == XMessage.TYPE_TIME){
				return true;
			}
			setTag(object);
		}
		return false;
	}

	@Override
	public boolean onViewLongClicked(XMessage message, int nViewId) {
		setTag(message);
		openContextMenu(mListView);
		return true;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		Object tag = getTag();
		if(tag != null && tag instanceof XMessage){
			XMessage m = (XMessage)tag;
			menu.setHeaderTitle(getContextMenuTitle(m));
			menu.add(0, MENUID_DELETEMESSAGE, 0, R.string.deletemessage);
			if(m.getType() == XMessage.TYPE_TEXT){
				menu.add(0, MENUID_COPYMESSAGE, 0, R.string.copymessage);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case MENUID_DELETEMESSAGE:
			onDeleteMessage((XMessage)getTag());
			break;
		case MENUID_COPYMESSAGE:
			final XMessage xm = (XMessage)getTag();
			SystemUtils.copyToClipBoard(this, xm.getContent());
			break;
		}
		return super.onContextItemSelected(item);
	}
	
	protected void onDeleteMessage(XMessage m){
		if(m.getType() == XMessage.TYPE_PHOTO){
			if(m.isThumbPhotoDownloading()){
				PhotoMessageDownloadProcessor.getInstance().stopDownload(m, true);
			}
			if(m.isPhotoUploading()){
				PhotoMessageUploadProcessor.getInstance().stopUpload(m);
			}
			FileHelper.deleteFile(m.getPhotoFilePath());
			FileHelper.deleteFile(m.getThumbPhotoFilePath());
		}else if(m.getType() == XMessage.TYPE_VOICE){
			if(VoicePlayProcessor.getInstance().isPlaying(m)){
				VoicePlayProcessor.getInstance().stop();
			}
			if(m.isVoiceDownloading()){
				VoiceMessageDownloadProcessor.getInstance().stopDownload(m);
			}
			if(m.isVoiceUploading()){
				VoiceMessageUploadProcessor.getInstance().stopUpload(m);
			}
			FileHelper.deleteFile(m.getVoiceFilePath());
		}else if(m.getType() == XMessage.TYPE_VIDEO){
			if(m.isVideoDownloading()){
				VideoMessageDownloadProcessor.getInstance().stopDownload(m, false);
			}
			if(m.isVideoUploading()){
				VideoMessageUploadProcessor.getInstance().stopUpload(m);
			}
			if(!m.isFromSelf()){
				FileHelper.deleteFile(m.getVideoFilePath());
			}
			FileHelper.deleteFile(m.getVideoThumbFilePath());
		}else if(m.getType() == XMessage.TYPE_FILE){
			if(m.isFileDownloading()){
				FileMessageDownloadProcessor.getInstance().stopDownload(m, false);
			}
			if(m.isFileUploading()){
				FileMessageUploadProcessor.getInstance().stopUpload(m);
			}
			FileHelper.deleteFile(m.getFilePath());
		}
		int nIndex = mMessageAdapter.indexOf(m);
		XMessage lastM = (XMessage)mMessageAdapter.getItem(nIndex - 1);
		if(lastM.getType() == XMessage.TYPE_TIME){
			boolean bDeleteLast = false;
			if(mMessageAdapter.getCount() > nIndex + 1){
				XMessage nextM = (XMessage)mMessageAdapter.getItem(nIndex + 1);
				if(nextM.getType() == XMessage.TYPE_TIME){
					bDeleteLast = true;
				}
			}else{
				bDeleteLast = true;
			}
			if(bDeleteLast){
				mMessageAdapter.removeItem(nIndex - 1);
				--nIndex;
			}
			
		}
		mMessageAdapter.removeItem(nIndex);
	}
	
	protected String getContextMenuTitle(XMessage message){
		return String.valueOf(mTextViewTitle.getText());
	}

	protected void viewDetailPhoto(XMessage m){
		ViewPictureActivity.launch(this, m);
	}
	
	protected void viewVideo(XMessage m){
		try{
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(m.getVideoFilePath())), "video/*");
			startActivity(intent);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	protected void redrawMessage(XMessage m){
		if(mMessageAdapter.isIMMessageViewVisible(m)){
			mMessageAdapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || 
				scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING )&& 
				mListView.getLastVisiblePosition() == mListView.getCount() - 1){
			mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		}else{
			mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	}

	protected static class ChatAttribute{
		protected boolean 	mKeepScreenOn 	= true;
		
		protected int		mIdListView 	= R.id.lv;
		
		protected String	mFromId;
	}*/
}
