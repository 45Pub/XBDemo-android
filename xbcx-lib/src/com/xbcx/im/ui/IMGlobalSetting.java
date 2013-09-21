package com.xbcx.im.ui;

import java.util.ArrayList;
import java.util.List;

import android.util.SparseArray;

import com.xbcx.im.MessageFactoryImpl;
import com.xbcx.im.XMessageFactory;
import com.xbcx.im.ui.messageviewprovider.CommonViewProviderDelegate;
import com.xbcx.im.ui.simpleimpl.CameraActivity;

public class IMGlobalSetting {
	
	public static boolean					photoCrop 						= false;
	
	public static boolean					photoSendPreview 				= false;
	
	@SuppressWarnings("rawtypes")
	public static Class						photoSendPreviewActivityClass 	= null;
	
	@SuppressWarnings("rawtypes")
	public static Class						videoCaptureActivityClass		= CameraActivity.class;
	
	public static SparseArray<SendPlugin> 	mapEditViewBtnIdToSendPlugin 	= new SparseArray<SendPlugin>();
	
	public static XMessageFactory			msgFactory						= new MessageFactoryImpl();
	
	public static List<Class<? extends EditViewExpressionProvider>> editViewExpProviders 	
								= new ArrayList<Class<? extends EditViewExpressionProvider>>();
	
	public static CommonViewProviderDelegate		msgViewProviderDelegate;
	
	public static List<TextMessageSpanner> textMsgImageCodeces			= new ArrayList<TextMessageSpanner>();
}
