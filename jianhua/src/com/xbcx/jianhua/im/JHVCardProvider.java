package com.xbcx.jianhua.im;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.EventCode;
import com.xbcx.core.FilePaths;
import com.xbcx.im.BaseVCard;
import com.xbcx.im.IMFilePathManager;
import com.xbcx.im.VCardProvider;
import com.xbcx.utils.FileHelper;

public class JHVCardProvider extends VCardProvider {
	
	public static JHVCardProvider getInstance(){
		return sInstance;
	}
	
	private static JHVCardProvider sInstance;

	protected JHVCardProvider(){
		sInstance = this;
	}

	@Override
	public Bitmap loadAvatar(String imUser) {
		if(TextUtils.isEmpty(imUser)){
			return mBmpDefault;
		}
		Bitmap bmp = getCacheBitmap(imUser);
		if(bmp == null){
			bmp = BitmapFactory.decodeFile(
					IMFilePathManager.getInstance().getAvatarSavePath(imUser));
			if(bmp == null){
				BaseVCard vcard = loadVCard(imUser);
				if(vcard != null){
					final String avatarurl = vcard.getAttribute("avatarurl");
					if(!TextUtils.isEmpty(avatarurl)){
						bmp = BitmapFactory.decodeFile(FilePaths.getUrlFileCachePath(avatarurl));
						if(bmp == null){
							requestDownloadAvatar(imUser, avatarurl);
						}
					}
				}
			}
			
			if(bmp == null){
				bmp = mBmpDefault;
			}
			
			if(bmp != null){
				addAvatarToCache(imUser, bmp);
			}
		}
		
		return bmp;
	}

	@Override
	public Bitmap loadAvatar(String imUser, String avatarurl) {
		return super.loadAvatar(imUser, avatarurl);
	}

	@Override
	public String loadUserName(String imUser) {
		String name = super.loadUserName(imUser);
		if(TextUtils.isEmpty(name)){
			name = imUser;
		}
		return name;
	}
	
	public void	saveInfo(String imUser,String name,String avatarurl){
		BaseVCard old = mMapIdToVCard.get(imUser);
		if(old == null || 
				!old.getAttribute("avatarurl").equals(avatarurl) ||
				!old.getAttribute("name").equals(name)){
			BaseVCard bv = new BaseVCard(imUser);
			bv.addAttribute("name", name);
			bv.addAttribute("avatarurl", avatarurl);
			mMapIdToVCard.put(imUser, bv);
			mMapIMUserToAvatar.remove(imUser);
			try{
				FileHelper.deleteFile(IMFilePathManager.getInstance().getAvatarSavePath(imUser));
			}catch(Exception e){
				
			}
			
			AndroidEventManager.getInstance().runEvent(EventCode.DB_SaveVCard, bv);
			
			AndroidEventManager.getInstance().runEvent(EventCode.IM_UserInfoChanged);
		}
	}
}
