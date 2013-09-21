package com.xbcx.im.ui.messageviewprovider;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.xbcx.im.XMessage;
import com.xbcx.im.ui.MessageThumbnailProvider;
import com.xbcx.library.R;

public class PhotoViewLeftProvider extends CommonViewProvider {

	public PhotoViewLeftProvider(OnViewClickListener listener) {
		super(listener);
	}
	
	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.getType() == XMessage.TYPE_PHOTO){
			XMessage m = (XMessage)message;
			return !m.isFromSelf();
		}
		return false;
	}

	@Override
	protected void onUpdateView(CommonViewHolder viewHolder, XMessage m) {
		PhotoViewHolder pHolder = (PhotoViewHolder)viewHolder;
		ProgressBar progressBar = pHolder.mProgressBar;
		if(m.isThumbPhotoDownloading()){
			pHolder.mImageViewPhoto.setImageResource(R.drawable.chat_img);
			progressBar.setVisibility(View.VISIBLE);
			progressBar.setProgress(m.getThumbPhotoDownloadPercentage());
		}else{
			if(m.isThumbPhotoFileExists()){
				final Bitmap bmp = MessageThumbnailProvider.loadThumbPhoto(m);
				if(bmp == null){
					pHolder.mImageViewPhoto.setImageResource(R.drawable.chat_img_wrong);
					pHolder.mViewWarning.setVisibility(View.VISIBLE);
				}else{
					pHolder.mImageViewPhoto.setImageBitmap(bmp);
				}
			}else if(m.isDownloaded()){
				pHolder.mImageViewPhoto.setImageResource(R.drawable.chat_img_wrong);
				pHolder.mViewWarning.setVisibility(View.VISIBLE);
			}else{
				pHolder.mImageViewPhoto.setImageResource(R.drawable.chat_img);
				pHolder.mViewWarning.setVisibility(View.GONE);
			}
			progressBar.setVisibility(View.GONE);
		}
	}

	@Override
	protected CommonViewHolder onCreateViewHolder() {
		return new PhotoViewHolder();
	}

	@Override
	protected void onSetViewHolder(View convertView, CommonViewHolder viewHolder) {
		super.onSetViewHolder(convertView, viewHolder);
		PhotoViewHolder pHolder = (PhotoViewHolder)viewHolder;
		final View contentView = LayoutInflater.from(convertView.getContext())
				.inflate(R.layout.message_content_photo, null);
		pHolder.mImageViewPhoto = (ImageView)contentView.findViewById(R.id.ivPhoto);
		pHolder.mProgressBar = (ProgressBar)contentView.findViewById(R.id.pb);
		pHolder.mProgressBar.setMax(100);
		pHolder.mContentView.addView(contentView);
	}

	protected static class PhotoViewHolder extends CommonViewHolder{
		public ImageView 	mImageViewPhoto;
		
		public ProgressBar 	mProgressBar;
	}
}
