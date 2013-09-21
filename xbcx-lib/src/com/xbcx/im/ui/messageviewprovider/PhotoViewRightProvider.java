package com.xbcx.im.ui.messageviewprovider;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ProgressBar;

import com.xbcx.im.XMessage;
import com.xbcx.im.ui.MessageThumbnailProvider;
import com.xbcx.library.R;

public class PhotoViewRightProvider extends PhotoViewLeftProvider {

	public PhotoViewRightProvider(OnViewClickListener listener) {
		super(listener);
	}

	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.getType() == XMessage.TYPE_PHOTO){
			XMessage m = (XMessage)message;
			return m.isFromSelf();
		}
		return false;
	}

	@Override
	protected void onUpdateView(CommonViewHolder viewHolder, XMessage m) {
		PhotoViewHolder pHolder = (PhotoViewHolder)viewHolder;
		Bitmap bmp = MessageThumbnailProvider.loadThumbPhoto(m);
		if(bmp == null){
			pHolder.mImageViewPhoto.setImageResource(R.drawable.chat_img);
		}else{
			pHolder.mImageViewPhoto.setImageBitmap(bmp);
		}
		if(m.isPhotoUploading()){
			final ProgressBar pb = pHolder.mProgressBar;
			pb.setVisibility(View.VISIBLE);
			pb.setProgress(m.getPhotoUploadPercentage());
		}else if(m.isThumbPhotoDownloading()){
			pHolder.mProgressBar.setVisibility(View.VISIBLE);
			pHolder.mProgressBar.setProgress(m.getThumbPhotoDownloadPercentage());
		}else if(m.isUploadSuccess()){
			pHolder.mProgressBar.setVisibility(View.GONE);
		}else{
			pHolder.mProgressBar.setVisibility(View.GONE);
			setShowWarningView(pHolder.mViewWarning, true);
		}
	}
}
