package com.xbcx.jianhua.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.xbcx.im.ui.XBaseActivity;
import com.xbcx.jianhua.R;
import com.xbcx.utils.SystemUtils;

public class JHViewPictureActivity extends XBaseActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addTextButtonInTitleRight(R.string.send);
		
		final String path = getIntent().getStringExtra("path");
		
		final ImageView	iv = (ImageView)findViewById(R.id.ivPhoto);
		BitmapFactory.Options op = new BitmapFactory.Options();
		SystemUtils.computeSampleSize(op, path,512, 512 * 512);
		Bitmap bmp = BitmapFactory.decodeFile(path, op);
		iv.setImageBitmap(bmp);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
	}

	@Override
	protected void onTitleRightButtonClicked(View v) {
		super.onTitleRightButtonClicked(v);
		setResult(RESULT_OK);
		finish();
	}
}
