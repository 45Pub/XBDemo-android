package com.xbcx.jianhua.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.xbcx.im.IMGroup;
import com.xbcx.jianhua.R;

public class OrgGroupAdapter extends OrgListAdapter<IMGroup>{

	public OrgGroupAdapter(Context context,OnChildViewClickListener listener) {
		super(context,listener);
	}

	@Override
	protected void onUpdateView(ViewHolder viewHolder, Object item, int pos) {
		viewHolder.mImageViewAvatar.setVisibility(View.GONE);
		
		IMGroup group = (IMGroup)item;
		
		final int totalCount = group.getMemberCount();
		viewHolder.mTextViewName.setText(group.getName());
		viewHolder.mTextViewMember.setText("(" + totalCount + ")");
	}

	@Override
	protected View createConvertView() {
		return LayoutInflater.from(mContext).inflate(R.layout.adapter_group, null);
	}
}
