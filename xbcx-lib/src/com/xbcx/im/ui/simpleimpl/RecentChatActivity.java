package com.xbcx.im.ui.simpleimpl;

import java.util.Collection;
import java.util.List;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.im.RecentChat;
import com.xbcx.im.RecentChatManager;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.im.ui.XBaseActivity;
import com.xbcx.library.R;

public class RecentChatActivity extends XBaseActivity implements 
												OnItemClickListener,
												OnItemLongClickListener{
	protected static final int 	MENUID_DELETE_RECORD = 1;
	
	protected ListView			mListView;
	protected RecentChatAdapter	mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mListView = (ListView)findViewById(R.id.lv);
		mListView.setDivider(null);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		mAdapter = onCreateRecentChatAdapter();
		mListView.setAdapter(mAdapter);
		
		registerForContextMenu(mListView);
		
		mAdapter.addAll(onFilterRecentChats(
				RecentChatManager.getInstance().getAllRecentChat()));
		
		addAndManageEventListener(EventCode.RecentChatChanged);
		addAndManageEventListener(EventCode.UnreadMessageCountChanged);
		
		addAndManageEventListener(EventCode.DownloadAvatar);
		addAndManageEventListener(EventCode.IM_LoadVCard);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onSetParam() {
		super.onSetParam();
		mTitleShowConnectState = true;
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mActivityLayoutId = R.layout.activity_recentchat;
	}
	
	protected RecentChatAdapter	onCreateRecentChatAdapter(){
		return new RecentChatAdapter(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == EventCode.RecentChatChanged){
			final List<RecentChat> rcs = (List<RecentChat>)event.getParamAtIndex(0);
			mAdapter.replaceAll(onFilterRecentChats(rcs));
		}else if(code == EventCode.UnreadMessageCountChanged){
			mAdapter.notifyDataSetChanged();
		}else if(code == EventCode.DownloadAvatar){
			mAdapter.notifyDataSetChanged();
		}else if(code == EventCode.IM_LoadVCard){
			mAdapter.notifyDataSetChanged();
		}
	}
	
	protected Collection<RecentChat> onFilterRecentChats(Collection<RecentChat> rcs){
		return rcs;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Object object = parent.getItemAtPosition(position);
		if(object != null && object instanceof RecentChat){
			RecentChat recentChat = (RecentChat)object;
			setTag(recentChat);
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Object obj = parent.getItemAtPosition(position);
		if(obj != null){
			if(obj instanceof RecentChat){
				RecentChat rc = (RecentChat)obj;
				ActivityType.launchChatActivity(this, rc.getActivityType(), rc.getId(), rc.getName());
			}
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Object object = getTag();
		if(object != null && object instanceof RecentChat){
			RecentChat recentChat = (RecentChat)object;
			menu.setHeaderTitle(recentChat.getName());
			
			menu.add(0, MENUID_DELETE_RECORD, 0, R.string.delete_record);
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(item.getItemId() == MENUID_DELETE_RECORD){
			Object tag = getTag();
			if(tag != null && tag instanceof RecentChat){
				final RecentChat recentChat = (RecentChat)tag;
				RecentChatManager.getInstance().deleteRecentChat(recentChat.getId());
				pushEvent(EventCode.DB_DeleteMessage, recentChat.getId());
			}
		}
		return super.onContextItemSelected(item);
	}
}
