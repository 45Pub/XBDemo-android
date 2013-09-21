package com.xbcx.jianhua.activity;

import java.util.List;

import com.xbcx.core.Event;
import com.xbcx.core.XApplication;
import com.xbcx.im.IMContact;
import com.xbcx.jianhua.Departmember;
import com.xbcx.jianhua.JHEventCode;
import com.xbcx.jianhua.JHUtils;
import com.xbcx.jianhua.R;
import com.xbcx.jianhua.adapter.OrgListAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class OrgActivity extends OrgListBaseActivity implements View.OnClickListener{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TextView tvBack = (TextView)mButtonBack;
		tvBack.setText(R.string.org_parent_directory);
		tvBack.setVisibility(View.GONE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mTitleTextStringId = R.string.org;
		ba.mAddBackButton = true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected ListView addNewListViewWithAdapter(OrgListAdapter adapter) {
		mButtonBack.setVisibility(View.VISIBLE);
		return super.addNewListViewWithAdapter(adapter);
	}
	
	protected void onWillBackFirstList(){
		mButtonBack.setVisibility(View.GONE);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == JHEventCode.HTTP_SearchOrg){
			if(event.isSuccess()){
				mEditText.setText(null);
				final List<Departmember> departs = (List<Departmember>)event.getReturnParamAtIndex(0);
				SearchOrgActivity.launch(this, (String)event.getParamAtIndex(0), departs);
			}
		}
	}

	@Override
	protected void onClickUser(Object obj) {
		if(obj instanceof Departmember){
			final Departmember dm = (Departmember)obj;
			if(dm.isUser()){
				JHUtils.handleUserItemClick(this,dm.getId(), dm.getName());
			}
		}else if(obj instanceof IMContact){
			final IMContact c = (IMContact)obj;
			JHUtils.handleUserItemClick(this, c.getId(), c.getName());
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void onBackPressed() {
		if(mMultiLevelViews.size() > 1){
			if(mScroller.isFinished()){
				mIsBack = true;
				mScroller.startScroll(mListLv2MinLeftMargin,0, 
						XApplication.getScreenWidth() - mListLv2MinLeftMargin, 
						mListLv2MinLeftMargin, 1000);
				mHandler.post(mRunnableMoveList);
				
				OrgListAdapter adapter = getAdapter(mMultiLevelViews.size() - 2);
				adapter.setSelectItem(null);
				adapter.setShowInfoBtn(true);
				
				if(mMultiLevelViews.size() - 2 != 0){
					adapter.setIsLv1Back(false);
				}else{
					onWillBackFirstList();
				}
			}
		}else{
			Intent intent = new Intent(Intent.ACTION_MAIN);
	        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        intent.addCategory(Intent.CATEGORY_HOME);
	        startActivity(intent);
		}
	}
}
