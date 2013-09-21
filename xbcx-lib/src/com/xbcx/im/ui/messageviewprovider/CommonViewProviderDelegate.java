package com.xbcx.im.ui.messageviewprovider;

import com.xbcx.im.XMessage;
import com.xbcx.im.ui.messageviewprovider.CommonViewProvider.CommonViewHolder;

import android.view.View;

public interface CommonViewProviderDelegate {
	
	public boolean 	onSetViewHolder(CommonViewHolder holder,View convertView);
	
	public boolean 	onSetViewTag(CommonViewHolder holder,XMessage xm);
	
	public boolean 	onSetContentViewBackground(CommonViewHolder holder,XMessage xm);
	
	public boolean 	onUpdateSendStatus(CommonViewHolder holder,XMessage xm);
	
	public void		onUpdateView(CommonViewHolder holder,XMessage xm);
}
