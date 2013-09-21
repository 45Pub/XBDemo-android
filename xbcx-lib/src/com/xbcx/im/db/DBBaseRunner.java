package com.xbcx.im.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xbcx.core.Event;
import com.xbcx.core.EventManager.OnEventRunner;


public abstract class DBBaseRunner implements OnEventRunner {

	protected List<Cursor> mListCursor;
	
	protected boolean mIsRead = true;
	
	protected void	managerCursor(Cursor cursor){
		if(cursor == null){
			return;
		}
		
		if(mListCursor == null){
			mListCursor = new ArrayList<Cursor>();
		}
		mListCursor.add(cursor);
	}
	
	protected void	requestExecute(boolean bRead,Event event){
		mIsRead = bRead;
		DatabaseManager dm = useIMDatabase() ? IMDatabaseManager.getInstance() :
												PublicDatabaseManager.getInstance();
		if(bRead){
			SQLiteDatabase db = dm.lockReadableDatabase();
			try{
				onExecute(db,event);
			}finally{
				dm.unlockReadableDatabase(db);
				closeCursor();
			}
		}else{
			SQLiteDatabase db = dm.lockWritableDatabase();
			try{
				onExecute(db,event);
			}finally{
				dm.unlockWritableDatabase(db);
				closeCursor();
			}
		}
	}
	
	protected void	safeInsert(SQLiteDatabase db,String strTableName,ContentValues cv){
		long lRet = db.insert(strTableName, null, cv);
		if(lRet == -1){
			if(!tabbleIsExist(strTableName, db)){
				db.execSQL(createTableSql());
				db.insert(strTableName, null, cv);
			}
		}
	}
	
	protected void 	safeUpdate(SQLiteDatabase db,String tableName,ContentValues cv,String whereColumn,String whereColumnValue){
		try{
			int ret = db.update(tableName, cv, 
					whereColumn + "='" + whereColumnValue + "'",null);
			if(ret <= 0){
				cv.put(whereColumn, whereColumnValue);
				safeInsert(db, tableName, cv);
			}
		}catch(Exception e){
			if(!tabbleIsExist(tableName, db)){
				db.execSQL(createTableSql());
				cv.put(whereColumn, whereColumnValue);
				db.insert(tableName, null, cv);
			}
		}
	}
	
	protected abstract String createTableSql();
	
	protected boolean tabbleIsExist(String tableName,SQLiteDatabase db) {
		boolean result = false;
		Cursor cursor = null;
		try {
			String sql = "select count(*) as c from Sqlite_master  where type ='table' and name ='" + tableName.trim() + "' ";
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					result = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(cursor != null){
				cursor.close();
			}
		}
		return result;
	}
	
	protected void	closeCursor(){
		if(mListCursor != null){
			for(Cursor cursor : mListCursor){
				cursor.close();
			}
			mListCursor.clear();
		}
	}
	
	
	protected boolean useIMDatabase(){
		return false;
	}
	
	protected abstract void 	onExecute(SQLiteDatabase db,Event event);
}
