package com.xbcx.im.ui.simpleimpl;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.xbcx.library.R;

public abstract class IMAdbAdapter<E extends Object> extends AbsBaseAdapter<E>{
	
	protected OnCheckCallBack			mOnCheckCallBack;
	
	protected boolean					mIsCheck;
	
	public IMAdbAdapter(Context context){
		super(context);
	}
	
	public void setOnCheckCallBack(OnCheckCallBack callback){
		mOnCheckCallBack = callback;
	}
	
	public void setIsCheck(boolean bCheck){
		mIsCheck = bCheck;
		notifyDataSetChanged();
	}
	
	protected View onCreateConvertView(){
		return LayoutInflater.from(mContext).inflate(R.layout.adapter_adb, null);
	}
	
	protected ViewHolder onCreateViewHolder(){
		return new AdbViewHolder();
	}
	
	protected void	onSetViewHolder(ViewHolder viewHolder,View convertView){
		AdbViewHolder aViewHolder = (AdbViewHolder)viewHolder;
		aViewHolder.mViewBackground = convertView.findViewById(R.id.viewBackground);
		aViewHolder.mCheckBox = (CheckBox)convertView.findViewById(R.id.cb);
		aViewHolder.mImageViewAvatar = (ImageView)convertView.findViewById(R.id.ivAvatar);
		aViewHolder.mTextViewName = (TextView)convertView.findViewById(R.id.tvName);
		aViewHolder.mViewDivider = convertView.findViewById(R.id.viewDivider);
		aViewHolder.mTextViewNumber = (TextView)convertView.findViewById(R.id.tvNumber);
		aViewHolder.mTextViewDetail = (TextView)convertView.findViewById(R.id.tvDetail);
		if(aViewHolder.mTextViewDetail != null){
			aViewHolder.mTextViewDetail.setVisibility(View.GONE);
		}
		
		if(aViewHolder.mCheckBox != null){
			aViewHolder.mCheckBox.setOnClickListener(this);
		}
	}
	
	protected void	onSetChildViewTag(ViewHolder viewHolder,Object item){
		AdbViewHolder aViewHolder = (AdbViewHolder)viewHolder;
		if(aViewHolder.mCheckBox != null){
			aViewHolder.mCheckBox.setTag(item);
		}
	}

	protected void	onUpdateView(ViewHolder viewHolder,Object item,int position){
		AdbViewHolder aViewHolder = (AdbViewHolder)viewHolder;
		if(aViewHolder.mCheckBox != null){
			if(mIsCheck){
				aViewHolder.mCheckBox.setVisibility(View.VISIBLE);
				if(mOnCheckCallBack != null){
					aViewHolder.mCheckBox.setChecked(mOnCheckCallBack.isCheck(item));
				}
			}else{
				aViewHolder.mCheckBox.setVisibility(View.GONE);
			}
		}
	}

	protected static class AdbViewHolder extends ViewHolder{
		public View			mViewBackground;
		public CheckBox		mCheckBox;
		public ImageView 	mImageViewAvatar;
		public TextView		mTextViewName;
		public TextView		mTextViewNumber;
		public TextView		mTextViewDetail;
		public View			mViewDivider;
	}
}
