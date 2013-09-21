package com.xbcx.im.ui.messageviewprovider;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xbcx.im.XMessage;
import com.xbcx.im.ui.MessageThumbnailProvider;
import com.xbcx.library.R;

public class VideoViewLeftProvider extends CommonViewProvider {

	public VideoViewLeftProvider(OnViewClickListener listener) {
		super(listener);
	}

	@Override
	public boolean acceptHandle(XMessage message) {
		if(!message.isFromSelf()){
			return message.getType() == XMessage.TYPE_VIDEO;
		}
		return false;
	}

	@Override
	protected CommonViewHolder onCreateViewHolder() {
		return new VideoViewHolder();
	}

	@Override
	protected void onSetViewHolder(View convertView, CommonViewHolder viewHolder) {
		super.onSetViewHolder(convertView, viewHolder);
		View v = LayoutInflater.from(convertView.getContext()).inflate(R.layout.message_content_video, null);
		VideoViewHolder vHolder = (VideoViewHolder)viewHolder;
		vHolder.mImageViewVideo = (ImageView)v.findViewById(R.id.ivVideo);
		vHolder.mTextViewTime = (TextView)v.findViewById(R.id.tvTime);
		vHolder.mProgressBar = (ProgressBar)v.findViewById(R.id.pb);
		vHolder.mContentView.addView(v);
		vHolder.mButton.setText(R.string.cancel);
		vHolder.mButton.setTextColor(0xffa40000);
		vHolder.mButton.setBackgroundResource(R.drawable.msg_btn_gray);
	}

	@Override
	protected void onUpdateView(CommonViewHolder viewHolder, XMessage m) {
		VideoViewHolder vHolder = (VideoViewHolder)viewHolder;
		ProgressBar progressBar = vHolder.mProgressBar;
		if(m.isVideoThumbDownloading()){
			vHolder.mButton.setVisibility(View.GONE);
			vHolder.mImageViewVideo.setImageResource(R.drawable.msg_video_default);
			progressBar.setVisibility(View.VISIBLE);
			progressBar.setProgress(m.getVideoThumbDownloadPercentage());
			vHolder.mTextViewTime.setVisibility(View.GONE);
		}else if(m.isVideoDownloading()){
			vHolder.mButton.setVisibility(View.VISIBLE);
			vHolder.mImageViewVideo.setImageResource(R.drawable.msg_video_default);
			if(m.isVideoThumbFileExists()){
				final Bitmap bmp = MessageThumbnailProvider.loadThumbPhoto(m);
				if(bmp == null){
					vHolder.mImageViewVideo.setImageResource(R.drawable.msg_video_default);
				}else{
					vHolder.mImageViewVideo.setImageBitmap(bmp);
				}
			}else{
				vHolder.mImageViewVideo.setImageResource(R.drawable.msg_video_default);
			}
			progressBar.setVisibility(View.VISIBLE);
			progressBar.setProgress(m.getVideoDownloadPercentage());
			vHolder.mTextViewTime.setVisibility(View.GONE);
		}else{
			vHolder.mButton.setVisibility(View.GONE);
			if(m.isVideoThumbFileExists()){
				final Bitmap bmp = MessageThumbnailProvider.loadThumbPhoto(m);
				if(bmp == null){
					vHolder.mImageViewVideo.setImageResource(R.drawable.msg_video_default);
				}else{
					vHolder.mImageViewVideo.setImageBitmap(bmp);
				}
			}else if(m.isDownloaded()){
				vHolder.mImageViewVideo.setImageResource(R.drawable.chat_img_wrong);
				vHolder.mViewWarning.setVisibility(View.VISIBLE);
			}else{
				vHolder.mImageViewVideo.setImageResource(R.drawable.msg_video_default);
				vHolder.mViewWarning.setVisibility(View.GONE);
			}
			progressBar.setVisibility(View.GONE);
			vHolder.mTextViewTime.setVisibility(View.VISIBLE);
			vHolder.mTextViewTime.setText(getVideoTimeShow(m.getVideoSeconds()));
		}
	}
	
	protected String getVideoTimeShow(int seconds){
		final int minute = seconds / 60;
		final int second = seconds % 60;
		String strMinute,strSecond;
		if(minute > 9){
			strMinute = String.valueOf(minute);
		}else{
			strMinute = "0" + minute;
		}
		if(second > 9){
			strSecond = String.valueOf(second);
		}else{
			strSecond = "0" + second;
		}
		return strMinute + ":" + strSecond;
	}

	protected static class VideoViewHolder extends CommonViewHolder{
		public ImageView	mImageViewVideo;
		public TextView		mTextViewTime;
		public ProgressBar	mProgressBar;
	}
}
