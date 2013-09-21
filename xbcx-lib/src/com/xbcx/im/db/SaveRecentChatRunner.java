package com.xbcx.im.db;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.xbcx.core.Event;
import com.xbcx.im.RecentChat;

public class SaveRecentChatRunner extends DBBaseRunner {

	@Override
	public void onEventRun(Event event) throws Exception {
		requestExecute(false, event);
	}

	@Override
	protected void onExecute(SQLiteDatabase db, Event event) {
		RecentChat rc = (RecentChat)event.getParamAtIndex(0);
		ContentValues cv = new ContentValues();
		cv.put(DBColumns.RecentChatDB.COLUMN_NAME, rc.getName());
		cv.put(DBColumns.RecentChatDB.COLUMN_UNREADCOUNT, rc.getUnreadMessageCount());
		cv.put(DBColumns.RecentChatDB.COLUMN_UPDATETIME, rc.getTime());
		cv.put(DBColumns.RecentChatDB.COLUMN_CONTENT, rc.getContent());
		if(rc.isExtraObjChanged()){
			if(rc.getExtraObj() != null){
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try{
					ObjectOutputStream oos = new ObjectOutputStream(baos);
					oos.writeObject(rc.getExtraObj());
					cv.put(DBColumns.RecentChatDB.COLUMN_EXTRAOBJ, baos.toByteArray());
					rc.setExtraObjChanged(false);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		try {
			int nRet = db.update(DBColumns.RecentChatDB.TABLENAME,
					cv, 
					DBColumns.RecentChatDB.COLUMN_ID + "='" + rc.getId() + "'", null);
			if (nRet <= 0) {
				cv.put(DBColumns.RecentChatDB.COLUMN_ID, rc.getId());
				cv.put(DBColumns.RecentChatDB.COLUMN_LOCAL_AVATAR, rc.getLocalAvatar());
				cv.put(DBColumns.RecentChatDB.COLUMN_ACTIVITY_TYPE, rc.getActivityType());
				safeInsert(db, DBColumns.RecentChatDB.TABLENAME, cv);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (!tabbleIsExist(DBColumns.RecentChatDB.TABLENAME, db)) {
				cv.put(DBColumns.RecentChatDB.COLUMN_ID, rc.getId());
				cv.put(DBColumns.RecentChatDB.COLUMN_LOCAL_AVATAR, rc.getLocalAvatar());
				cv.put(DBColumns.RecentChatDB.COLUMN_ACTIVITY_TYPE, rc.getActivityType());
				db.execSQL(createTableSql());
				db.insert(DBColumns.RecentChatDB.TABLENAME, null, cv);
			}
		}
	}

	@Override
	protected boolean useIMDatabase() {
		return true;
	}

	@Override
	public String createTableSql() {
		return "CREATE TABLE " + DBColumns.RecentChatDB.TABLENAME + " (" +
				DBColumns.RecentChatDB.COLUMN_ID + " TEXT PRIMARY KEY, " +
				DBColumns.RecentChatDB.COLUMN_NAME + " TEXT, " +
				DBColumns.RecentChatDB.COLUMN_CONTENT + " TEXT, " +
				DBColumns.RecentChatDB.COLUMN_LOCAL_AVATAR + " INTEGER, " +
				DBColumns.RecentChatDB.COLUMN_ACTIVITY_TYPE + " INTEGER, " +
				DBColumns.RecentChatDB.COLUMN_UNREADCOUNT + " INTEGER, " +
				DBColumns.RecentChatDB.COLUMN_EXTRAOBJ + " BLOB, " +
				DBColumns.RecentChatDB.COLUMN_UPDATETIME + " INTEGER);";
	}
}
