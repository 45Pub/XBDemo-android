package com.xbcx.jianhua.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.xbcx.core.Event;
import com.xbcx.im.ui.XBaseActivity;
import com.xbcx.jianhua.Departmember;
import com.xbcx.jianhua.JHEventCode;
import com.xbcx.jianhua.JHUtils;
import com.xbcx.jianhua.R;
import com.xbcx.jianhua.adapter.DepartmemberAdapter;
import com.xbcx.jianhua.adapter.OrgListAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

public class SearchOrgActivity extends XBaseActivity implements 
														View.OnClickListener,
														AdapterView.OnItemClickListener,
														DepartmemberAdapter.OnChildViewClickListener{
	
	private EditText	mEditText;
	
	private DepartmemberAdapter	mDepartmemberAdapter;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final String key = getIntent().getStringExtra("key");
		final List<Departmember> departs = (List<Departmember>)getIntent().getSerializableExtra("departs");
		
		mEditText = (EditText)findViewById(R.id.etSearch);
		findViewById(R.id.ivClear).setOnClickListener(this);
		findViewById(R.id.btnSearch).setOnClickListener(this);
		
		mEditText.setText(key);
		
		final ListView lv = (ListView)findViewById(R.id.lv);
		lv.setDivider(null);
		lv.setOnItemClickListener(this);
		mDepartmemberAdapter = new DepartmemberAdapter(this,this);
		mDepartmemberAdapter.addAll(departs);
		mDepartmemberAdapter.setIsLv1Back(true);
		mDepartmemberAdapter.setShowInfoBtn(true);
		lv.setAdapter(mDepartmemberAdapter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
		ba.mTitleTextStringId = R.string.search;
	}
	
	public static void launch(Activity activity,String key,List<Departmember> departs){
		Intent intent = new Intent(activity, SearchOrgActivity.class);
		intent.putExtra("key", key);
		Serializable seri = null;
		if(departs == null){
			departs = new ArrayList<Departmember>();
		}
		if(departs instanceof Serializable){
			seri = (Serializable)departs;
		}else{
			seri = new ArrayList<Departmember>(departs);
		}
		intent.putExtra("departs", seri);
		activity.startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if(id == R.id.ivClear){
			mEditText.setText(null);
		}else if(id == R.id.btnSearch){
			final String key = mEditText.getEditableText().toString();
			if(!TextUtils.isEmpty(key)){
				pushEvent(JHEventCode.HTTP_SearchOrg, key);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == JHEventCode.HTTP_SearchOrg){
			if(event.isSuccess()){
				List<Departmember> departs = (List<Departmember>)event.getReturnParamAtIndex(0);
				mDepartmemberAdapter.replaceAll(departs);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onChildViewClicked(OrgListAdapter adapter,Object item, int viewId,View v) {
		if(viewId == R.id.ivInfo){
			JHUtils.launchDetails(this,item);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		final Object item = parent.getItemAtPosition(position);
		if(item != null && item instanceof Departmember){
			final Departmember dm = (Departmember)item;
			if(dm.isUser()){
				JHUtils.handleUserItemClick(this,dm.getId(), dm.getName());
			}
		}
	}
}
