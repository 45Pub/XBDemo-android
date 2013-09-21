package com.xbcx.jianhua.activity;

import java.util.List;

import com.xbcx.core.Event;
import com.xbcx.core.IDObject;
import com.xbcx.jianhua.Departmember;
import com.xbcx.jianhua.JHEventCode;
import com.xbcx.jianhua.R;
import com.xbcx.jianhua.adapter.DepartmemberAdapter;
import com.xbcx.jianhua.adapter.OrgListAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;

public class AddMemberFromOrgActivity extends OrgListBaseActivity {
	
	private ListView	mListViewSearch;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mIsCheck = true;
		super.onCreate(savedInstanceState);
		
		addTextButtonInTitleRight(R.string.cancel);
		
		mListViewSearch = (ListView)findViewById(R.id.lvSearch);
		mListViewSearch.setDivider(null);
		mListViewSearch.setVisibility(View.GONE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
		ba.mTitleTextStringId = R.string.add_discussion_member;
	}

	public static void launch(Activity activity){
		Intent intent = new Intent(activity,AddMemberFromOrgActivity.class);
		activity.startActivity(intent);
	}
	
	public static void launch(Activity activity,String id,String defaultUserId,String defaultUserName){
		Intent intent = new Intent(activity,AddMemberFromOrgActivity.class);
		if(id != null){
			intent.putExtra("id", id);
		}
		if(defaultUserId != null){
			intent.putExtra("defaultUserId", defaultUserId);
		}
		if(defaultUserName != null){
			intent.putExtra("defaultUserName", defaultUserName);
		}
		
		activity.startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		if(mListViewSearch.getVisibility() == View.VISIBLE){
			mListViewSearch.setAdapter(null);
			mListViewSearch.setVisibility(View.GONE);
			mLayoutListView.setVisibility(View.VISIBLE);
			if(mMultiLevelViews.size() == 1){
				setBackButtonText(R.string.back);
			}else{
				setBackButtonText(R.string.org_parent_directory);
			}
		}else{
			super.onBackPressed();
		}
	}
	
	protected void setBackButtonText(int resId){
		TextView tv = (TextView)mButtonBack;
		tv.setText(resId);
	}

	@Override
	protected void onTitleRightButtonClicked(View v) {
		super.onTitleRightButtonClicked(v);
		finish();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == JHEventCode.HTTP_SearchOrg){
			dismissProgressDialog();
			if(event.isSuccess()){
				final List<Departmember> departs = (List<Departmember>)event.getReturnParamAtIndex(0);
				DepartmemberAdapter adapter = new DepartmemberAdapter(this, this);
				adapter.setShowInfoBtn(true);
				adapter.setIsLv1Back(true);
				adapter.setOnCheckCallback(this);
				adapter.setIsCheck(true);
				adapter.addAll(departs);
				mListViewSearch.setVisibility(View.VISIBLE);
				mListViewSearch.setAdapter(adapter);
				mLayoutListView.setVisibility(View.GONE);
				setBackButtonText(R.string.back);
			}
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
		}
	}

	@Override
	protected void onWillBackFirstList() {
		super.onWillBackFirstList();
		((TextView)mButtonBack).setText(R.string.back);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected ListView addNewListViewWithAdapter(OrgListAdapter adapter) {
		((TextView)mButtonBack).setText(R.string.org_parent_directory);
		return super.addNewListViewWithAdapter(adapter);
	}

	@Override
	protected void onClickUser(Object obj) {
		IDObject ido = (IDObject)obj;
		final String id = ido.getId();
		if(mMapCheckUsers.containsKey(id)){
			removeCheckUser(id,true);
		}else{
			String name = "";
			if(obj instanceof Departmember){
				name = ((Departmember)obj).getName();
			}
			addCheckUser(id,name ,true);
		}
		notifyLastTwoAdapter();
	}
}
