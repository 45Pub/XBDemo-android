package com.xbcx.im.ui.simpleimpl;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.xbcx.adapter.SetBaseAdapter;

public abstract class AbsBaseAdapter<E extends Object> extends SetBaseAdapter<E>
												implements View.OnClickListener{

	protected Context mContext;
	
	protected OnChildViewClickListener	mOnChildViewClickListener;
	
	public AbsBaseAdapter(Context context){
		mContext = context;
	}
	
	public void setOnChildViewClickListener(OnChildViewClickListener listerner){
		mOnChildViewClickListener = listerner;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if(convertView == null){
			convertView = onCreateConvertView();
			viewHolder = onCreateViewHolder();
			onSetViewHolder(viewHolder, convertView);
			
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		
		final Object item = getItem(position);
		onSetChildViewTag(viewHolder, item);
		
		onUpdateView(viewHolder, item, position);
		
		return convertView;
	}

	protected abstract View onCreateConvertView();
	
	protected abstract ViewHolder onCreateViewHolder();
	
	protected abstract void	onSetViewHolder(ViewHolder viewHolder,View convertView);
	
	protected abstract void	onSetChildViewTag(ViewHolder viewHolder,Object item);
	
	protected abstract void	onUpdateView(ViewHolder viewHolder,Object item,int position);
	
	@Override
	public void onClick(View v) {
		if(mOnChildViewClickListener != null){
			mOnChildViewClickListener.onChildViewClicked(this, v.getTag(), v.getId(), v);
		}
	}
	
	protected static class ViewHolder{
		
	}
}
