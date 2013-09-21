package com.xbcx.core;

import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SimpleBaseUIFactory extends BaseUIFactory {
	
	public SimpleBaseUIFactory(Context context){
		super(context);
	}
	
	@Override
	public View createTitleBackButton() {
		ImageView iv = new ImageView(mContext);
		iv.setImageResource(R.drawable.nav_image_back);
		return iv;
	}

	@Override
	public int getTitleBackButtonLeftMargin() {
		return SystemUtils.dipToPixel(mContext, 2);
	}

	@Override
	public int getTitleBackButtonTopMargin() {
		return 0;
	}

	@Override
	public View createTitleRightImageButton(int resId) {
		ImageView iv = new ImageView(mContext);
		iv.setImageResource(resId);
		return iv;
	}

	@Override
	public int getTitleRightImageButtonRightMargin() {
		return SystemUtils.dipToPixel(mContext, 2);
	}

	@Override
	public int getTitleRightImageButtonTopMargin() {
		return SystemUtils.dipToPixel(mContext, 0);
	}

	@Override
	public View createTitleRightTextButton(int textId) {
		TextView v = (TextView)LayoutInflater.from(mContext).inflate(R.layout.textview_titleright, null);
		v.setText(textId);
		return v;
	}

	@Override
	public int getTitleRightTextButtonRightMargin() {
		return SystemUtils.dipToPixel(mContext, 2);
	}

	@Override
	public int getTitleRightTextButtonTopMargin() {
		return SystemUtils.dipToPixel(mContext, 10);
	}

}
