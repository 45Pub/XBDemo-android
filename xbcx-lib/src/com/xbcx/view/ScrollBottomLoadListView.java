package com.xbcx.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;

import com.xbcx.library.R;
import com.xbcx.view.PullToRefreshListView;

public class ScrollBottomLoadListView extends PullToRefreshListView implements
														AbsListView.OnScrollListener,
														View.OnClickListener{

	private OnScrollListener		mScrollListener;
	
	private OnScrollBottomListener	mListener;
	
	private View					mLoadView;
	private View					mProgressBar;
	private TextView				mTextView;
	
	private boolean					mIsLoading;
	
	private boolean					mIsAutoLoad = true;
	
	private boolean					mHasMore;
	
	public ScrollBottomLoadListView(Context context) {
		super(context);
		init();
	}
	
	public ScrollBottomLoadListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init(){
		super.setOnScrollListener(this);
		
		final View footer = LayoutInflater.from(getContext()).inflate(R.layout.footer_bottomload, null);
		mProgressBar = footer.findViewById(R.id.pb);
		mTextView = (TextView)footer.findViewById(R.id.tv);
		footer.setOnClickListener(this);
		addFooterView(footer);
		
		mLoadView = footer;
	}
	
	public void setIsAutoLoad(boolean bAuto){
		mIsAutoLoad = bAuto;
		if(mIsAutoLoad){
			mProgressBar.setVisibility(View.VISIBLE);
			mTextView.setText(R.string.bottom_load_loading);
		}else{
			if(!mIsLoading){
				mProgressBar.setVisibility(View.GONE);
				mTextView.setText(R.string.bottom_load_loadmore);
			}
		}
	}
	
	public void setOnScrollBottomListener(OnScrollBottomListener listener){
		mListener = listener;
	}
	
	@Override
	public void setOnScrollListener(OnScrollListener listener){
		mScrollListener = listener;
	}
	
	public void endLoad(){
		mIsLoading = false;
		if(!mIsAutoLoad){
			mProgressBar.setVisibility(View.GONE);
		}
	}
	
	public void hideBottomView(){
		mLoadView.setVisibility(View.GONE);
	}
	
	public void hasMoreLoad(boolean bHasMore){
		mHasMore = bHasMore;
		mLoadView.setVisibility(View.VISIBLE);
		if(bHasMore){
			mProgressBar.setVisibility(View.VISIBLE);
			mTextView.setText(R.string.bottom_load_loadmore);
		}else{
			mProgressBar.setVisibility(View.GONE);
			mTextView.setText(R.string.bottom_load_nomore);
		}
	}
	
	public boolean hasMore(){
		return mHasMore;
	}
	
	public void checkBottomLoad(){
		if(!mIsLoading){
			if(getLastVisiblePosition() == getCount() - 1){
				mIsLoading = true;
				if(mListener != null){
					mListener.onBottomLoad(ScrollBottomLoadListView.this);
				}
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(mIsAutoLoad && mLoadView.getVisibility() == View.VISIBLE && mHasMore){
			if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
				checkBottomLoad();
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if(mScrollListener != null){
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}
	
	@Override
	public void onClick(View v) {
		if(!mIsAutoLoad){
			if(v == mLoadView && mHasMore){
				mProgressBar.setVisibility(View.VISIBLE);
				mIsLoading = true;
				if(mListener != null){
					mListener.onBottomLoad(this);
				}
			}
		}
	}
	
	public static interface OnScrollBottomListener{
		public void onBottomLoad(ScrollBottomLoadListView listView);
	}
}
