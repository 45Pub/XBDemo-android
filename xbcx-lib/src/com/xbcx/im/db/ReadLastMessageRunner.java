package com.xbcx.im.db;

import com.xbcx.core.Event;
import com.xbcx.im.IMMessage;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ReadLastMessageRunner extends MessageBaseRunner {
	
	private DBReadLastMessageParam mParam;
	
	public ReadLastMessageRunner() {
	}

	@Override
	public void onEventRun(Event event) throws Exception {
		mParam = (DBReadLastMessageParam)event.getParamAtIndex(0);
		requestExecute(true,event);
	}
	
	@Override
	public String createTableSql() {
		return null;
	}

	@Override
	protected void onExecute(SQLiteDatabase db,Event event) {
		doQuery(db, getTableName(mParam.mId));
	}
	
	protected void doQuery(SQLiteDatabase db,String strTableName){
		mParam.mHasValue = false;
		Cursor cursor = db.query(strTableName,
				mParam.mColumnNames,
				null, null, null, null, DBColumns.Message.COLUMN_AUTOID + " DESC",
				"0,1");
		managerCursor(cursor);
		if(cursor != null && cursor.moveToFirst()){
			mParam.mHasValue = true;
			if(mParam.mSetMessage && mParam.mColumnNames == null){
				mParam.mMessageOut = new IMMessage(cursor);
			}else{
				final int nCount = cursor.getColumnCount();
				for(int nIndex = 0;nIndex < nCount;++nIndex){
					mParam.mMapColumnNameToValue.put(cursor.getColumnName(nIndex),
							cursor.getString(nIndex));
				}
			}
		}
	}

}
