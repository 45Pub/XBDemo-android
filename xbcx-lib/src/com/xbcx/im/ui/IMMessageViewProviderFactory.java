package com.xbcx.im.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.xbcx.im.ui.IMMessageViewProvider.OnViewClickListener;
import com.xbcx.im.ui.messageviewprovider.FileViewLeftProvider;
import com.xbcx.im.ui.messageviewprovider.FileViewRightProvider;
import com.xbcx.im.ui.messageviewprovider.LocationMessageLeftProvider;
import com.xbcx.im.ui.messageviewprovider.LocationMessageRightProvider;
import com.xbcx.im.ui.messageviewprovider.PhotoViewLeftProvider;
import com.xbcx.im.ui.messageviewprovider.PhotoViewRightProvider;
import com.xbcx.im.ui.messageviewprovider.PromptViewProvider;
import com.xbcx.im.ui.messageviewprovider.TextViewLeftProvider;
import com.xbcx.im.ui.messageviewprovider.TextViewRightProvider;
import com.xbcx.im.ui.messageviewprovider.TimeViewProvider;
import com.xbcx.im.ui.messageviewprovider.VideoViewLeftProvider;
import com.xbcx.im.ui.messageviewprovider.VideoViewRightProvider;
import com.xbcx.im.ui.messageviewprovider.VoiceViewLeftProvider;
import com.xbcx.im.ui.messageviewprovider.VoiceViewRightProvider;

public class IMMessageViewProviderFactory {
	
	public List<IMMessageViewProvider> createIMMessageViewProviders(
			Context context,OnViewClickListener listener){
		List<IMMessageViewProvider> providers = new ArrayList<IMMessageViewProvider>();
		providers.add(new TextViewLeftProvider(listener));
		providers.add(new TextViewRightProvider(listener));
		providers.add(new TimeViewProvider());
		providers.add(new VoiceViewLeftProvider(context,listener));
		providers.add(new VoiceViewRightProvider(context,listener));
		providers.add(new PhotoViewLeftProvider(listener));
		providers.add(new PhotoViewRightProvider(listener));
		providers.add(new VideoViewLeftProvider(listener));
		providers.add(new VideoViewRightProvider(listener));
		providers.add(new FileViewLeftProvider(listener));
		providers.add(new FileViewRightProvider(listener));
		providers.add(new LocationMessageLeftProvider(listener));
		providers.add(new LocationMessageRightProvider(listener));
		providers.add(new PromptViewProvider());
		return providers;
	}
}
