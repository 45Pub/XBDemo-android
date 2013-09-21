package com.xbcx.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.xbcx.library.R;

import android.os.Environment;

public class LoggerSystemOutHandler extends Handler {
	
	public static boolean DEBUG = true;

	private BufferedWriter mBw;
	
	@Override
	public void close() {
	}

	@Override
	public void flush() {
	}

	@Override
	public void publish(LogRecord record) {
//		System.out.println(record.getSourceClassName() + " " + 
//				record.getSourceMethodName() + ":" + 
//				record.getMessage());
		if(record.getLevel() == Level.WARNING){
			if(mBw == null){
				try {
					mBw = new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory().getPath() + 
							File.separator + 
							XApplication.getApplication().getString(R.string.app_name) + ".txt"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(mBw != null){
				try {
					mBw.newLine();
					mBw.write(record.getMessage());
					mBw.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		if(DEBUG){
			System.out.println(record.getMessage());
		}
	}

}
