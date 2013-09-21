package com.xbcx.core;

import java.io.File;

import com.xbcx.core.XApplication;
import com.xbcx.utils.Encrypter;
import com.xbcx.utils.SystemUtils;

public class FilePaths {
	public static String getAvatarTempFilePath(){
		return SystemUtils.getExternalCachePath(XApplication.getApplication()) + 
				File.separator + "tempavatar";
	}
	
	public static String getCameraSaveFilePath(){
		return SystemUtils.getExternalCachePath(XApplication.getApplication()) + 
				File.separator + "camera";
	}
	
	public static String getPictureChooseFilePath(){
		return SystemUtils.getExternalCachePath(XApplication.getApplication()) + 
				File.separator + "choose.jpg";
	}
	
	public static String getUrlFileCachePath(String strUrl){
		return SystemUtils.getExternalCachePath(XApplication.getApplication()) + 
				File.separator + "urlfile" + File.separator + Encrypter.encryptBySHA1(strUrl);
	}
	
	public static String getQrcodeSavePath(String strIMUser){
		return SystemUtils.getExternalCachePath(XApplication.getApplication()) + 
				File.separator + "qrcode" + File.separator + strIMUser;
	}
	
	public static String getImportFolderPath(){
		return SystemUtils.getExternalCachePath(XApplication.getApplication()) +
				File.separator + "importfile" + File.separator;
	}
	
	public static String getCameraVideoFolderPath(){
		return SystemUtils.getExternalCachePath(XApplication.getApplication()) +
				File.separator + "videos" + File.separator;
	}
}
