package com.xbcx.jianhua.activity;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.im.RecentChatManager;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.jianhua.JHEventCode;
import com.xbcx.jianhua.R;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity implements OnEventListener{
	
	private TextView	mTextViewNumber;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		mTextViewNumber = (TextView)findViewById(R.id.tvNumber);
		
		addTab(MessageActivity.class, R.drawable.selector_btn_tab_msg);
		addTab(FriendActivity.class, R.drawable.selector_btn_tab_friend);
		addTab(OrgActivity.class, R.drawable.selector_btn_tab_addressbook);
		addTab(SetupActivity.class, R.drawable.selector_btn_tab_setup);
		
		processIntent(getIntent());
		
		updateUnreadMessageTotalCount();
		
		AndroidEventManager.getInstance().addEventListener(EventCode.UnreadMessageCountChanged, this, false);
		
		AndroidEventManager.getInstance().runEvent(JHEventCode.MainActivityLaunched);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		AndroidEventManager.getInstance().removeEventListener(EventCode.UnreadMessageCountChanged, this);
	}

	public static void launch(Activity activity){
		Intent intent = new Intent(activity, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		processIntent(intent);
	}
	
	private void processIntent(Intent intent){
		final String strJumpId = intent.getStringExtra("id");
		final String strJumpName = intent.getStringExtra("name");
		final int activity = intent.getIntExtra("activity", -1);
		if(!TextUtils.isEmpty(strJumpId)){
			getTabHost().setCurrentTab(0);
			ActivityType.launchChatActivity(this, activity, strJumpId, strJumpName);
		}
	}

	private void addTab(Class<?> cls,int nResId){
		final TabHost tabHost = getTabHost();
		final TabSpec tabSpec = tabHost.newTabSpec(cls.getName());
		
		final ImageView imageView = new ImageView(this);
		imageView.setBackgroundResource(nResId);
		Intent intent = new Intent(this,cls);
		tabSpec.setIndicator(imageView).setContent(intent);
		tabHost.addTab(tabSpec);
	}

	@Override
	public void onEventRunEnd(Event event) {
		final int code = event.getEventCode();
		if(code == EventCode.UnreadMessageCountChanged){
			updateUnreadMessageTotalCount();
		}
	}
	
	protected void updateUnreadMessageTotalCount(){
		final int count = RecentChatManager.getInstance().getUnreadMessageTotalCount();
		if(count > 0){
			mTextViewNumber.setVisibility(View.VISIBLE);
			mTextViewNumber.setText(String.valueOf(count));
		}else{
			mTextViewNumber.setVisibility(View.GONE);
		}
	}
}