package com.xbcx.jianhua.activity;

import java.util.Collection;
import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.xbcx.im.IMContact;
import com.xbcx.im.IMGroup;
import com.xbcx.im.ui.simpleimpl.AddressBooksActivity;
import com.xbcx.jianhua.R;

public class AddMemberFromFriendActivity extends AddressBooksActivity implements
														CheckBox.OnCheckedChangeListener{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mGroupId = getIntent().getStringExtra("id");
		
		addImageButtonInTitleRight(R.drawable.nav_btn_confirm);
		
		CheckBox cb = (CheckBox)findViewById(R.id.cbAll);
		cb.setOnCheckedChangeListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mTitleTextStringId = R.string.add_discussion_member;
		ba.mActivityLayoutId = R.layout.activity_addmemberfromfriend;
		ba.mAddBackButton = true;
	}

	public static void launch(Activity activity,String groupId,
			String defaultUserId,String defaultUserName){
		Intent intent = new Intent(activity, AddMemberFromFriendActivity.class);
		intent.putExtra("ischeck", true);
		intent.putExtra("id", groupId);
		if(!TextUtils.isEmpty(defaultUserId)){
			intent.putExtra("defaultUserId", defaultUserId);
			intent.putExtra("defaultUserName", defaultUserName);
		}
		activity.startActivity(intent);
	}

	@Override
	protected void onTitleRightButtonClicked(View v) {
		super.onTitleRightButtonClicked(v);
		createGroupOrAddGroupMember();
	}
	
	@Override
	protected Collection<IMGroup> filterGroups(Collection<IMGroup> groups,String key){
		if(mIsCheck){
			return Collections.emptySet();
		}else{
			return super.filterGroups(groups, key);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			for(IMContact c : mContacts){
				addCheckItem(c);
			}
			mSectionAdapter.notifyDataSetChanged();
		}else{
			for(IMContact c: mContacts){
				removeCheckItem(c);
			}
			mSectionAdapter.notifyDataSetChanged();
		}
	}
}
