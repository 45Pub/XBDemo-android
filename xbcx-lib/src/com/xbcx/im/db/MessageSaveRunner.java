package com.xbcx.im.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.xbcx.core.Event;
import com.xbcx.im.IMMessage;

public class MessageSaveRunner extends MessageBaseRunner {
	
	@Override
	public void onEventRun(Event event) throws Exception {
		requestExecute(false,event);
	}

	@Override
	protected String createTableSql() {
		return null;
	}

	@Override
	protected void onExecute(SQLiteDatabase db,Event event) {
		final IMMessage m = (IMMessage)event.getParamAtIndex(0);
		final String strTableName = getTableName(m.getOtherSideId());
		if (!TextUtils.isEmpty(strTableName)) {
			if (m.isStoraged()) {
				ContentValues cv = m.getSaveContentValues();
				if (cv.size() > 0) {
					db.update(strTableName, 
							cv,
							DBColumns.Message.COLUMN_ID + "='" + m.getId() + "'", null);
				}
			} else {
				final ContentValues cv = m.getSaveContentValues();
				long lRet = db.insert(strTableName, null, cv);
				if (lRet == -1) {
					if (!tabbleIsExist(strTableName, db)) {
						db.execSQL(createTableSql(strTableName));
						db.insert(strTableName, null, cv);
					}
				}
			}
			m.setStoraged();
		}
	}
}
