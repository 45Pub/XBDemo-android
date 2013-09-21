package com.xbcx.im.ui.simpleimpl;

import com.xbcx.library.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdbSectionAdapter extends BaseAdapter {

	private Context	mContext;
	private String	mText;
	private boolean	mIsShow = true;
	
	public AdbSectionAdapter(Context context,String text) {
		mContext = context;
		mText = text;
	}
	
	public void setText(String text){
		mText = text;
	}
	
	public void 	setVisible(boolean bShow){
		mIsShow = bShow;
		notifyDataSetChanged();
	}
	
	public boolean	isVisible(){
		return mIsShow;
	}
	
	@Override
	public int getCount() {
		return 1;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_adb_section, null);
			viewHolder = new ViewHolder();
			viewHolder.mTextViewName = (TextView)convertView.findViewById(R.id.tvName);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		
		viewHolder.mTextViewName.setText(mText);
		
		if(mIsShow){
			viewHolder.mTextViewName.setVisibility(View.VISIBLE);
			convertView.setBackgroundResource(R.drawable.contact_category_bar);
		}else{
			viewHolder.mTextViewName.setVisibility(View.GONE);
			convertView.setBackgroundDrawable(null);
			convertView.setMinimumHeight(0);
		}
		
		return convertView;
	}

	private static class ViewHolder{
		public TextView mTextViewName;
	}
}
