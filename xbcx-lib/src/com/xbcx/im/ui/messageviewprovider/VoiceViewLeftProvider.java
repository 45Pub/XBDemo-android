package com.xbcx.im.ui.messageviewprovider;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xbcx.im.XMessage;
import com.xbcx.im.messageprocessor.VoicePlayProcessor;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

public class VoiceViewLeftProvider extends CommonViewProvider {

	protected int mTextViewMinWidth;
	protected int mTextViewWidthIncrement;
	
	public VoiceViewLeftProvider(Context context,OnViewClickListener listener){
		super(listener);
		mTextViewMinWidth = SystemUtils.dipToPixel(context, 30);
		mTextViewWidthIncrement = SystemUtils.dipToPixel(context, 2);
	}
	
	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.getType() == XMessage.TYPE_VOICE){
			XMessage hm = (XMessage)message;
			return !hm.isFromSelf();
		}
		return false;
	}
	
	@Override
	protected CommonViewHolder onCreateViewHolder() {
		return new VoiceViewHolder();
	}

	@Override
	protected void onSetViewHolder(View convertView, CommonViewHolder viewHolder) {
		super.onSetViewHolder(convertView, viewHolder);
		final View voiceView = onCreateVoiceView(convertView.getContext());
		voiceView.setClickable(false);
		VoiceViewHolder vViewHolder = (VoiceViewHolder)viewHolder;
		vViewHolder.mImageViewVoice = (ImageView)voiceView.findViewById(R.id.ivVoice);
		vViewHolder.mTextViewVoice = (TextView)voiceView.findViewById(R.id.tvVoice);
		vViewHolder.mProgressBar = (ProgressBar)voiceView.findViewById(R.id.pbDownload);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT, 
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER_VERTICAL);
		vViewHolder.mContentView.addView(voiceView,lp);
	}
	
	protected View onCreateVoiceView(Context context){
		return LayoutInflater.from(context).inflate(R.layout.message_content_voice_left, null);
	}

	@Override
	protected void onUpdateView(CommonViewHolder viewHolder, XMessage m) {
		VoiceViewHolder voiceViewHolder = (VoiceViewHolder)viewHolder;
		if(m.isVoiceDownloading()){
			voiceViewHolder.mProgressBar.setVisibility(View.VISIBLE);
			voiceViewHolder.mImageViewVoice.setVisibility(View.GONE);
		}else{
			final ImageView imageViewVoice = voiceViewHolder.mImageViewVoice;
			voiceViewHolder.mProgressBar.setVisibility(View.GONE);
			imageViewVoice.setVisibility(View.VISIBLE);
			if(VoicePlayProcessor.getInstance().isPlaying(m)){
				imageViewVoice.setImageResource(R.drawable.animlist_play_voice);
				AnimationDrawable ad = (AnimationDrawable)imageViewVoice.getDrawable();
				ad.start();
			}else if(m.isPlayed()){
				imageViewVoice.setImageResource(R.drawable.voice_played);
			}else if(m.isVoiceFileExists()){
				imageViewVoice.setImageResource(R.drawable.voice_playing_unplay);
			}else if(m.isDownloaded()){
				imageViewVoice.setImageResource(R.drawable.voice_playing_unplay);
				setShowWarningView(viewHolder.mViewWarning, true);
			}else{
				imageViewVoice.setImageResource(R.drawable.voice_playing_unplay);
			}
		}
		
		showSeconds(voiceViewHolder.mTextViewVoice, m);
	}
	
	protected void showSeconds(TextView textView,XMessage m){
		final int seconds = m.getVoiceSeconds();
		textView.setMinimumWidth(mTextViewMinWidth + (seconds - 1) * mTextViewWidthIncrement);
		textView.setText(seconds + "\"");
	}
	
	protected void showFail(TextView textView,int nResId){
		textView.setMinimumWidth(mTextViewMinWidth);
		textView.setText(nResId);
	}

	protected static class VoiceViewHolder extends CommonViewHolder{
		public ImageView 	mImageViewVoice;
		
		public ProgressBar 	mProgressBar;
		
		public TextView		mTextViewVoice;
	}
}
