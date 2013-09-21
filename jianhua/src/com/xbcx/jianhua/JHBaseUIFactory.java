package com.xbcx.jianhua;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.xbcx.core.SimpleBaseUIFactory;
import com.xbcx.utils.SystemUtils;

public class JHBaseUIFactory extends SimpleBaseUIFactory {

	public JHBaseUIFactory(Context context) {
		super(context);
	}

	@Override
	public View createTitleBackButton() {
		TextView tv = (TextView)LayoutInflater.from(mContext).inflate(
				R.layout.textview_titleback, null);
		tv.setText(R.string.back);
		return tv;
	}

	@Override
	public int getTitleBackButtonLeftMargin() {
		return SystemUtils.dipToPixel(mContext, 2);
	}

	@Override
	public int getTitleBackButtonTopMargin() {
		return SystemUtils.dipToPixel(mContext, 10);
	}
	
	@Override
	public int getTitleRightImageButtonTopMargin() {
		return SystemUtils.dipToPixel(mContext, 2);
	}
}
