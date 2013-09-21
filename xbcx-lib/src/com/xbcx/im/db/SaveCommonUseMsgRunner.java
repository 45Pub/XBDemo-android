package com.xbcx.im.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.xbcx.core.Event;

public class SaveCommonUseMsgRunner extends DBBaseRunner {

	@Override
	public void onEventRun(Event event) throws Exception {
		requestExecute(false, event);
	}

	@Override
	protected String createTableSql() {
		return "CREATE TABLE " + DBColumns.CommonUseMsg.TABLENAME + " (" +
				DBColumns.CommonUseMsg.COLUMN_CONTENT + " TEXT PRIMARY KEY);";
	}

	@Override
	protected void onExecute(SQLiteDatabase db, Event event) {
		final String msg = (String)event.getParamAtIndex(0);
		ContentValues cv = new ContentValues();
		cv.put(DBColumns.CommonUseMsg.COLUMN_CONTENT, msg);
		safeInsert(db, DBColumns.CommonUseMsg.TABLENAME, cv);
	}

	@Override
	protected boolean useIMDatabase() {
		return true;
	}
}
