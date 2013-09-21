package com.xbcx.im.db;

import com.xbcx.core.Event;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class ReadMessageCountRunner extends MessageBaseRunner {

	private DBReadMessageCountParam mParam;
	
	@Override
	public void onEventRun(Event event) throws Exception {
		mParam = (DBReadMessageCountParam)event.getParamAtIndex(0);
		requestExecute(true,event);
	}

	@Override
	protected void onExecute(SQLiteDatabase db,Event event) {
		String strTableName = getTableName(mParam.mId);
		
		if(!TextUtils.isEmpty(strTableName)){
			Cursor cursor = db.query(strTableName, new String[]{"count(*)"}, 
					null, null, null, null, null);
			managerCursor(cursor);
			if(cursor != null && cursor.moveToFirst()){
				mParam.mReturnCount = cursor.getInt(0);
			}
		}else{
			throw new IllegalArgumentException("unknow tablename");
		}
	}

}
