package com.xbcx.jianhua.activity;

import java.util.HashMap;

import com.xbcx.im.ui.XBaseActivity;
import com.xbcx.jianhua.R;
import com.xbcx.utils.SystemUtils;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ChooseBaseActivity extends XBaseActivity implements 
												View.OnClickListener{

	protected LinearLayout			mLayoutChooseFiles;
	protected Button				mButtonOK;
	
	protected int					mChooseCount;
	
	protected HashMap<Object, View> mMapTagToView = new HashMap<Object, View>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mButtonOK = (Button)findViewById(R.id.btnOK);
		if(mButtonOK != null){
			mButtonOK.setOnClickListener(this);
			mButtonOK.setVisibility(View.GONE);
		}
		mLayoutChooseFiles = (LinearLayout)findViewById(R.id.layoutChooseFiles);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		final Object tag = v.getTag();
		if(tag != null && mMapTagToView.containsKey(tag)){
			removeChooseView(v);
			onChooseViewRemoved(tag);
		}
	}
	
	protected void onChooseViewRemoved(Object tag){
	}
	
	protected void addChooseImageView(int resId,Object tag){
		ImageView iv = new ImageView(this);
		iv.setImageResource(resId);
		addChooseView(iv,tag);
	}

	protected void addChooseView(View iv,Object tag){
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)iv.getLayoutParams();
		final int size = SystemUtils.dipToPixel(this, 30);
		if(lp == null){
			lp = new LinearLayout.LayoutParams(size,size);
		}
		lp.leftMargin = SystemUtils.dipToPixel(this, 5);
		iv.setLayoutParams(lp);
		iv.setOnClickListener(this);
		iv.setTag(tag);
		
		mLayoutChooseFiles.addView(iv);
		
		mMapTagToView.put(tag, iv);
		
		++mChooseCount;
		
		updateButton();
	}
	
	protected void removeChooseViewByTag(Object tag){
		final View v = mMapTagToView.get(tag);
		if(v != null){
			removeChooseView(v);
		}
	}
	
	protected void removeChooseView(View v){
		mLayoutChooseFiles.removeView(v);
		
		--mChooseCount;
		
		updateButton();
	}
	
	protected void updateButton(){
		final int showCount = getChooseShowCount();
		
		if(showCount > 0){
			mButtonOK.setVisibility(View.VISIBLE);
			mButtonOK.setText(getString(R.string.sure) + "(" + showCount + ")");
		}else{
			mButtonOK.setVisibility(View.GONE);
		}
	}
	
	protected int getChooseShowCount(){
		return mChooseCount;
	}
}
