package com.xbcx.jianhua.adapter;

import java.util.ArrayList;
import java.util.List;

import com.xbcx.jianhua.R;

import android.content.Context;
import android.view.View;

public class RootOrgAdapter extends DepartmemberAdapter {
	
	private List<PluginItem> mPluginItems = new ArrayList<RootOrgAdapter.PluginItem>();
	
	public RootOrgAdapter(Context context,OnChildViewClickListener listener) {
		super(context,listener);
	}
	
	public void addPluginItem(PluginItem item){
		mPluginItems.add(item);
	}

	@Override
	public void clear() {
		mPluginItems.clear();
		super.clear();
	}

	@Override
	public Object getItem(int position) {
		if(position < super.getCount()){
			return super.getItem(position);
		}else{
			return mPluginItems.get(position - super.getCount());
		}
	}

	@Override
	public int getCount() {
		return super.getCount() + mPluginItems.size();
	}

	@Override
	protected void onUpdateView(ViewHolder viewHolder, Object item,int pos) {
		final int oldItemCount = super.getCount();
		if(pos < oldItemCount){
			super.onUpdateView(viewHolder, item,pos);
			viewHolder.mImageViewAvatar.setVisibility(View.VISIBLE);
			viewHolder.mImageViewAvatar.setImageResource(R.drawable.multilevel_company);
			viewHolder.mViewTriangle.setVisibility(View.GONE);
		}else{
			final PluginItem pItem = mPluginItems.get(pos - oldItemCount);
			viewHolder.mViewInfo.setVisibility(View.GONE);
			viewHolder.mImageViewAvatar.setImageResource(pItem.mAvatarResId);
			viewHolder.mTextViewName.setText(pItem.mNameResId);
			viewHolder.mCheckBox.setVisibility(View.GONE);
		}
	}

	public static class PluginItem{
		public final int	mPluginId;
		
		public final int 	mAvatarResId;
		
		public final int	mNameResId;
		
		public PluginItem(int pluginId,int avatarResId,int nameResId){
			mPluginId = pluginId;
			mAvatarResId = avatarResId;
			mNameResId = nameResId;
		}

		@Override
		public boolean equals(Object o) {
			if(o == this){
				return true;
			}
			if(o != null && o instanceof PluginItem){
				return mPluginId == ((PluginItem)o).mPluginId;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return mPluginId;
		}

		@Override
		public String toString() {
			return String.valueOf(mPluginId);
		}
	}
}
