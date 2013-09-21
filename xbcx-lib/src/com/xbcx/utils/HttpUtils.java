package com.xbcx.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

import android.os.Handler;

public class HttpUtils {
	
	private static final String BOUNDARY = "1234567890abcd";
	
	private static final int TIMEOUT_CONNECTION = 8000;
	private static final int TIMEOUT_SO = 30000;
	
	public static InputStream doGetInputStream(String strUrl){
		HttpResponse response = doConnection(strUrl);
		if(isResponseAvailable(response)){
			try {
				return response.getEntity().getContent();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static String doGetString(String strUrl){
		String strResult = null;
		HttpResponse response = doConnection(strUrl);
		if(isResponseAvailable(response)){
			try {
				strResult = EntityUtils.toString(response.getEntity());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return strResult;
	}
	
	public static String doGetString(String strUrl,String defaultCharset){
		String strResult = null;
		HttpResponse response = doConnection(strUrl);
		if(isResponseAvailable(response)){
			try {
				strResult = EntityUtils.toString(response.getEntity(),defaultCharset);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return strResult;
	}
	
	public static String doPost(String strUrl,Map<String, String> mapToNameValue){
		return doPost(strUrl, mapToNameValue, null);
	}
	
	public static String doPost(String strUrl,
			Map<String, String> mapToNameValue,Map<String, String> mapKeyToFilePath){
		return doPost(strUrl, mapToNameValue, mapKeyToFilePath, null, null,null);
	}
	
	public static String doPost(String strUrl,String filePath){
		Map<String, String> map = new HashMap<String, String>();
		map.put("upfile", filePath);
		return doPost(strUrl, null, map);
	}
	
	public static String doPost(String strUrl,String filePath,
			ProgressRunnable pr,Handler handler,AtomicBoolean cancel){
		Map<String, String> map = new HashMap<String, String>();
		map.put("upfile", filePath);
		return doPost(strUrl, null, map,pr,handler,cancel);
	}
	
	public static String doPost(String strUrl,
			Map<String, String> mapToNameValue,Map<String, String> mapKeyToFilePath,
			ProgressRunnable pr,Handler handler,AtomicBoolean cancel){
		String strResult = null;
		HttpResponse response = null;
		try {
			final URI uri = new URI(strUrl);
			HttpPost httpPost = new HttpPost(uri);
			httpPost.addHeader("charset", HTTP.UTF_8);
			
			MultipartEntityEx entity = new MultipartEntityEx(HttpMultipartMode.BROWSER_COMPATIBLE,
					pr,handler,cancel);
			if(mapToNameValue != null){
				for(String str : mapToNameValue.keySet()){
					final String value = mapToNameValue.get(str);
					entity.addPart(str, 
							new StringBody(value == null ? "" : value,
									Charset.forName("UTF-8")));
				}
			}
			
			if(mapKeyToFilePath != null){
				for(String strKey : mapKeyToFilePath.keySet()){
					entity.addPart(strKey, new FileBody(new File(mapKeyToFilePath.get(strKey))));
				}
			}
			
			entity.mTotalSize = entity.getContentLength();
			
			httpPost.setEntity(entity);
	        HttpClient httpClient = new DefaultHttpClient();
	        HttpParams params = httpClient.getParams();
	        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_CONNECTION);
	        HttpConnectionParams.setSoTimeout(params, TIMEOUT_SO);
	       
	        response = httpClient.execute(httpPost);
	        
	        if(isResponseAvailable(response)){
	        	strResult = EntityUtils.toString(response.getEntity());
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strResult;
	}
	
	
	public static String doUpload(String strUrl,String strFilePath,
			ProgressRunnable progress,Handler handler){
		InputStream is = null;
		OutputStream os = null;
		HttpURLConnection conn = null;
		try {
			URL url = new URL(strUrl);
			conn = (HttpURLConnection) url.openConnection();
			
			File file = new File(strFilePath);
			String strFilename = file.getName();
			
			StringBuffer sbPrefix = new StringBuffer();
			sbPrefix.append("\r\n")
			.append("--" + BOUNDARY + "\r\n")
			.append("Content-Disposition: form-data; name=\"pic_file\"; filename=\"" + strFilename + "\"\r\n")
			.append("Content-Type: " + "application/octet-stream" + "\r\n")
			.append("\r\n");
			
			StringBuffer sbSuffix = new StringBuffer();
			sbSuffix.append("\r\n--" + BOUNDARY + "--\r\n");
			
			byte bytePrefix[] = sbPrefix.toString().getBytes("UTF-8");
			byte byteSuffix[] = sbSuffix.toString().getBytes("UTF-8");
			
			final long lContentLength = bytePrefix.length + file.length() + byteSuffix.length;
			
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type","multipart/form-data; boundary=" + BOUNDARY);
			conn.setRequestProperty("Content-Length",String.valueOf(lContentLength));
			//conn.setConnectTimeout(TIMEOUT_CONNECTION);
			//conn.setReadTimeout(TIMEOUT_SO);
			// conn.setRequestProperty("HOST", "192.168.1.16:8080");
			conn.setDoOutput(true);
			
			os = conn.getOutputStream();
			is = new FileInputStream(file);
			
			os.write(bytePrefix);

			byte[] buf = new byte[1024];
			int nReadBytes = 0;
			
			if(progress == null){
				while ((nReadBytes = is.read(buf)) != -1) {
					os.write(buf, 0, nReadBytes);
				}
				
				os.write(byteSuffix);
			}else{
				long lUploadBytes = bytePrefix.length;
				while ((nReadBytes = is.read(buf)) != -1) {
					os.write(buf, 0, nReadBytes);
					lUploadBytes += nReadBytes;
					
					progress.mPercentage = (int)(lUploadBytes * 100 / lContentLength);
					handler.post(progress);
				}
				
				os.write(byteSuffix);
				
				progress.mPercentage = 100;
				handler.post(progress);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			conn = null;
		} finally {
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					conn = null;
					e.printStackTrace();
				}
			}
			if(os != null){
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
					conn = null;
				}
			}
		}
		
		String strRet = null;
		
		if(conn != null){
			try {
				InputStream isResponse = (InputStream)conn.getContent();
				if(isResponse != null){
					int nRead = 0;
					byte buf[] = new byte[128];
					CharArrayBuffer bab = new CharArrayBuffer(128);
					while((nRead = isResponse.read(buf)) != -1){
						bab.append(buf, 0, nRead);
					}
					strRet = bab.substring(0, bab.length());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	    return strRet;
	}
	
	public static String doUpload(String strUrl,String strFilePath){
		return doUpload(strUrl, strFilePath, null,null);
	}
	
	public static boolean doDownload(String strUrl,String strSavePath,
			ProgressRunnable progress,Handler handler,AtomicBoolean bCancel){
		HttpResponse response = doConnection(strUrl);
		if(isResponseAvailable(response)){
			InputStream is = null;
			FileOutputStream fos = null;
			try {
				is = response.getEntity().getContent();
				fos = FileHelper.createFileOutputStream(strSavePath);
				if(fos != null){
					final byte buf[] = new byte[1024];
					if(progress == null){
						int lReadLength = 0;
						while((lReadLength = is.read(buf)) != -1){
							fos.write(buf, 0, lReadLength);
						}
					}else{
						long lDownloadLength = 0;
						int lReadLength = 0;
						final long lTotalLength = response.getEntity().getContentLength();
						while(true){
							if(bCancel != null && bCancel.get()){
								File file = new File(strSavePath);
								file.delete();
								return false;
							}else if((lReadLength = is.read(buf)) != -1){
								fos.write(buf, 0, lReadLength);
								lDownloadLength += lReadLength;
								progress.mPercentage = (int)(lDownloadLength * 100 / lTotalLength);
								handler.post(progress);
							}else{
								break;
							}
						}
					}
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				FileHelper.deleteFile(strSavePath);
			} finally{
				try{
					if(is != null){
						is.close();
					}
					if(fos != null){
						fos.close();
					}
				}catch(Exception e){
				}
			}
		}
		return false;
	}
	
	public static boolean doDownload(String strUrl,String strSavePath,boolean bUseTemp,
			ProgressRunnable progress,Handler handler,AtomicBoolean bCancel){
		HttpResponse response = doConnection(strUrl);
		if(isResponseAvailable(response)){
			InputStream is = null;
			boolean bSuccess = false;
			FileOutputStream fos = null;
			final String path = bUseTemp ? strSavePath + ".temp" : strSavePath;
			try {
				is = response.getEntity().getContent();
				fos = FileHelper.createFileOutputStream(path);
				if(fos != null){
					final byte buf[] = new byte[1024];
					if(progress == null){
						int lReadLength = 0;
						while((lReadLength = is.read(buf)) != -1){
							fos.write(buf, 0, lReadLength);
						}
					}else{
						long lDownloadLength = 0;
						int lReadLength = 0;
						final long lTotalLength = response.getEntity().getContentLength();
						while(true){
							if(bCancel != null && bCancel.get()){
								File file = new File(path);
								file.delete();
								return false;
							}else if((lReadLength = is.read(buf)) != -1){
								fos.write(buf, 0, lReadLength);
								lDownloadLength += lReadLength;
								progress.mPercentage = (int)(lDownloadLength * 100 / lTotalLength);
								handler.post(progress);
							}else{
								break;
							}
						}
					}
					bSuccess = true;
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				FileHelper.deleteFile(path);
			} finally{
				try{
					if(is != null){
						is.close();
					}
					if(fos != null){
						fos.close();
					}
					if(bSuccess){
						if(bUseTemp){
							File file = new File(path);
							File fileDst = new File(strSavePath);
							file.renameTo(fileDst);
						}
					}
				}catch(Exception e){
				}
			}
		}
		return false;
	}
	
	private static boolean	isResponseAvailable(HttpResponse response){
		if(response != null && 
				response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
			return true;
		}
		return false;
	}
	
	private static HttpResponse doConnection(String strUrl){
		HttpResponse response = null;
		try {
			final URI uri = new URI(strUrl);
			HttpGet httpGet = new HttpGet(uri);
	        HttpClient httpClient = new DefaultHttpClient();
	        HttpParams params = httpClient.getParams();
	        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_CONNECTION);
	        HttpConnectionParams.setSoTimeout(params, TIMEOUT_SO);
	       
	        response = httpClient.execute(httpGet);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			
		}
		
		return response;
	}
	
	private static class MultipartEntityEx extends MultipartEntity{
		
		public long mTransferredSize;
		
		public long mTotalSize;
		
		public ProgressRunnable mRunnable;
		public Handler			mHandler;
		public AtomicBoolean	mCancel;
		
		public MultipartEntityEx(HttpMultipartMode mode,
				ProgressRunnable run,Handler handler,AtomicBoolean cancel){
			super(mode);
			mRunnable = run;
			mHandler = handler;
			mCancel = cancel;
		}

		@Override
		public void writeTo(OutputStream outstream) throws IOException {
			super.writeTo(new CustomOutputStream(outstream));
		}
		
		private class CustomOutputStream extends FilterOutputStream{

			public CustomOutputStream(OutputStream out) {
				super(out);
			}

			@Override
			public void write(byte[] buffer, int offset, int length) throws IOException {
				if(mCancel != null && mCancel.get()){
					throw new IOException();
				}
				out.write(buffer, offset, length);
				//super.write(buffer, offset, length);
				mTransferredSize += length;
				notifyProgress();
			}

			@Override
			public void write(int oneByte) throws IOException {
				if(mCancel != null && mCancel.get()){
					throw new IOException();
				}
				super.write(oneByte);
				++mTransferredSize;
				notifyProgress();
			}
			
			protected void notifyProgress(){
				if(mHandler != null && mRunnable != null){
					final int nPer = (int)(mTransferredSize * 100 / mTotalSize);
					if(mRunnable.mPercentage != nPer){
						mRunnable.mPercentage = nPer;
						mHandler.post(mRunnable);
					}
				}
			}
		}
	}
	
	public 	static abstract class ProgressRunnable implements Runnable{
		private int mPercentage = -1;
		
		public int getPercentage(){
			return mPercentage;
		}
	}
}
