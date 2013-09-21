package com.xbcx.im.db;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xbcx.core.Event;

public class ReadCommonUseMsgRunner extends SaveCommonUseMsgRunner {

	@Override
	public void onEventRun(Event event) throws Exception {
		requestExecute(true, event);
	}

	@Override
	protected void onExecute(SQLiteDatabase db, Event event) {
		Cursor cursor = db.query(DBColumns.CommonUseMsg.TABLENAME, 
				null, null, null, null, null, null);
		if(cursor != null && cursor.moveToFirst()){
			List<String> msgs = new ArrayList<String>();
			do{
				msgs.add(cursor.getString(
						cursor.getColumnIndex(DBColumns.CommonUseMsg.COLUMN_CONTENT)));
			}while(cursor.moveToNext());
			event.addReturnParam(msgs);
			event.setSuccess(true);
		}
	}
}
