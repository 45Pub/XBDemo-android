package com.xbcx.im.ui.messageviewprovider;

import java.util.Collection;
import java.util.HashMap;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.xbcx.im.IMKernel;
import com.xbcx.im.VCardProvider;
import com.xbcx.im.XMessage;
import com.xbcx.im.ui.IMGlobalSetting;
import com.xbcx.im.ui.IMMessageViewProvider;
import com.xbcx.library.R;

public abstract class CommonViewProvider extends IMMessageViewProvider implements
													View.OnClickListener,
													View.OnLongClickListener{
	
	public static final int ViewInfo_DEFAULT 	= 0;
	public static final int ViewInfo_SHOW		= 1;
	public static final int ViewInfo_HIDE		= 2;
	
	protected int	mViewInfoShowType = ViewInfo_DEFAULT;
	
	protected CommonViewProviderDelegate	mDelegate;
	
	protected HashMap<String, XMessage> 	mMapIdToCheckMessage = new HashMap<String, XMessage>();
	
	public CommonViewProvider(OnViewClickListener listener){
		setOnViewClickListener(listener);
		mDelegate = IMGlobalSetting.msgViewProviderDelegate;
	}
	
	public void setViewInfoShowType(int type){
		mViewInfoShowType = type;
	}
	
	@Override
	public void setIsCheck(boolean bCheck) {
		super.setIsCheck(bCheck);
		if(!bCheck){
			mMapIdToCheckMessage.clear();
		}
	}

	@Override
	public void setCheckItem(XMessage xm, boolean bCheck) {
		super.setCheckItem(xm, bCheck);
		if(bCheck){
			mMapIdToCheckMessage.put(xm.getId(), xm);
		}else{
			mMapIdToCheckMessage.remove(xm.getId());
		}
		onCheckChanged(xm);
	}

	@Override
	public boolean isCheckedItem(XMessage xm) {
		return mMapIdToCheckMessage.containsKey(xm.getId());
	}

	@Override
	public Collection<XMessage> getCheckedMessage() {
		return mMapIdToCheckMessage.values();
	}

	@Override
	public View getView(XMessage message, View convertView, ViewGroup parent) {
		CommonViewHolder viewHolder = null;
		XMessage m = (XMessage)message;
		
		if(convertView == null){
			viewHolder = onCreateViewHolder();
			convertView = onCreateView(m,parent.getContext());
			onSetViewHolder(convertView, viewHolder);
			
			if(viewHolder.mViewInfo != null){
				if(mViewInfoShowType == ViewInfo_SHOW){
					viewHolder.mViewInfo.setVisibility(View.VISIBLE);
				}else if(mViewInfoShowType == ViewInfo_HIDE){
					viewHolder.mViewInfo.setVisibility(View.GONE);
				}else{
					if(m.getFromType() == XMessage.FROMTYPE_SINGLE){
						viewHolder.mViewInfo.setVisibility(View.GONE);
					}else{
						viewHolder.mViewInfo.setVisibility(View.VISIBLE);
					}
				}
			}

			convertView.setTag(viewHolder);
		}else{
			viewHolder = (CommonViewHolder)convertView.getTag();
		}
		
		onSetViewTag(viewHolder, m);
		
		if(mIsCheck){
			viewHolder.mCheckBox.setVisibility(View.VISIBLE);
			viewHolder.mCheckBox.setChecked(mMapIdToCheckMessage.containsKey(m.getId()));
		}else{
			viewHolder.mCheckBox.setVisibility(View.GONE);
		}
		
		if(m.isFromSelf()){
			viewHolder.mImageViewAvatar.setImageBitmap(
					VCardProvider.getInstance().loadAvatar(IMKernel.getLocalUser()));
		}else{
			viewHolder.mImageViewAvatar.setImageBitmap(
					VCardProvider.getInstance().loadAvatar(m.getUserId()));
		}
		
		if(m.isFromSelf()){
			viewHolder.mTextViewNickname.setText(
					VCardProvider.getInstance().loadUserName(IMKernel.getLocalUser()));
		}else{
			viewHolder.mTextViewNickname.setText(m.getUserName());
		}
		
		onSetContentViewBackground(viewHolder, m);
		
		onUpdateSendStatus(viewHolder, m);
		
		onUpdateView(viewHolder, m);
		
		if(mDelegate != null){
			mDelegate.onUpdateView(viewHolder, m);
		}
		
		return convertView;
	}

	protected View onCreateView(XMessage m,Context context){
		if(m.isFromSelf()){
			return LayoutInflater.from(context).inflate(R.layout.message_common_right, null);
		}else{
			return LayoutInflater.from(context).inflate(R.layout.message_common_left, null);
		}
	}
	
	protected CommonViewHolder onCreateViewHolder(){
		return new CommonViewHolder();
	}
	
	protected void onSetViewHolder(View convertView,CommonViewHolder viewHolder){
		if(mDelegate == null || !mDelegate.onSetViewHolder(viewHolder, convertView)){
			viewHolder.mCheckBox = (CheckBox)convertView.findViewById(R.id.cb);
			viewHolder.mImageViewAvatar = (ImageView)convertView.findViewById(R.id.ivAvatar);
			viewHolder.mViewInfo = convertView.findViewById(R.id.viewInfo);
			viewHolder.mTextViewNickname = (TextView)convertView.findViewById(R.id.tvNickname);
			viewHolder.mContentView = (FrameLayout)convertView.findViewById(R.id.viewContent);
			viewHolder.mViewWarning = (ImageView)convertView.findViewById(R.id.ivWarning);
			viewHolder.mViewSending = convertView.findViewById(R.id.pbSending);
			viewHolder.mButton = (TextView)convertView.findViewById(R.id.btn);
			viewHolder.mButton.setVisibility(View.GONE);
			
			viewHolder.mCheckBox.setOnClickListener(this);
			
			viewHolder.mImageViewAvatar.setOnClickListener(this);
			viewHolder.mContentView.setOnClickListener(this);
			viewHolder.mContentView.setOnLongClickListener(this);
			viewHolder.mButton.setOnClickListener(this);
			viewHolder.mViewWarning.setOnClickListener(this);
		}
	}
	
	protected void onSetViewTag(CommonViewHolder viewHolder,XMessage m){
		if(mDelegate == null || !mDelegate.onSetViewTag(viewHolder, m)){
			viewHolder.mImageViewAvatar.setTag(m);
			viewHolder.mContentView.setTag(m);
			viewHolder.mButton.setTag(m);
			viewHolder.mViewWarning.setTag(m);
			viewHolder.mCheckBox.setTag(m);
		}
	}
	
	protected void onSetContentViewBackground(CommonViewHolder viewHolder,XMessage m){
		if(mDelegate != null){
			mDelegate.onSetContentViewBackground(viewHolder, m);
		}
	}
	
	protected void onUpdateSendStatus(CommonViewHolder viewHolder,XMessage m){
		if(mDelegate == null || !mDelegate.onUpdateSendStatus(viewHolder, m)){
			if(IMKernel.isSendingMessage(m.getId())){
				viewHolder.mViewSending.setVisibility(View.VISIBLE);
				viewHolder.mViewWarning.setVisibility(View.GONE);
			}else{
				viewHolder.mViewSending.setVisibility(View.GONE);
				final View viewWarning = viewHolder.mViewWarning;
				if(m.isFromSelf()){
					if(m.isSended()){
						if(m.isSendSuccess()){
							viewWarning.setVisibility(View.GONE);
						}else{
							viewWarning.setVisibility(View.VISIBLE);
						}
					}else{
						viewWarning.setVisibility(View.GONE);
					}
				}else{
					viewWarning.setVisibility(View.GONE);
				}
			}
		}
	}
	
	protected void setShowWarningView(ImageView viewWarning,boolean bShow){
		if(bShow){
			viewWarning.setVisibility(View.VISIBLE);
		}else{
			viewWarning.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onClick(View v) {
		if(mOnViewClickListener != null){
			mOnViewClickListener.onViewClicked((XMessage)v.getTag(), v.getId());
		}
		if(v.getId() == R.id.cb){
			final CheckBox cb = (CheckBox)v;
			XMessage xm = (XMessage)cb.getTag();
			if(cb.isChecked()){
				mMapIdToCheckMessage.put(xm.getId(), xm);
			}else{
				mMapIdToCheckMessage.remove(xm.getId());
			}
			onCheckChanged(xm);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if(mOnViewClickListener != null){
			return mOnViewClickListener.onViewLongClicked((XMessage)v.getTag(), v.getId());
		}
		return false;
	}

	@Override
	public boolean acceptHandle(XMessage message) {
		return false;
	}

	protected abstract void onUpdateView(CommonViewHolder viewHolder,XMessage m);
	
	public static class CommonViewHolder{
		
		public CheckBox				mCheckBox;
		
		public ImageView 			mImageViewAvatar;
		
		public View					mViewInfo;
		
		public TextView				mTextViewNickname;
		
		public FrameLayout			mContentView;
		
		public ImageView			mViewWarning;
		
		public TextView				mButton;
		
		public View					mViewSending;
		
		public SparseArray<View> 	mMapIdToView;
	}
}
