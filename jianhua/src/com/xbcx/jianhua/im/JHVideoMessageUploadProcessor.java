package com.xbcx.jianhua.im;

import com.xbcx.im.XMessage;
import com.xbcx.im.messageprocessor.VideoMessageUploadProcessor;

public class JHVideoMessageUploadProcessor extends VideoMessageUploadProcessor {

	@Override
	protected String getUploadType(XMessage xm) {
		return xm.isFromGroup() ? "5" : "2";
	}

	@Override
	protected String getUploadThumbType(XMessage xm) {
		return xm.isFromGroup() ? "5" : "2";
	}
}
