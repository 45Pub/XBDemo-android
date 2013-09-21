package com.xbcx.im.messageprocessor;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.EventCode;
import com.xbcx.core.MediaRecordManager;
import com.xbcx.core.SharedPreferenceDefine;
import com.xbcx.core.XApplication;
import com.xbcx.im.XMessage;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;

public class VoicePlayProcessor implements OnCompletionListener,
									OnErrorListener,
									OnInfoListener,
									MediaRecordManager.OnRecordListener{
	
	public static VoicePlayProcessor getInstance(){
		if(sInstance == null){
			sInstance = new VoicePlayProcessor();
		}
		return sInstance;
	}
	
	private static VoicePlayProcessor sInstance;
	
	private Context			mContext;
	
	private int				mInitTimes;
	
	private MediaPlayer 	mMediaPlayer;
	
	private AudioManager 	mAudioManager;
	
	private XMessage		mPlayingMessage;
	private XMessage 		mPausedMessage;
	private boolean			mPaused;
	
	private XMessage		mPlayPartMessage;
	
	private boolean			mIsSpearkOn;
	
	OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int focusChange) {
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
					focusChange == AudioManager.AUDIOFOCUS_LOSS) {
				pause();
			} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
				resume();
			}
		}
	};
	
	private Runnable mRunnablePlayPartToStop = new Runnable() {
		@Override
		public void run() {
			if(mPlayPartMessage != null && 
					mPlayPartMessage.equals(mPlayingMessage)){
				stop();
				
				mPlayPartMessage = null;
			}
		}
	};
	
	private VoicePlayProcessor(){
		mInitTimes = 0;
	}
	
	public void initial(Context context){
		if(mInitTimes == 0){
			mContext = context.getApplicationContext();
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setLooping(false);
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnErrorListener(this);
			mMediaPlayer.setOnInfoListener(this);
			
			mPaused = false;
		
			mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
			mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, 
					AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
			
			mIsSpearkOn = isSpeakerOn();
			
			MediaRecordManager.getInstance(context).addOnRecordListener(this);
		}
		++mInitTimes;
	}
	
	public void release(){
		--mInitTimes;
		if(mInitTimes == 0){
			mMediaPlayer.release();
			
			mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
			mAudioManager.setSpeakerphoneOn(true);
			
			mPlayingMessage = null;
			mPausedMessage = null;
			
			XApplication.getMainThreadHandler().removeCallbacks(mRunnablePlayPartToStop);
			
			MediaRecordManager.getInstance(mContext).removeOnRecordListener(this);
		}
	}
	
	public boolean isSpeakerOn(){
		final SharedPreferences sp = mContext.getSharedPreferences(SharedPreferenceDefine.SP_IM, 0);
		return sp.getBoolean(SharedPreferenceDefine.KEY_SPEAKERON, true);
	}
	
	public void	setSpeakerOn(boolean bOn){
		final SharedPreferences sp = mContext.getSharedPreferences(SharedPreferenceDefine.SP_IM, 0);
		sp.edit().putBoolean(SharedPreferenceDefine.KEY_SPEAKERON, bOn).commit();
		mIsSpearkOn = bOn;
		if(mInitTimes > 0){
			if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
				checkAndSetPlayMode();
			}
		}
	}
	
	protected void checkAndSetPlayMode(){
		if(!mIsSpearkOn){
			mAudioManager.setSpeakerphoneOn(false);
			mAudioManager.setMode(AudioManager.MODE_IN_CALL);
		}
	}
	
	protected void restorePlayMode(){
		mAudioManager.setSpeakerphoneOn(true);
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
	}
	
	public void play(XMessage m){
		play(m, false);
	}
	
	public void play(XMessage m,boolean bPlayPart){
		if(m.getType() == XMessage.TYPE_VOICE){
			try{
				checkAndSetPlayMode();
				
				mMediaPlayer.reset();
				mMediaPlayer.setDataSource(m.getVoiceFilePath());
				mMediaPlayer.prepare();
				mMediaPlayer.start();
				
				m.setPlayed(true);
				m.updateDB();
				
				mPlayingMessage = m;
				
				AndroidEventManager.getInstance().runEvent(EventCode.VoicePlayStarted, mPlayingMessage);
			}catch(Exception e){
				e.printStackTrace();
				AndroidEventManager.getInstance().runEvent(EventCode.VoicePlayErrored, m);
			}
		}
	}
	
	public void stop(){
		if(mPlayingMessage != null){
			mMediaPlayer.stop();
			
			restorePlayMode();
			
			AndroidEventManager.getInstance().runEvent(
					EventCode.VoicePlayStoped,mPlayingMessage);
			mPlayingMessage = null;
		}
	}
	
	public boolean isPlaying(XMessage m){
		return m != null && m.equals(mPlayingMessage);
	}
	
	private void resume(){
		if(mPaused){
			if(mPausedMessage != null){
				checkAndSetPlayMode();
				mPlayingMessage = mPausedMessage;
				mPausedMessage = null;
				mMediaPlayer.start();
				
				if(mPlayPartMessage != null && 
						mPlayPartMessage.equals(mPlayingMessage)){
					XApplication.getMainThreadHandler().postDelayed(
							mRunnablePlayPartToStop, 
							mPlayPartMessage.getVoiceMilliseconds() / 2 - mMediaPlayer.getCurrentPosition());
				}
			}
		}
	}
	
	private void pause(){
		restorePlayMode();
		mPaused = true;
		if(mPlayingMessage != null){
			mPausedMessage = mPlayingMessage;
			mPlayingMessage = null;
			mMediaPlayer.pause();
			if(mPlayPartMessage != null){
				XApplication.getMainThreadHandler().removeCallbacks(
						mRunnablePlayPartToStop);
			}
		}
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		return false;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		XApplication.getLogger().warning("play error what = " + what);
		
		restorePlayMode();
		
		AndroidEventManager.getInstance().runEvent(EventCode.VoicePlayErrored,mPlayingMessage);
		mPlayingMessage = null;
		mPausedMessage = null;
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		restorePlayMode();
		
		AndroidEventManager.getInstance().runEvent(
				EventCode.VoicePlayCompletioned,mPlayingMessage);
		mPlayingMessage = null;
	}

	@Override
	public void onStarted(boolean bSuccess) {
		pause();
	}

	@Override
	public void onStoped(boolean bBeyondMinTime) {
		resume();
	}

	@Override
	public void onExceedMaxTime() {
	}

	@Override
	public void onInterrupted() {
	}

	@Override
	public void onDecibelChanged(double decibel) {
	}
}
