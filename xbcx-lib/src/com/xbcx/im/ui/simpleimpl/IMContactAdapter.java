package com.xbcx.im.ui.simpleimpl;

import android.content.Context;

import com.xbcx.im.IMContact;
import com.xbcx.im.VCardProvider;

public class IMContactAdapter extends IMAdbAdapter<IMContact> {
	
	public IMContactAdapter(Context context){
		super(context);
	}

	@Override
	protected void onUpdateView(ViewHolder viewHolder,Object item, int position) {
		super.onUpdateView(viewHolder, item, position);
		AdbViewHolder aViewHolder = (AdbViewHolder)viewHolder;
		final IMContact contact = (IMContact)item;
		aViewHolder.mImageViewAvatar.setImageBitmap(
				VCardProvider.getInstance().loadAvatar(contact.getId()));
		aViewHolder.mTextViewName.setText(contact.getName());
	}
}
