package com.xbcx.im.db;

import android.database.sqlite.SQLiteDatabase;

import com.xbcx.core.Event;

public class DeleteCommonUseMsgRunner extends SaveCommonUseMsgRunner {

	@Override
	public void onEventRun(Event event) throws Exception {
		requestExecute(false, event);
	}

	@Override
	protected void onExecute(SQLiteDatabase db, Event event) {
		final String msg = (String)event.getParamAtIndex(0);
		db.delete(DBColumns.CommonUseMsg.TABLENAME,
				DBColumns.CommonUseMsg.COLUMN_CONTENT + "='" + msg + "'", null);
	}

}
