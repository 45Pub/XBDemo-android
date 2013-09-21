package com.xbcx.im.ui.messageviewprovider;

import com.xbcx.im.XMessage;

public class LocationMessageRightProvider extends LocationMessageLeftProvider {

	public LocationMessageRightProvider(OnViewClickListener listener) {
		super(listener);
	}

	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.isFromSelf()){
			return message.getType() == XMessage.TYPE_LOCATION;
		}
		return false;
	}
}
