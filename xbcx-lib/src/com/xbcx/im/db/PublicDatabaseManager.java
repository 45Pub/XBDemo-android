package com.xbcx.im.db;


import com.xbcx.im.IMKernel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PublicDatabaseManager extends DatabaseManager{
	
	public 	static PublicDatabaseManager getInstance(){
		return sInstance;
	}
	
	private static PublicDatabaseManager sInstance;
	
	private PublicDatabaseManager() {
		sInstance = this;
	}

	@Override
	protected void onInitial(IMKernel kernel) {
		mDBHelper = new DBHelper(kernel.getContext());
	}

	@Override
	protected void onRelease() {
		mDBHelper.close();
	}
	
	public String 	getDatabaseName(){
		return DBHelper.DB_NAME;
	}

	private static class DBHelper extends SQLiteOpenHelper{
		
		private static final int DB_VERSION = 1;
		private static final String DB_NAME = "public";

		public DBHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}
}
