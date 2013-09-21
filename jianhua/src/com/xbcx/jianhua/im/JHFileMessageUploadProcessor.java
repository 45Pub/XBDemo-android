package com.xbcx.jianhua.im;

import com.xbcx.im.XMessage;
import com.xbcx.im.messageprocessor.FileMessageUploadProcessor;

public class JHFileMessageUploadProcessor extends FileMessageUploadProcessor {

	@Override
	protected String getUploadType(XMessage xm) {
		return "7";
	}

}
