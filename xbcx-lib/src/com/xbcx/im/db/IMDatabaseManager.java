package com.xbcx.im.db;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.EventCode;
import com.xbcx.im.IMKernel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class IMDatabaseManager extends DatabaseManager{
	
	public static IMDatabaseManager getInstance(){
		return sInstance;
	}
	
	private static IMDatabaseManager sInstance;
	
	protected IMDatabaseManager(){
		sInstance = this;
		AndroidEventManager.getInstance().registerEventRunner(
				EventCode.DB_SaveMessage, new MessageSaveRunner());
		AndroidEventManager.getInstance().registerEventRunner(
				EventCode.DB_ReadMessageCount, new ReadMessageCountRunner());
		AndroidEventManager.getInstance().registerEventRunner(
				EventCode.DB_DeleteMessage, new DeleteMessageRunner());
		AndroidEventManager.getInstance().registerEventRunner(
				EventCode.DB_ReadLastMessage, new ReadLastMessageRunner());
		AndroidEventManager.getInstance().registerEventRunner(
				EventCode.DB_ReadMessage, new ReadMessageRunner());
		AndroidEventManager.getInstance().registerEventRunner(
				EventCode.DB_ReadRecentChat, new ReadRecentChatRunner());
		AndroidEventManager.getInstance().registerEventRunner(
				EventCode.DB_SaveRecentChat, new SaveRecentChatRunner());
		AndroidEventManager.getInstance().registerEventRunner(
				EventCode.DB_DeleteRecentChat, new DeleteRecentChatRunner());
		AndroidEventManager.getInstance().registerEventRunner(
				EventCode.DB_SaveCommonUseMsg, new SaveCommonUseMsgRunner());
		AndroidEventManager.getInstance().registerEventRunner(
				EventCode.DB_ReadCommonUseMsg, new ReadCommonUseMsgRunner());
		AndroidEventManager.getInstance().registerEventRunner(
				EventCode.DB_DeleteCommonUseMsg, new DeleteCommonUseMsgRunner());
	}
	
	@Override
	protected void onInitial(IMKernel kernel) {
		mDBHelper = new DBUserHelper(kernel.getContext(), kernel.getUserId());
	}

	@Override
	protected void onRelease() {
		if(mDBHelper != null){
			mDBHelper.close();
		}
	}
	
	private static class DBUserHelper extends SQLiteOpenHelper{

		private static final int DB_VERSION = 1;
		
		public DBUserHelper(Context context, String name) {
			super(context, name, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}
}
