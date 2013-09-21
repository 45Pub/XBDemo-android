package com.xbcx.im.ui;

import com.xbcx.im.XMessage;

import android.app.Activity;
import android.content.Intent;

public interface SendPlugin {
	
	public static final int REQUESTCODE_MIN				= 100;
	
	public static final int	SENDTYPE_PHOTO_ALL		 	= 1;
	public static final int	SENDTYPE_PHOTO_ALBUMS 		= 2;
	public static final int	SENDTYPE_PHOTO_CAMERA 		= 3;
	public static final int SENDTYPE_VIDEO_ALL			= 4;
	public static final int SENDTYPE_VIDEO_ALBUMS		= 5;
	public static final int SENDTYPE_VIDEO_CAMERA		= 6;
	public static final int SENDTYPE_FILE				= 7;
	
	public int		getSendType();
	
	public int		getLaunchActivityRequestCode();
	
	public void		startActivityForResult(Activity activity,int requestCode);
	
	public XMessage	onActivityResult(int resultCode,int requestCode,Intent data);
	
	public XMessage	onCreateXMessage();
	
	public boolean	isHideEditPullUpView();
}
