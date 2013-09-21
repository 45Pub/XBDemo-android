package com.xbcx.jianhua.im;

import com.xbcx.im.XMessage;
import com.xbcx.im.messageprocessor.PhotoMessageUploadProcessor;

public class JHPhotoMessageUploadProcessor extends PhotoMessageUploadProcessor {

	@Override
	protected String getUploadType(XMessage xm) {
		return xm.isFromGroup() ? "4" : "1";
	}
}
