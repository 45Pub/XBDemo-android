package com.xbcx.im.ui.simpleimpl;

import com.xbcx.im.IMContact;
import com.xbcx.im.IMKernel;
import com.xbcx.library.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class IMGroupMemberAdapter extends IMContactAdapter {
	
	protected boolean	mIsEdit;

	public IMGroupMemberAdapter(Context context) {
		super(context);
	}

	public void setIsEdit(boolean bEdit){
		mIsEdit = bEdit;
		notifyDataSetChanged();
	}
	
	public boolean isEdit(){
		return mIsEdit;
	}

	@Override
	protected void onUpdateView(ViewHolder viewHolder, Object item, int position) {
		super.onUpdateView(viewHolder, item, position);
		final MemberViewHolder holder = (MemberViewHolder)viewHolder;
		
		final IMContact c = (IMContact)item;
		holder.mViewDelete.setTag(item);
		if(mIsEdit){
			if(IMKernel.isLocalUser(c.getId())){
				holder.mViewDelete.setVisibility(View.GONE);
			}else{
				holder.mViewDelete.setVisibility(View.VISIBLE);
			}
		}else{
			holder.mViewDelete.setVisibility(View.GONE);
		}
	}

	@Override
	protected View onCreateConvertView() {
		return LayoutInflater.from(mContext).inflate(R.layout.adapter_imgroup_member, null);
	}

	@Override
	protected ViewHolder onCreateViewHolder() {
		return new MemberViewHolder();
	}

	@Override
	protected void onSetViewHolder(ViewHolder viewHolder, View convertView) {
		super.onSetViewHolder(viewHolder, convertView);
		MemberViewHolder holder = (MemberViewHolder)viewHolder;
		holder.mViewDelete = convertView.findViewById(R.id.btnDelete);
		holder.mViewDelete.setOnClickListener(this);
	}
	
	protected static class MemberViewHolder extends AdbViewHolder{
		public View	mViewDelete;
	}
}
