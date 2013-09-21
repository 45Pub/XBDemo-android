package com.xbcx.jianhua.adapter;

import android.content.Context;
import android.view.View;

import com.xbcx.jianhua.Departmember;
import com.xbcx.jianhua.R;

public class DepartmemberAdapter extends OrgListAdapter<Departmember> {
	
	public DepartmemberAdapter(Context context,OnChildViewClickListener listener){
		super(context,listener);
	}

	@Override
	protected void onUpdateView(ViewHolder viewHolder, Object item, int pos) {
		Departmember dm = (Departmember)item;
		viewHolder.mTextViewName.setText(dm.getName());
		if(dm.isUser()){
			viewHolder.mImageViewAvatar.setVisibility(View.VISIBLE);
			setBitmap(viewHolder.mImageViewAvatar, dm.getAvatar(), R.drawable.avatar_user);
			viewHolder.mViewTriangle.setVisibility(View.GONE);
			viewHolder.mViewInfo.setVisibility(View.VISIBLE);
		}else{
			viewHolder.mImageViewAvatar.setVisibility(View.GONE);
			viewHolder.mViewTriangle.setVisibility(View.VISIBLE);
			viewHolder.mViewInfo.setVisibility(View.GONE);
		}
	}
}
