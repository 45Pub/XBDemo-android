package com.xbcx.im;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.SoftReference;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.FilePaths;
import com.xbcx.core.XApplication;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.core.HttpDownloadRunner;
import com.xbcx.im.db.DBBaseRunner;
import com.xbcx.im.db.DBColumns;
import com.xbcx.library.R;
import com.xbcx.utils.FileHelper;
import com.xbcx.utils.HttpUtils;

public class VCardProvider extends IMModule implements OnEventListener{
	
	public static VCardProvider getInstance(){
		return sInstance;
	}
	
	protected static VCardProvider sInstance;
	
	protected Map<String, SoftReference<Bitmap>>	mMapIMUserToAvatar = new HashMap<String, SoftReference<Bitmap>>();

	protected Bitmap								mBmpDefault;
	
	protected Map<String, BaseVCard> 				mMapIdToVCard = new ConcurrentHashMap<String, BaseVCard>();
	protected Map<String, String>  					mMapLoadingId = new ConcurrentHashMap<String, String>();
	
	protected boolean								mUseUrlCachePath;
	
	protected VCardProvider(){
		sInstance = this;
		AndroidEventManager.getInstance().registerEventRunner(EventCode.DB_ReadVCard, new ReadVCardRunner());
		AndroidEventManager.getInstance().registerEventRunner(EventCode.DB_SaveVCard, new SaveVCardRunner());
		AndroidEventManager.getInstance().registerEventRunner(EventCode.DownloadAvatar, new DownloadAvatarRunner());
	}

	@Override
	protected void onInitial(IMKernel kernel) {
		mBmpDefault = BitmapFactory.decodeResource(kernel.getContext().getResources(), R.drawable.avatar_user);
		AndroidEventManager.getInstance().addEventListener(EventCode.IM_LoadVCard, this, false);
		AndroidEventManager.getInstance().addEventListener(EventCode.DownloadAvatar, this, false);
	}

	@Override
	protected void onRelease() {
		AndroidEventManager.getInstance().removeEventListener(EventCode.IM_LoadVCard, this);
		AndroidEventManager.getInstance().removeEventListener(EventCode.DownloadAvatar, this);
	}
	
	public Bitmap loadAvatar(String imUser){
		if(TextUtils.isEmpty(imUser)){
			return mBmpDefault;
		}
		Bitmap bmp = getCacheBitmap(imUser);
		if(bmp == null){
			if(mUseUrlCachePath){
				BaseVCard vcard = loadVCard(imUser);
				if(vcard != null){
					final String avatarurl = vcard.getAttribute("avatarurl");
					if(!TextUtils.isEmpty(avatarurl)){
						bmp = BitmapFactory.decodeFile(FilePaths.getUrlFileCachePath(avatarurl));
						if(bmp == null){
							requestDownloadAvatar(imUser, vcard.getAttribute("avatarurl"));
						}
					}
				}
			}else{
				bmp = BitmapFactory.decodeFile(
						IMFilePathManager.getInstance().getAvatarSavePath(imUser));
				if(bmp == null){
					BaseVCard vcard = loadVCard(imUser);
					if(vcard != null){
						requestDownloadAvatar(imUser, vcard.getAttribute("avatarurl"));
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
	
	public Bitmap loadAvatar(String imUser,String avatarurl){
		Bitmap bmp = getCacheBitmap(imUser);
		boolean bNeedDownload = false;
		if(bmp == null){
			if(!TextUtils.isEmpty(avatarurl)){
				if(mUseUrlCachePath){
					bmp = BitmapFactory.decodeFile(FilePaths.getUrlFileCachePath(avatarurl));
				}else{
					bmp = BitmapFactory.decodeFile(
							IMFilePathManager.getInstance().getAvatarSavePath(imUser));
				}
			}
			
			if(bmp == null){
				if(!TextUtils.isEmpty(avatarurl)){
					bNeedDownload = true;
				}
				bmp = mBmpDefault;
			}
			if(bmp != null){
				addAvatarToCache(imUser, bmp);
			}
		}
		
		if(!mUseUrlCachePath){
			BaseVCard vcard = loadVCard(imUser);
			if(vcard == null || !avatarurl.equals(vcard.getAttribute("avatarurl"))){
				requestDownloadAvatar(imUser, avatarurl);
			}
		}else{
			if(bNeedDownload){
				if(!FileHelper.isFileExists(FilePaths.getUrlFileCachePath(avatarurl))){
					requestDownloadAvatar(imUser, avatarurl);
				}
			}
		}
		
		return bmp;
	}
	
	protected Bitmap getCacheBitmap(String imUser){
		SoftReference<Bitmap> ref = mMapIMUserToAvatar.get(imUser);
		if(ref != null){
			return ref.get();
		}
		return null;
	}
	
	protected void addAvatarToCache(String imUser,Bitmap bmp){
		mMapIMUserToAvatar.put(imUser, new SoftReference<Bitmap>(bmp));
	}
	
	public void removeCacheBitmap(String imUser){
		mMapIMUserToAvatar.remove(imUser);
	}
	
	public String loadUserName(String imUser){
		BaseVCard vcard = loadVCard(imUser);
		if(vcard != null){
			return vcard.getAttribute("name");
		}
		return "";
	}
	
	public BaseVCard loadVCard(String userId){
		if(TextUtils.isEmpty(userId)){
			return null;
		}
		BaseVCard vcard = mMapIdToVCard.get(userId);
		if(vcard == null){
			vcard = loadVCardFromDB(userId);
			if(vcard == null){
				if(!mMapLoadingId.containsKey(userId)){
					AndroidEventManager.getInstance().pushEvent(
							EventCode.IM_LoadVCard, userId);
					mMapLoadingId.put(userId, userId);
				}
			}else{
				mMapIdToVCard.put(userId, vcard);
			}
		}
		return vcard;
	}
	
	protected BaseVCard loadVCardFromDB(String userId){
		Event e = AndroidEventManager.getInstance().runEvent(
				EventCode.DB_ReadVCard, 
				userId);
		BaseVCard vcard = (BaseVCard)e.getReturnParamAtIndex(0);
		if(vcard != null){
			final long updateTime = (Long)e.getReturnParamAtIndex(1);
			if(System.currentTimeMillis() - updateTime >= 86400000){
				AndroidEventManager.getInstance().pushEvent(EventCode.IM_LoadVCard, userId);
			}
		}
		return vcard;
	}

	@Override
	public void onEventRunEnd(Event event) {
		final int code = event.getEventCode();
		if(code == EventCode.IM_LoadVCard){
			final String userId = (String)event.getParamAtIndex(0);
			if(!TextUtils.isEmpty(userId)){
				mMapLoadingId.remove(userId);
				if(event.isSuccess()){
					final BaseVCard vcard = (BaseVCard)event.getReturnParamAtIndex(0);
					BaseVCard oldVCard = mMapIdToVCard.put(userId, vcard);
					
					AndroidEventManager.getInstance().runEvent(
							EventCode.DB_SaveVCard, vcard);
					onLoadVCardSuccess(userId, vcard,oldVCard);
				}
			}
		}else if(code == EventCode.DownloadAvatar){
			if(event.isSuccess()){
				final String imUser = (String)event.getParamAtIndex(0);
				mMapIMUserToAvatar.remove(imUser);
			}
		}
	}
	
	protected void onLoadVCardSuccess(String imUser,BaseVCard vcard,BaseVCard oldVcard){
		if(oldVcard == null || 
				!vcard.getAttribute("avatarurl").equals(oldVcard.getAttribute("avatarurl"))){
			requestDownloadAvatar(imUser, vcard.getAttribute("avatarurl"));
		}
	}
	
	protected void requestDownloadAvatar(String imUser,String strUrl){
		if(!TextUtils.isEmpty(strUrl)){
			AndroidEventManager.getInstance().pushEvent(
					EventCode.DownloadAvatar,
					imUser,
					strUrl);
		}
	}
	
	protected String getCreateTableSql(){
		return "CREATE TABLE " + DBColumns.VCard.TABLENAME + " (" +
				DBColumns.VCard.COLUMN_ID + " TEXT PRIMARY KEY, " +
				DBColumns.VCard.COLUMN_OBJ + " TEXT, " +
				DBColumns.VCard.COLUMN_UPDATETIME + " INTEGER);";
	}
	
	private class ReadVCardRunner extends DBBaseRunner{

		@Override
		public void onEventRun(Event event) throws Exception {
			requestExecute(true, event);
		}

		@Override
		protected String createTableSql() {
			return getCreateTableSql();
		}

		@Override
		protected void onExecute(SQLiteDatabase db, Event event) {
			final String userId = (String)event.getParamAtIndex(0);
			Cursor cursor = db.query(DBColumns.VCard.TABLENAME, null,
					DBColumns.VCard.COLUMN_ID + "='" + userId + "'", 
					null, null, null, null);		
			managerCursor(cursor);
			if(cursor != null && cursor.moveToFirst()){
				final byte data[] = cursor.getBlob(
						cursor.getColumnIndex(DBColumns.VCard.COLUMN_OBJ));
				ByteArrayInputStream bais = new ByteArrayInputStream(data);
				try{
					ObjectInputStream ois = new ObjectInputStream(bais);
					try{
						BaseVCard vcard = (BaseVCard)ois.readObject();
						event.addReturnParam(vcard);
						event.addReturnParam(
								cursor.getLong(cursor.getColumnIndex(DBColumns.VCard.COLUMN_UPDATETIME)));
					}finally{
						ois.close();
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	private class SaveVCardRunner extends DBBaseRunner{

		@Override
		public void onEventRun(Event event) throws Exception {
			requestExecute(false, event);
		}

		@Override
		protected String createTableSql() {
			return getCreateTableSql();
		}

		@Override
		protected void onExecute(SQLiteDatabase db, Event event) {
			final BaseVCard vcard = (BaseVCard)event.getParamAtIndex(0);
			final String userId = vcard.getId();
			ContentValues cv = new ContentValues();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try{
				ObjectOutputStream bos = new ObjectOutputStream(baos);
				bos.writeObject(vcard);
				cv.put(DBColumns.VCard.COLUMN_OBJ,baos.toByteArray());
				cv.put(DBColumns.VCard.COLUMN_UPDATETIME, System.currentTimeMillis());
				try{
					int ret = db.update(DBColumns.VCard.TABLENAME, cv, 
							DBColumns.VCard.COLUMN_ID + "='" + userId + "'",
							null);
					if(ret <= 0){
						cv.put(DBColumns.VCard.COLUMN_ID, userId);
						safeInsert(db, DBColumns.VCard.TABLENAME, cv);
					}
				}catch(Exception e){
					if(!tabbleIsExist(DBColumns.VCard.TABLENAME, db)){
						db.execSQL(createTableSql());
						cv.put(DBColumns.VCard.COLUMN_ID, userId);
						db.insert(DBColumns.VCard.TABLENAME, null, cv);
					}
				}finally{
					bos.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private static class DownloadAvatarRunner extends HttpDownloadRunner{
		@Override
		public void onEventRun(Event event) throws Exception {
			final String imUser = (String)event.getParamAtIndex(0);
			String url = (String)event.getParamAtIndex(1);
			url = URLDecoder.decode(url,"UTF-8");
			final String path = getInstance().mUseUrlCachePath ? 
					FilePaths.getUrlFileCachePath(url) : 
						IMFilePathManager.getInstance().getAvatarSavePath(imUser);
			event.setSuccess(HttpUtils.doDownload(url, path,
					true, null, null, null));
			if(!event.isSuccess()){
				XApplication.getLogger().info("download avatar false:" + imUser + " url = " + url);
			}
			
		}
	}
}
