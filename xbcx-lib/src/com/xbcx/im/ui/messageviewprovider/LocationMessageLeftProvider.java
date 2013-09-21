package com.xbcx.im.ui.messageviewprovider;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xbcx.im.XMessage;
import com.xbcx.im.ui.MessageThumbnailProvider;
import com.xbcx.library.R;

public class LocationMessageLeftProvider extends CommonViewProvider {

	public LocationMessageLeftProvider(OnViewClickListener listener) {
		super(listener);
	}

	@Override
	public boolean acceptHandle(XMessage message) {
		if(!message.isFromSelf()){
			return message.getType() == XMessage.TYPE_LOCATION;
		}
		return false;
	}

	@Override
	protected CommonViewHolder onCreateViewHolder() {
		return new LocationViewHolder();
	}

	@Override
	protected void onSetViewHolder(View convertView, CommonViewHolder viewHolder) {
		super.onSetViewHolder(convertView, viewHolder);
		LocationViewHolder holder = (LocationViewHolder)viewHolder;
		View v = LayoutInflater.from(convertView.getContext()).inflate(R.layout.message_content_location, null);
		holder.mImageViewLocation = (ImageView)v.findViewById(R.id.ivLocation);
		holder.mViewLoad = v.findViewById(R.id.pb);
		holder.mTextViewLocation = (TextView)v.findViewById(R.id.tvLocation);
		holder.mContentView.addView(v);
	}

	@Override
	protected void onUpdateView(CommonViewHolder viewHolder, XMessage m) {
		LocationViewHolder holder = (LocationViewHolder)viewHolder;
		if(m.isLocationDownloading()){
			holder.mViewLoad.setVisibility(View.VISIBLE);
		}else{
			holder.mViewLoad.setVisibility(View.GONE);
		}
		holder.mImageViewLocation.setImageBitmap(MessageThumbnailProvider.loadThumbPhoto(m));
		holder.mTextViewLocation.setText(m.getContent());
	}

	protected static class LocationViewHolder extends CommonViewHolder{
		public ImageView 	mImageViewLocation;
		public View			mViewLoad;
		public TextView		mTextViewLocation;
	}
}
