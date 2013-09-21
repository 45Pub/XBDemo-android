package com.xbcx.im.db;

import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xbcx.core.Event;
import com.xbcx.im.XMessage;
import com.xbcx.im.ui.IMGlobalSetting;

public class ReadMessageRunner extends MessageBaseRunner {

	@Override
	public void onEventRun(Event event) throws Exception {
		requestExecute(true, event);
	}

	@Override
	protected void onExecute(SQLiteDatabase db, Event event) {
		final DBReadMessageParam param = (DBReadMessageParam)event.getParamAtIndex(0);
		final String id = param.mId;
		final int fromType = param.mFromType;
		final int nReadPosition = param.mReadPosition;
		int nReadCount = param.mReadCount;
		final List<XMessage> messages = param.mMessages;
		
		int nStartPosition = nReadPosition - nReadCount + 1;
		if(nStartPosition < 0){
			nReadCount = nReadPosition + 1;
			nStartPosition = 0;
		}
		
		Cursor cursor = null;
		cursor = db.query(getTableName(id), null, null, null, null, null,
				DBColumns.Message.COLUMN_AUTOID + " ASC", 
				nStartPosition + "," + nReadCount);
		managerCursor(cursor);
		if (cursor != null && cursor.moveToFirst()) {
			do{
				XMessage m = IMGlobalSetting.msgFactory.createXMessage(cursor);
				m.setFromType(fromType);
				if(fromType != XMessage.FROMTYPE_SINGLE){
					m.setGroupId(id);
				}
				messages.add(m);
			}while(cursor.moveToNext());
		}
	}

}
