package com.xbcx.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TabWidget;

public class TabWidgetEx extends TabWidget {

	private int mCurrentTab = -1;
	
	private OnTabSelectionChanged mSelectionChangedListener;
	
	public TabWidgetEx(Context context) {
		super(context);
	}
	
	public TabWidgetEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public TabWidgetEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
	
	public void setOnTabSelectionChangedListener(OnTabSelectionChanged listener){
		mSelectionChangedListener = listener;
	}
	
	@Override
	public void setCurrentTab(int index) {
		super.setCurrentTab(index);
		
		if(mCurrentTab != index){
			mCurrentTab = index;
			if(mSelectionChangedListener != null){
				mSelectionChangedListener.onTabSelectionChanged(mCurrentTab, false);
			}
		}
	}
	
	public int getCurrentTab(){
		return mCurrentTab;
	}

	@Override
	public void addView(View child) {
		super.addView(child);
		
		child.setOnClickListener(new TabClickListener(getTabCount() - 1));
	}
	
	@Override
	public void addView(View child, android.view.ViewGroup.LayoutParams params) {
		super.addView(child, params);
		child.setOnClickListener(new TabClickListener(getTabCount() - 1));
	}
	
	// registered with each tab indicator so we can notify tab host
    private class TabClickListener implements OnClickListener {

        private final int mTabIndex;

        private TabClickListener(int tabIndex) {
            mTabIndex = tabIndex;
        }

        public void onClick(View v) {
        	if(mCurrentTab != mTabIndex){
        		mCurrentTab = mTabIndex;
        		if(mSelectionChangedListener != null){
            		mSelectionChangedListener.onTabSelectionChanged(mTabIndex, true);
            	}
        	}
        }
    }

	public static interface OnTabSelectionChanged {
        void onTabSelectionChanged(int tabIndex, boolean clicked);
    }
}
