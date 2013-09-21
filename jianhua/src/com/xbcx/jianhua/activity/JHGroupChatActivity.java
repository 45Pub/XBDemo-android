package com.xbcx.jianhua.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.im.IMKernel;
import com.xbcx.im.ui.simpleimpl.GroupChatActivity;
import com.xbcx.jianhua.R;

public class JHGroupChatActivity extends GroupChatActivity {
	
	private View mViewTitleRight;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mViewTitleRight = addImageButtonInTitleRight(R.drawable.nav_btn_info);
		if(!IMKernel.getInstance().isSelfInGroup(mId)){
			mViewTitleRight.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public static void launch(Activity activity,String id,String name){
		Intent intent = new Intent(activity, JHGroupChatActivity.class);
		intent.putExtra(EXTRA_ID, id);
		intent.putExtra(EXTRA_NAME, name);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(intent);
	}

	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == EventCode.IM_GroupChatListChanged){
			if(IMKernel.getInstance().isSelfInGroup(mId)){
				mViewTitleRight.setVisibility(View.VISIBLE);
			}else{
				mViewTitleRight.setVisibility(View.GONE);
			}
		}
	}

	@Override
	protected void onTitleRightButtonClicked(View v) {
		super.onTitleRightButtonClicked(v);
		GroupChatInfoActivity.launch(this,mId);
	}
}
