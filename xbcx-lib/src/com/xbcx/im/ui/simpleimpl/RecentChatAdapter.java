package com.xbcx.im.ui.simpleimpl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.xbcx.im.ExpressionCoding;
import com.xbcx.im.RecentChat;
import com.xbcx.im.VCardProvider;
import com.xbcx.im.ui.LocalAvatar;
import com.xbcx.library.R;
import com.xbcx.utils.DateUtils;

import android.content.Context;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class RecentChatAdapter extends AbsBaseAdapter<RecentChat> {
	
	private static final SimpleDateFormat DATEFORMAT_HM 	= new SimpleDateFormat("H:mm",Locale.getDefault());
	private static SimpleDateFormat DATEFORMAT_MD;
	private static SimpleDateFormat DATEFORMAT_YMD;
	
	public RecentChatAdapter(Context context){
		super(context);
	}
	
	@Override
	protected View onCreateConvertView(){
		return LayoutInflater.from(mContext).inflate(R.layout.adapter_recentchat, null);
	}
	
	@Override
	protected ViewHolder onCreateViewHolder() {
		return new RcViewHolder();
	}

	@Override
	protected void onSetViewHolder(ViewHolder viewHolder,View convertView) {
		RcViewHolder rViewHolder = (RcViewHolder)viewHolder;
		viewHolder = new ViewHolder();
		rViewHolder.mImageViewAvatar = (ImageView)convertView.findViewById(R.id.ivAvatar);
		rViewHolder.mTextViewName = (TextView)convertView.findViewById(R.id.tvName);
		rViewHolder.mTextViewTime = (TextView)convertView.findViewById(R.id.tvTime);
		rViewHolder.mTextViewMessage = (TextView)convertView.findViewById(R.id.tvMessage);
		rViewHolder.mTextViewUnreadMessageCount = (TextView)convertView.findViewById(R.id.tvNumber);
	}

	@Override
	protected void onSetChildViewTag(ViewHolder viewHolder,Object item) {
		
	}

	@Override
	protected void onUpdateView(ViewHolder viewHolder,Object item, int position) {
		RcViewHolder rViewHolder = (RcViewHolder)viewHolder;
		RecentChat recentChat = (RecentChat)item;
		
		final int nUnreadMessageCount = recentChat.getUnreadMessageCount();
		if(nUnreadMessageCount > 0){
			rViewHolder.mTextViewUnreadMessageCount.setVisibility(View.VISIBLE);
			rViewHolder.mTextViewUnreadMessageCount.setText(String.valueOf(nUnreadMessageCount));
		}else{
			rViewHolder.mTextViewUnreadMessageCount.setVisibility(View.GONE);
		}
		
		onSetAvatar(rViewHolder.mImageViewAvatar, recentChat);
		
		rViewHolder.mTextViewName.setText(recentChat.getName());
		rViewHolder.mTextViewTime.setText(getSendTimeShow(recentChat.getTime()));
		rViewHolder.mTextViewMessage.setText(ExpressionCoding.spanMessage(mContext,
				recentChat.getContent(), 0.6f,ImageSpan.ALIGN_BOTTOM));
	}
	
	protected void onSetAvatar(ImageView iv,RecentChat rc){
		if(rc.isLocalAvatar()){
			try{
				iv.setImageResource(LocalAvatar.getAvatarResId(rc.getLocalAvatar()));
			}catch(Exception e){
				iv.setImageBitmap(null);
			}
		}else{
			iv.setImageBitmap(VCardProvider.getInstance().loadAvatar(rc.getId()));
		}
	}
	
	protected String getSendTimeShow(long lSendTime){
		if(lSendTime == 0){
			return "";
		}
		String strRet = null;
		try {
			Date date = new Date(lSendTime);
			if(DateUtils.isToday(lSendTime)){
				strRet = DATEFORMAT_HM.format(date);
			}else if(DateUtils.isInCurrentYear(lSendTime)){
				if(DATEFORMAT_MD == null){
					DATEFORMAT_MD = new SimpleDateFormat(
							mContext.getString(R.string.dateformat_md),Locale.getDefault());
				}
				strRet = DATEFORMAT_MD.format(date);
			}else{
				if(DATEFORMAT_YMD == null){
					DATEFORMAT_YMD = new SimpleDateFormat(
							mContext.getString(R.string.dateformat_ymd),Locale.getDefault());
				}
				strRet = DATEFORMAT_YMD.format(date);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return strRet;
	}

	protected static class RcViewHolder extends ViewHolder{
		public ImageView 	mImageViewAvatar;
		
		public TextView		mTextViewName;
		
		public TextView		mTextViewTime;
		
		public TextView		mTextViewMessage;
		
		public TextView		mTextViewUnreadMessageCount;
	}
}
