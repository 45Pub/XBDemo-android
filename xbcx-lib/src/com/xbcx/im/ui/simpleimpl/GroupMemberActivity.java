package com.xbcx.im.ui.simpleimpl;

import java.util.ArrayList;
import java.util.Collection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.im.IMContact;
import com.xbcx.im.IMGroup;
import com.xbcx.im.IMKernel;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.im.ui.XBaseActivity;
import com.xbcx.library.R;

public class GroupMemberActivity extends XBaseActivity implements 
													AdapterView.OnItemClickListener,
													OnChildViewClickListener{

	protected String				mId;
	protected IMGroup				mGroup;
	
	protected TextView				mTextViewTitleRight;
	
	protected ListView				mListView;
	protected IMGroupMemberAdapter	mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mId = getIntent().getStringExtra("id");
		mGroup = IMKernel.getInstance().getGroup(mId);
		super.onCreate(savedInstanceState);
		
		mListView = (ListView)findViewById(R.id.lv);
		mListView.setDivider(null);
		mListView.setOnItemClickListener(this);
		mAdapter = new IMGroupMemberAdapter(this);
		mAdapter.setOnChildViewClickListener(this);
		if(mGroup != null){
			mAdapter.addAll(mGroup.getMembers());
			if(IMGroup.ROLE_ADMIN.equals(mGroup.getMemberRole(IMKernel.getLocalUser()))){
				mTextViewTitleRight = (TextView)addTextButtonInTitleRight(R.string.delete);
			}
		}
		mListView.setAdapter(mAdapter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
		if(mGroup == null){
			ba.mTitleTextStringId = R.string.group_member;
		}else{
			ba.mTitleText = getString(R.string.group_member) + 
					"(" + mGroup.getMemberCount() + getString(R.string.people) + ")";
		}
	}

	public static void launch(Activity activity,String groupId){
		Intent intent = new Intent(activity, GroupMemberActivity.class);
		intent.putExtra("id", groupId);
		activity.startActivity(intent);
	}

	@Override
	protected void onTitleRightButtonClicked(View v) {
		super.onTitleRightButtonClicked(v);
		if(mTextViewTitle != null){
			mAdapter.setIsEdit(!mAdapter.isEdit());
			if(mAdapter.isEdit()){
				mTextViewTitleRight.setText(R.string.complete);
			}else{
				mTextViewTitleRight.setText(R.string.delete);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		final Object item = parent.getItemAtPosition(position);
		if(item != null && item instanceof IMContact){
			final IMContact contact = (IMContact)item;
			if(!IMKernel.isLocalUser(contact.getId())){
				ActivityType.launchChatActivity(this, ActivityType.UserDetailActivity,
						contact.getId(),contact.getName());
			}
		}
	}

	@Override
	public void onChildViewClicked(BaseAdapter adapter, Object item, int viewId, View v) {
		if(viewId == R.id.btnDelete){
			final IMContact c = (IMContact)item;
			Collection<String> ids = new ArrayList<String>();
			ids.add(c.getId());
			pushEvent(EventCode.IM_DeleteGroupChatMember,mId,ids);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == EventCode.IM_DeleteGroupChatMember){
			if(event.isSuccess()){
				final Collection<String> ids = (Collection<String>)event.getParamAtIndex(1);
				for(String id : ids){
					mAdapter.removeItemById(id);
				}
			}
		}
	}
}
