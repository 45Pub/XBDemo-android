package com.xbcx.im.ui.messageviewprovider;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.xbcx.im.ExpressionCoding;
import com.xbcx.im.XMessage;
import com.xbcx.im.ui.IMGlobalSetting;
import com.xbcx.im.ui.TextMessageSpanner;
import com.xbcx.library.R;

public class TextViewLeftProvider extends CommonViewProvider {

	
	public TextViewLeftProvider(OnViewClickListener listener) {
		super(listener);
	}

	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.getType() == XMessage.TYPE_TEXT){
			XMessage hm = (XMessage)message;
			return !hm.isFromSelf();
		}
		return false;
	}
	
	@Override
	protected void onSetViewHolder(View convertView, CommonViewHolder viewHolder) {
		super.onSetViewHolder(convertView, viewHolder);
		final Context context = convertView.getContext();
		TextViewHolder textViewHolder = (TextViewHolder)viewHolder;
		textViewHolder.mTextView = (TextView)LayoutInflater.from(context)
				.inflate(R.layout.message_content_text, null);
		textViewHolder.mTextView.setClickable(false);
		//textViewHolder.mTextView.setMaxWidth(
		//		XApplication.getScreenWidth() - SystemUtils.dipToPixel(context, 110));
		textViewHolder.mContentView.addView(textViewHolder.mTextView);
	}

	@Override
	protected CommonViewHolder onCreateViewHolder() {
		return new TextViewHolder();
	}

	@Override
	protected void onUpdateView(CommonViewHolder viewHolder, XMessage m) {
		TextViewHolder tViewHolder = (TextViewHolder)viewHolder;
		final String strContent = m.getContent();
		SpannableStringBuilder ssb = null;
		for(TextMessageSpanner codec : IMGlobalSetting.textMsgImageCodeces){
			ssb = codec.spanMessage(strContent);
			if(ssb != null){
				tViewHolder.mTextView.setText(ssb);
				break;
			}
		}
		if(ssb == null){
			tViewHolder.mTextView.setText(
					ExpressionCoding.spanMessage(tViewHolder.mTextView.getContext(),
					strContent, 
					0.6f,
					ImageSpan.ALIGN_BOTTOM));
		}
		
		tViewHolder.mTextView.requestLayout();
	}

	protected static class TextViewHolder extends CommonViewHolder{
		public TextView mTextView;
	}
}
