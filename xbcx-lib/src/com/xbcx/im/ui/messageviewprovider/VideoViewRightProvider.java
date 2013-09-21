package com.xbcx.im.ui.messageviewprovider;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ProgressBar;

import com.xbcx.im.XMessage;
import com.xbcx.im.ui.MessageThumbnailProvider;
import com.xbcx.library.R;

public class VideoViewRightProvider extends VideoViewLeftProvider {

	public VideoViewRightProvider(OnViewClickListener listener) {
		super(listener);
	}

	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.isFromSelf()){
			return message.getType() == XMessage.TYPE_VIDEO;
		}
		return false;
	}

	@Override
	protected void onUpdateView(CommonViewHolder viewHolder, XMessage m) {
		VideoViewHolder vHolder = (VideoViewHolder)viewHolder;
		Bitmap bmp = MessageThumbnailProvider.loadThumbPhoto(m);
		if(bmp == null){
			vHolder.mImageViewVideo.setImageResource(R.drawable.msg_video_default);
		}else{
			vHolder.mImageViewVideo.setImageBitmap(bmp);
		}
		if(m.isVideoUploading()){
			final ProgressBar pb = vHolder.mProgressBar;
			pb.setVisibility(View.VISIBLE);
			pb.setProgress(m.getVideoUploadPercentage());
			vHolder.mTextViewTime.setVisibility(View.GONE);
			vHolder.mButton.setVisibility(View.VISIBLE);
		}else if(m.isVideoDownloading()){
			vHolder.mProgressBar.setVisibility(View.VISIBLE);
			vHolder.mProgressBar.setProgress(m.getVideoDownloadPercentage());
			vHolder.mTextViewTime.setVisibility(View.GONE);
			vHolder.mButton.setVisibility(View.VISIBLE);
		}else if(m.isUploadSuccess()){
			vHolder.mProgressBar.setVisibility(View.GONE);
			vHolder.mTextViewTime.setVisibility(View.VISIBLE);
			vHolder.mTextViewTime.setText(getVideoTimeShow(m.getVideoSeconds()));
			vHolder.mButton.setVisibility(View.GONE);
		}else{
			vHolder.mProgressBar.setVisibility(View.GONE);
			setShowWarningView(vHolder.mViewWarning, true);
			vHolder.mTextViewTime.setVisibility(View.VISIBLE);
			vHolder.mTextViewTime.setText(getVideoTimeShow(m.getVideoSeconds()));
			vHolder.mButton.setVisibility(View.GONE);
		}
	}

}
