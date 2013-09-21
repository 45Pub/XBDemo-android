package com.xbcx.im.ui.simpleimpl;

import android.content.Context;

import com.xbcx.im.IMGroup;
import com.xbcx.im.ui.LocalAvatar;
import com.xbcx.library.R;

public class IMGroupAdapter extends IMAdbAdapter<IMGroup> {
	
	public IMGroupAdapter(Context context){
		super(context);
	}

	@Override
	protected void onUpdateView(ViewHolder viewHolder,Object item, int position) {
		super.onUpdateView(viewHolder, item, position);
		AdbViewHolder aViewHolder = (AdbViewHolder)viewHolder;
		final IMGroup group = (IMGroup)item;
		aViewHolder.mImageViewAvatar.setImageResource(
				LocalAvatar.getAvatarResId(LocalAvatar.Group));
		aViewHolder.mTextViewName.setText(group.getName());
		aViewHolder.mTextViewNumber.setText(
				" (" + group.getMemberCount() + mContext.getString(R.string.people) + ")");
	}

}
