package com.xbcx.im.ui;

import com.xbcx.adapter.CommonPagerAdapter;
import com.xbcx.adapter.SetBaseAdapter;
import com.xbcx.im.ExpressionCoding;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;
import com.xbcx.view.PageIndicator;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

public class EditViewQQExpressionProvider extends EditViewExpressionProvider implements
														ViewPager.OnPageChangeListener,
														AdapterView.OnItemClickListener{
	
	protected ViewPager			mViewPagerExpression;
	protected PageIndicator 	mPageIndicatorExpression;
	
	@Override
	public View createTabButton(Context context) {
		return null;
	}

	@Override
	public boolean isTabSeletable() {
		return true;
	}

	@Override
	public View createTabContent(Context context) {
		View v = LayoutInflater.from(context).inflate(R.layout.editview_qqexpression, null);
		mViewPagerExpression = (ViewPager)v.findViewById(R.id.vpExpression);
		mPageIndicatorExpression = (PageIndicator)v.findViewById(R.id.pageIndicator);
		mViewPagerExpression.setOnPageChangeListener(this);
		CommonPagerAdapter pagerAdapter = onCreatePagerAdapter(context);
		final int nResIds[] = getExpressionResIds();
		int nPageCount = nResIds.length / getOnePageMaxCount();
		if(nResIds.length % getOnePageMaxCount() > 0){
			++nPageCount;
		}
		pagerAdapter.setPageCount(nPageCount);
		mPageIndicatorExpression.setSelectColor(0xff6f8536);
		mPageIndicatorExpression.setNormalColor(0xffafafaf);
		mPageIndicatorExpression.setPageCount(nPageCount);
		mPageIndicatorExpression.setPageCurrent(0);
		
		mViewPagerExpression.setAdapter(pagerAdapter);
		
		return v;
	}
	
	protected CommonPagerAdapter onCreatePagerAdapter(Context context){
		return new ExpressionPagerAdapter(context);
	}
	
	protected int	getOnePageMaxCount(){
		return 23;
	}
	
	protected int[] getExpressionResIds(){
		return ExpressionCoding.getExpressionResIds();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Integer resid = (Integer)parent.getItemAtPosition(position);
		if(resid.intValue() == 0){
			int nIndex = mEditText.getSelectionStart();
			if(nIndex > 0){
				final Editable editable = mEditText.getEditableText();
				ImageSpan[] spans = editable.getSpans(0, mEditText.length(), ImageSpan.class);
				final int length = spans.length;
				boolean bDelete = false;
				for (int i = 0; i < length; i++) {
					final int s = editable.getSpanStart(spans[i]);
					final int e = editable.getSpanEnd(spans[i]);
					if (e == nIndex) {
						editable.delete(s, e);
						bDelete = true;
						break;
					}
				}
				if(!bDelete){
					editable.delete(nIndex - 1, nIndex);
				}
			}
		}else{
			try{
				SpannableStringBuilder ssb = new SpannableStringBuilder(
						ExpressionCoding.getCodingByResId(resid));
				
				Drawable d = parent.getContext().getResources().getDrawable(resid);
				d.setBounds(0, 0, (int)(d.getIntrinsicWidth() * 0.6), 
						(int)(d.getIntrinsicHeight() * 0.6));
				ssb.setSpan(new ImageSpan(d),
						0, ssb.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
				mEditText.append(ssb);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		mPageIndicatorExpression.setPageCurrent(position);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}
	
	protected class ExpressionPagerAdapter extends CommonPagerAdapter{
		
		public Context	mContext;
		
		public ExpressionPagerAdapter(Context context){
			mContext = context;
		}
		
		@Override
		protected View getView(View v, int nPos) {
			ExpressionImageAdapter adapter;
			if(v == null){
				final Context context = mContext;
				final GridView gridView = new GridView(context);
				gridView.setColumnWidth(SystemUtils.dipToPixel(context, 32));
				gridView.setNumColumns(8);
				gridView.setVerticalSpacing(SystemUtils.dipToPixel(context, 10));
				gridView.setCacheColorHint(0x00000000);
				gridView.setSelector(new ColorDrawable(0x00000000));
				gridView.setStretchMode(GridView.STRETCH_SPACING);
				final int nPaddingHorizontal = SystemUtils.dipToPixel(context, 10);
				gridView.setPadding(nPaddingHorizontal, nPaddingHorizontal, nPaddingHorizontal, 0);
				
				adapter = new ExpressionImageAdapter(context);
				gridView.setAdapter(adapter);
				gridView.setOnItemClickListener(EditViewQQExpressionProvider.this);
				gridView.setTag(adapter);
				
				v = gridView;
			}else{
				adapter = (ExpressionImageAdapter)v.getTag();
			}
			
			final int nResIds[] = getExpressionResIds();
			final int nStart = nPos * getOnePageMaxCount();
			int nEnd = nStart + getOnePageMaxCount();
			if(nEnd > nResIds.length)nEnd = nResIds.length;
			
			adapter.clear();
			for(int nIndex = nStart;nIndex < nEnd;++nIndex){
				adapter.addItem(Integer.valueOf(nResIds[nIndex]));
			}
			adapter.addItem(0);
			
			return v;
		}
	}

	protected static class ExpressionImageAdapter extends SetBaseAdapter<Integer>{

		private Context mContext;
		
		public ExpressionImageAdapter(Context context){
			mContext = context;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				final int nSize = SystemUtils.dipToPixel(mContext, 32);
				convertView = new ImageView(mContext);
				convertView.setLayoutParams(new GridView.LayoutParams(nSize,nSize));
			}
			
			Integer id = (Integer)getItem(position);
			final ImageView imageView = (ImageView)convertView;
			if(id.intValue() == 0){
				imageView.setImageResource(R.drawable.emotion_del);
			}else{
				imageView.setImageResource(id.intValue());
			}
			
			return imageView;
		}
	}
}
