package com.xbcx.core;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.utils.SystemUtils;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;

public class XApplication extends Application implements OnEventListener{
	
	public static String URL_GetLocationImage = "http://maps.google.com/maps/api/staticmap?" +
			"center=%f,%f&" +
			"zoom=%d&" +
			"size=%dx%d&" +
			"maptype=roadmap&" +
			"markers=color:red%%7Clabel:%%7C30.634107,103.977148&" +
			"sensor=false&language=zh-Hans";
	
	public static XApplication getApplication(){
		return sInstance;
	}
	
	private static XApplication sInstance;
	
	private static Logger 	sLogger;
	
	private static Handler	sMainThreadHandler;
	
	private static int		sScreenWidth;
	private static int		sScreenHeight;
	private static int 		sScreenDpi;
	
	private static HashMap<String, SoftReference<Bitmap>> 		mapUrlToBitmap 		= new HashMap<String, SoftReference<Bitmap>>();
	private static HashMap<String, String> 						mapDownloadingUrl 	= new HashMap<String, String>();
	private static HashMap<String, UrlBitmapDownloadCallback> 	mapUrlToCallback	= new HashMap<String, UrlBitmapDownloadCallback>();

	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
		
		CrashHandler.getInstance().init(this);
		
		sMainThreadHandler = new Handler();
		
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		sScreenWidth = dm.widthPixels;
		sScreenHeight = dm.heightPixels;
		sScreenDpi = dm.densityDpi;
		
		AndroidEventManager.getInstance().registerEventRunner(EventCode.HTTP_DownloadBitmap, new HttpDownloadRunner());
	}
	
	public static Logger getLogger(){
		if(sLogger == null){
			sLogger = Logger.getLogger(sInstance.getPackageName());
			sLogger.setLevel(Level.ALL);
			LoggerSystemOutHandler handler = new LoggerSystemOutHandler();
			handler.setLevel(Level.ALL);
			sLogger.addHandler(handler);
		}
		return sLogger;
	}
	
	public static Handler getMainThreadHandler(){
		return sMainThreadHandler;
	}
	
	public static int	getScreenWidth(){
		return sScreenWidth;
	}
	
	public static int	getScreenHeight(){
		return sScreenHeight;
	}
	
	public static int 	getScreenDpi(){
		return sScreenDpi;
	}

	public static boolean checkExternalStorageAvailable(){
		boolean bAvailable = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
		if(bAvailable){
			StatFs statfs = new StatFs(Environment.getExternalStorageDirectory().getPath());
			if((long)statfs.getAvailableBlocks() * (long)statfs.getBlockSize() < 1048576){
				ToastManager.getInstance(sInstance).show(ResIds.string.prompt_sdcard_full);
				bAvailable = false;
			}
		}else{
			ToastManager.getInstance(sInstance).show(ResIds.string.prompt_sdcard_unavailable);
		}
		return bAvailable;
	}
	
	public static void	setBitmap(ImageView iv,String url,int defaultResId,
			UrlBitmapDownloadCallback callback){
		final Bitmap bmp = loadBitmap(url, callback);
		if(bmp == null){
			iv.setImageResource(defaultResId);
		}else{
			iv.setImageBitmap(bmp);
		}
	}
	
	public static void	setBitmapEx(final ImageView iv,String url,int defaultResId){
		final Bitmap bmp = loadBitmap(url, new UrlBitmapDownloadCallback() {
			@Override
			public void onBitmapDownloadSuccess(String url) {
				iv.setImageBitmap(loadBitmap(url, null));
			}
		});
		if(bmp == null){
			if(defaultResId != 0){
				iv.setImageResource(defaultResId);
			}
		}else{
			iv.setImageBitmap(bmp);
		}
	}
	
	public static Bitmap loadBitmap(String url,UrlBitmapDownloadCallback callback){
		if(TextUtils.isEmpty(url)){
			return null;
		}
		SoftReference<Bitmap> sf = mapUrlToBitmap.get(url);
		Bitmap bmp = null;
		if(sf != null){
			bmp = sf.get();
		}
		if(bmp == null){
			String filePath = FilePaths.getUrlFileCachePath(url);
			if(new File(filePath).exists()){
				BitmapFactory.Options op = new BitmapFactory.Options();
				SystemUtils.computeSampleSize(op, filePath, 1024, 1024 * 512);
				try{
					bmp = BitmapFactory.decodeFile(filePath,op);
				}catch(OutOfMemoryError e){
					e.printStackTrace();
					op.inSampleSize *= 2;
					bmp = BitmapFactory.decodeFile(filePath, op);
				}
			}
			if(bmp == null){
				if(callback != null){
					mapUrlToCallback.put(url, callback);
				}
				requestDownloadBitmap(url, filePath);
			}else if(bmp != null){
				mapUrlToBitmap.put(url, new SoftReference<Bitmap>(bmp));
			}
		}
		return bmp;
	}
	
	protected static void requestDownloadBitmap(String url,String path){
		if(!mapDownloadingUrl.containsKey(url)){
			mapDownloadingUrl.put(url, url);
			AndroidEventManager.getInstance().pushEventEx(EventCode.HTTP_DownloadBitmap, 
					sInstance, url,path);
		}
	}

	@Override
	public void onEventRunEnd(Event event) {
		final int code = event.getEventCode();
		if(code == EventCode.HTTP_DownloadBitmap){
			final String url = (String)event.getParamAtIndex(0);
			mapDownloadingUrl.remove(url);
			if(event.isSuccess()){
				UrlBitmapDownloadCallback callback = mapUrlToCallback.remove(url);
				if(callback != null){
					callback.onBitmapDownloadSuccess(url);
				}
			}
		}
	}
}
