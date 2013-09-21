package com.xbcx.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.widget.ImageView;

@SuppressLint("FloatMath")
public class FaceImageView extends ImageView implements Runnable{

	private int mBmpWidth;
	private int mBmpHeight;
	
	private boolean		mIsInit = false;
	private float		mMinScale;
	private int			mViewWidth;
	private int 		mViewHeight;
	
	private boolean		mIsScaling;
	
	private Matrix 	mTempMatrix = new Matrix();
	
	private float	mPointLastX;
	private	float	mPointLastY;
	
	private float 	mScaleCenterX;
	private float	mScaleCenterY;
	private float	mScaleDistanceLast;
	
	private float	mTemp[] = new float[9];
	
	public FaceImageView(Context context) {
		super(context);
		init();
	}
	
	public FaceImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public FaceImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init(){
		setScaleType(ScaleType.MATRIX);
		
		mScaleCenterX = -1;
		mPointLastX = -1;
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		if(bm != null){
			mBmpWidth = bm.getWidth();
			mBmpHeight = bm.getHeight();

			if(mIsInit){
				setInitMatrix();
			}
		}
	}

	@Override
	public void setImageURI(Uri uri) {
		super.setImageURI(uri);
		mBmpWidth = getDrawable().getIntrinsicWidth();
		mBmpHeight = getDrawable().getIntrinsicHeight();
	
		if(mIsInit){
			setInitMatrix();
		}
	}

	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		mBmpWidth = getDrawable().getIntrinsicWidth();
		mBmpHeight = getDrawable().getIntrinsicHeight();
	
		if(mIsInit){
			setInitMatrix();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		if(getMeasuredWidth() != 0){
			mViewWidth = getMeasuredWidth();
			mViewHeight = getMeasuredHeight();
			if(!mIsInit){
				setInitMatrix();
				mIsInit = true;
			}
		}
	}
	
	private void setInitMatrix(){
		final int nMeasuredWidth = mViewWidth;
		final int nMeasuredHeight = mViewHeight;
		if(mBmpHeight == 0){
			return;
		}
		if(mBmpWidth / mBmpHeight > nMeasuredWidth / nMeasuredHeight){
			int nFixedWidth = nMeasuredWidth;
			Matrix m = new Matrix();
			float fScale = (float)nFixedWidth / mBmpWidth;
			mMinScale = fScale;
			m.setTranslate(0 - (mBmpWidth - nMeasuredWidth) / 2, 
					0 - (mBmpHeight - nMeasuredHeight) / 2);
			m.postScale(fScale, fScale, nMeasuredWidth / 2, nMeasuredHeight / 2);
			setImageMatrix(m);
		}else{
			int nFixedHeight = nMeasuredHeight;
			Matrix m = new Matrix();
			float fScale = (float)nFixedHeight / mBmpHeight;
			mMinScale = fScale;
			m.setTranslate(0 - (mBmpWidth - nMeasuredWidth) / 2,
					0 - (mBmpHeight - nMeasuredHeight) / 2);
			m.postScale(fScale, fScale, nMeasuredWidth / 2, nMeasuredHeight / 2);
			setImageMatrix(m);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			mPointLastX = event.getX();
			mPointLastY = event.getY();
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			if(event.getPointerCount() > 1){
				final float fPointX1 = event.getX(0);
				final float fPointY1 = event.getY(0);
				
				final float fPointX2 = event.getX(1);
				final float fPointY2 = event.getY(1);
				
				initScale(fPointX1, fPointY1, fPointX2, fPointY2);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if(event.getPointerCount() > 1){
				final float fPointX1 = event.getX(0);
				final float fPointY1 = event.getY(0);
				
				final float fPointX2 = event.getX(1);
				final float fPointY2 = event.getY(1);
				
				if(mScaleCenterX == -1){
					initScale(fPointX1, fPointY1, fPointX2, fPointY2);
				}else{
					mIsScaling = true;
					float fDistance = getDistance(fPointX1, fPointY1, fPointX2, fPointY2);
					if(mScaleDistanceLast == 0){
						mScaleDistanceLast = fDistance;
					}else{
						if(Math.abs(fDistance - mScaleDistanceLast) > 2){
							mTempMatrix.set(getImageMatrix());
							final float fScale = fDistance / mScaleDistanceLast;
							mScaleDistanceLast = fDistance;
							
							mTempMatrix.postScale(fScale, fScale,mScaleCenterX,mScaleCenterY);
							
							mTempMatrix.getValues(mTemp);
							if(mTemp[Matrix.MSCALE_X] >= mMinScale){
								final float fMinX = getMinX(mTemp);
								if(mTemp[Matrix.MTRANS_X] < fMinX){
									mTempMatrix.postTranslate(fMinX - mTemp[Matrix.MTRANS_X], 0);
								}else{
									final float fMaxX = getMaxX(mTemp);
									if(mTemp[Matrix.MTRANS_X] > fMaxX){
										mTempMatrix.postTranslate(fMaxX - mTemp[Matrix.MTRANS_X], 0);
									}
								}
								final float fMinY = getMinY(mTemp);
								if(mTemp[Matrix.MTRANS_Y] < fMinY){
									mTempMatrix.postTranslate(0, fMinY - mTemp[Matrix.MTRANS_Y]);
								}else{
									final float fMaxY = getMaxY(mTemp);
									if(mTemp[Matrix.MTRANS_Y] > fMaxY){
										mTempMatrix.postTranslate(0, fMaxY - mTemp[Matrix.MTRANS_Y]);
									}
								}
								
								setImageMatrix(mTempMatrix);
							}
						}
					}
				}
			}else{
				if(!mIsScaling){
					final float fx = event.getX();
					final float fy = event.getY();
				
					if(mPointLastX != -1){
						Matrix m = getImageMatrix();
						mTempMatrix.set(m);
					
						mTempMatrix.postTranslate(fx - mPointLastX, 0);
						mTempMatrix.getValues(mTemp);
						final float fMaxX = getMaxX(mTemp);
						final float fMinX = getMinX(mTemp);
						final float fTempX = mTemp[Matrix.MTRANS_X];
						if(fTempX >= fMinX && fTempX <= fMaxX){
							setImageMatrix(mTempMatrix);
						}else{
							mTempMatrix.postTranslate(mPointLastX - fx, 0);
						}
					
						mTempMatrix.postTranslate(0, fy - mPointLastY);
						mTempMatrix.getValues(mTemp);
						final float fMaxY = getMaxY(mTemp);
						final float fMinY = getMinY(mTemp);
						final float fTempY = mTemp[Matrix.MTRANS_Y];
						if(fTempY >= fMinY && fTempY <= fMaxY){
							setImageMatrix(mTempMatrix);
						}
					}
				
					mPointLastX = fx;
					mPointLastY = fy;
				}
			}
			break;
		//case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_OUTSIDE:
			mScaleCenterX = -1;
			mPointLastX = -1;
			mIsScaling = false;
			mScaleDistanceLast = 0;
			break;
		}
		return true;
	}
	
	private void initScale(float fPointX1,float fPointY1,float fPointX2,float fPointY2){
		mScaleCenterX = Math.min(fPointX2, fPointX1) + Math.abs(fPointX2 - fPointX1) / 2;
		mScaleCenterY = Math.min(fPointY2, fPointY1) + Math.abs(fPointY2 - fPointY1) / 2;
		mScaleDistanceLast = getDistance(fPointX1, fPointY1, fPointX2, fPointY2);
	}
	
	private float getMinX(float f[]){
		return Math.min(mViewWidth - mBmpWidth * f[Matrix.MSCALE_X], 0);
	}
	
	private float getMaxX(float f[]){
		return Math.max(0, mViewWidth - mBmpWidth * f[Matrix.MSCALE_X]);
	}
	
	private float getMinY(float f[]){
		return Math.min(mViewHeight - mBmpHeight * f[Matrix.MSCALE_Y],0);
	}
	
	private float getMaxY(float f[]){
		return Math.max(mViewHeight - mBmpHeight * f[Matrix.MSCALE_Y],0);
	}
	
	private float getDistance(float fPointX1,float fPointY1,float fPointX2,float fPointY2){
		return FloatMath.sqrt(
				(float)Math.pow(fPointX1 - fPointX2, 2) + 
				(float)Math.pow(fPointY1 - fPointY2, 2));
	}

	@Override
	public void run() {
	}
}
