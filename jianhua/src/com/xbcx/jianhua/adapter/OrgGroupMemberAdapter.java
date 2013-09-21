package com.xbcx.jianhua.adapter;

import com.xbcx.im.IMContact;
import com.xbcx.im.VCardProvider;

import android.content.Context;

public class OrgGroupMemberAdapter extends OrgListAdapter<IMContact> {

	public OrgGroupMemberAdapter(Context context,OnChildViewClickListener listener) {
		super(context,listener);
	}

	@Override
	protected void onUpdateView(ViewHolder viewHolder, Object item, int pos) {
		IMContact gm = (IMContact)item;
		viewHolder.mImageViewAvatar.setImageBitmap(
				VCardProvider.getInstance().loadAvatar(gm.getId()));
		viewHolder.mTextViewName.setText(gm.getName());
	}

}
