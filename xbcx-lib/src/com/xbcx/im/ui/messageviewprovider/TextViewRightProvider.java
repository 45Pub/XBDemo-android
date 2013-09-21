package com.xbcx.im.ui.messageviewprovider;

import com.xbcx.im.XMessage;

public class TextViewRightProvider extends TextViewLeftProvider {

	public TextViewRightProvider(OnViewClickListener listener) {
		super(listener);
	}

	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.getType() == XMessage.TYPE_TEXT){
			XMessage hm = (XMessage)message;
			return hm.isFromSelf();
		}
		return false;
	}
}
