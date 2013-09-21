package com.xbcx.view;

import com.xbcx.utils.SystemUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundCornerImageView extends ImageView {
	
	private float 	mRadius;
	
	private Path 	mPathClip;
	
	public RoundCornerImageView(Context context) {
		super(context);
		init();
	}

	public RoundCornerImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init(){
		mRadius = SystemUtils.dipToPixel(getContext(),2);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		try{
			if(mPathClip == null){
				mPathClip = new Path();
				mPathClip.addRoundRect(new RectF(0, 0, getWidth(), getHeight()),
						mRadius, mRadius, Path.Direction.CW);
			}
			canvas.clipPath(mPathClip);
			super.onDraw(canvas);
		}catch(Exception e){
			super.onDraw(canvas);
		}
	}

}
