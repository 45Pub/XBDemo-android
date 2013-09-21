package com.xbcx.jianhua.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.xbcx.adapter.SetBaseAdapter;
import com.xbcx.core.UrlBitmapDownloadCallback;
import com.xbcx.core.XApplication;
import com.xbcx.im.ui.simpleimpl.OnCheckCallBack;
import com.xbcx.jianhua.R;

public abstract class OrgListAdapter<E extends Object> extends SetBaseAdapter<E> implements
																View.OnClickListener,
																UrlBitmapDownloadCallback{
	
	public static int TAG_KEY_ADAPTER = 1; 
	
	protected Context 		mContext;
	
	protected boolean		mIsLv1Back;
	protected boolean		mIsShowInfoBtn;
	
	protected boolean		mIsCheck;
	
	protected Object		mSelectItem;
	
	private OnChildViewClickListener	mOnChildViewClickListener;
	private OnCheckCallBack				mOnCheckCallback;				
	
	public OrgListAdapter(Context context,OnChildViewClickListener listener){
		mContext = context;
		mOnChildViewClickListener = listener;
	}
	
	public void 	setSelectItem(Object e){
		mSelectItem = e;
		notifyDataSetChanged();
	}
	
	public Object	getSelectItem(){
		return mSelectItem;
	}
	
	public void setOnChildViewClickListener(OnChildViewClickListener listener){
		mOnChildViewClickListener = listener;
	}
	
	public void setOnCheckCallback(OnCheckCallBack callback){
		mOnCheckCallback = callback;
	}
	
	public void setIsLv1Back(boolean bIs){
		mIsLv1Back = bIs;
		notifyDataSetChanged();
	}
	
	public void setShowInfoBtn(boolean bShow){
		mIsShowInfoBtn = bShow;
		notifyDataSetChanged();
	}
	
	public void	setIsCheck(boolean bCheck){
		if(mIsCheck != bCheck){
			mIsCheck = bCheck;
			notifyDataSetChanged();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if(convertView == null){
			convertView = createConvertView();
			viewHolder = onCreateViewHolder();
			onSetViewHolder(viewHolder, convertView);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		
		final Object item = getItem(position);
		viewHolder.mViewInfo.setTag(item);
		if(viewHolder.mCheckBox != null){
			viewHolder.mCheckBox.setTag(item);
			if(mIsCheck){
				viewHolder.mCheckBox.setVisibility(View.VISIBLE);
				if(mOnCheckCallback != null){
					viewHolder.mCheckBox.setChecked(mOnCheckCallback.isCheck(item));
				}
			}else{
				viewHolder.mCheckBox.setVisibility(View.GONE);
			}
		}
		
		if(item.equals(mSelectItem)){
			viewHolder.mViewBack.setBackgroundResource(R.drawable.multilevel_selected_190);
		}else{
			if(mIsLv1Back){
				viewHolder.mViewBack.setBackgroundResource(R.drawable.list_bg_92);
			}else{
				viewHolder.mViewBack.setBackgroundResource(R.drawable.multilevel_bg_right);
			}
		}
		
		if(mIsShowInfoBtn){
			viewHolder.mViewInfo.setVisibility(View.VISIBLE);
		}else{
			viewHolder.mViewInfo.setVisibility(View.GONE);
		}
		
		onUpdateView(viewHolder, item,position);
		
		return convertView;
	}
	
	protected View createConvertView(){
		return LayoutInflater.from(mContext).inflate(R.layout.adapter_org, null);
	}
	
	protected ViewHolder onCreateViewHolder(){
		return new ViewHolder();
	}
	
	protected void onSetViewHolder(ViewHolder viewHolder,View convertView){
		viewHolder.mViewBack = convertView.findViewById(R.id.viewBack);
		viewHolder.mCheckBox = (CheckBox)convertView.findViewById(R.id.cb);
		viewHolder.mViewTriangle = convertView.findViewById(R.id.ivTriangle);
		viewHolder.mImageViewAvatar = (ImageView)convertView.findViewById(R.id.ivAvatar);
		viewHolder.mTextViewName = (TextView)convertView.findViewById(R.id.tvName);
		viewHolder.mTextViewMember = (TextView)convertView.findViewById(R.id.tvMember);
		viewHolder.mViewInfo = convertView.findViewById(R.id.ivInfo);
		viewHolder.mViewInfo.setOnClickListener(this);
		viewHolder.mCheckBox.setOnClickListener(this);
		viewHolder.mCheckBox.setFocusable(false);
	}
	
	protected abstract void onUpdateView(ViewHolder viewHolder,Object item,int pos);
	
	@Override
	public void onBitmapDownloadSuccess(String url) {
	}
	
	protected void setBitmap(ImageView iv,String url,int defaultResId){
		final Bitmap bmp = XApplication.loadBitmap(url, this);
		if(bmp == null){
			iv.setImageResource(defaultResId);
		}else{
			iv.setImageBitmap(bmp);
		}
	}

	@Override
	public void onClick(View v) {
		if(mOnChildViewClickListener != null){
			mOnChildViewClickListener.onChildViewClicked(this,v.getTag(), v.getId(),v);
		}
	}

	public static interface OnChildViewClickListener{
		@SuppressWarnings("rawtypes")
		public void onChildViewClicked(OrgListAdapter adapter,Object item,int viewId,View v);
	}
	
	protected static class ViewHolder{
		public View			mViewBack;
		public CheckBox		mCheckBox;
		public View			mViewTriangle;
		public ImageView	mImageViewAvatar;
		public TextView 	mTextViewName;
		public TextView		mTextViewMember;
		public View			mViewInfo;
	}
}
