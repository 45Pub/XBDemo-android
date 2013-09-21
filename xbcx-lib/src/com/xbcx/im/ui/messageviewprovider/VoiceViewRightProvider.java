package com.xbcx.im.ui.messageviewprovider;

import com.xbcx.im.XMessage;
import com.xbcx.im.messageprocessor.VoicePlayProcessor;
import com.xbcx.library.R;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class VoiceViewRightProvider extends VoiceViewLeftProvider {

	public VoiceViewRightProvider(Context context,OnViewClickListener listener) {
		super(context,listener);
	}

	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.getType() == XMessage.TYPE_VOICE){
			XMessage hm = (XMessage)message;
			return hm.isFromSelf();
		}
		return false;
	}
	
	@Override
	protected void onUpdateView(CommonViewHolder viewHolder, XMessage m) {
		VoiceViewHolder voiceHolder = (VoiceViewHolder)viewHolder;
		if(m.isVoiceUploading() ||
				m.isVoiceDownloading()){
			voiceHolder.mProgressBar.setVisibility(View.VISIBLE);
			voiceHolder.mImageViewVoice.setVisibility(View.GONE);
		}else{
			ImageView imageViewVoice = voiceHolder.mImageViewVoice;
			voiceHolder.mProgressBar.setVisibility(View.GONE);
			imageViewVoice.setVisibility(View.VISIBLE);
			if (m.isUploadSuccess()) {
				if (VoicePlayProcessor.getInstance().isPlaying(m)) {
					imageViewVoice.setImageResource(R.drawable.animlist_play_voice);
					AnimationDrawable ad = (AnimationDrawable)imageViewVoice.getDrawable();
					ad.start();
				} else{
					imageViewVoice.setImageResource(R.drawable.voice_played);
				}
			}else{
				imageViewVoice.setImageResource(R.drawable.voice_played);
				setShowWarningView(voiceHolder.mViewWarning, true);
			}
		}
		
		showSeconds(voiceHolder.mTextViewVoice, m);
	}

	@Override
	protected void onSetViewHolder(View convertView, CommonViewHolder viewHolder) {
		super.onSetViewHolder(convertView, viewHolder);
		VoiceViewHolder vHolder = (VoiceViewHolder)viewHolder;
		vHolder.mImageViewVoice.setScaleType(ScaleType.MATRIX);
		vHolder.mImageViewVoice.setImageResource(R.drawable.voice_playing_unplay);
		final int width = vHolder.mImageViewVoice.getDrawable().getIntrinsicWidth();
		final int height = vHolder.mImageViewVoice.getDrawable().getIntrinsicHeight();
		Matrix m = new Matrix();
		//final int fPx = SystemUtils.dipToPixel(convertView.getContext(), 10);
		//final int fPy = SystemUtils.dipToPixel(convertView.getContext(), 9);
		m.setRotate(180, (float)width / 2,(float)height / 2);
		vHolder.mImageViewVoice.setImageMatrix(m);
	}

	@Override
	protected View onCreateVoiceView(Context context) {
		return LayoutInflater.from(context).inflate(R.layout.message_content_voice_right, null);
	}

}
