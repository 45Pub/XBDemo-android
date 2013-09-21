package com.xbcx.jianhua.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.XApplication;
import com.xbcx.im.IMContact;
import com.xbcx.im.IMGroup;
import com.xbcx.jianhua.BaseInfo;
import com.xbcx.jianhua.Departmember;
import com.xbcx.jianhua.JHApplication;
import com.xbcx.jianhua.JHEventCode;
import com.xbcx.jianhua.JHUtils;
import com.xbcx.jianhua.R;
import com.xbcx.jianhua.adapter.DepartmemberAdapter;
import com.xbcx.jianhua.adapter.OrgDepartmemberAdapter;
import com.xbcx.jianhua.adapter.OrgGroupAdapter;
import com.xbcx.jianhua.adapter.OrgGroupMemberAdapter;
import com.xbcx.jianhua.adapter.OrgListAdapter;
import com.xbcx.jianhua.adapter.RootOrgAdapter;
import com.xbcx.jianhua.adapter.RootOrgAdapter.PluginItem;
import com.xbcx.utils.SystemUtils;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class OrgListBaseActivity extends UserChooseBaseActivity implements 
												AdapterView.OnItemClickListener,
												DepartmemberAdapter.OnChildViewClickListener{

	private static final int PLUGID_GROUP 		= 1;
	
	private RootOrgAdapter		mRootAdapter;
	
	protected boolean			mIsCheck;
	
	protected RelativeLayout	mLayoutListView;
	protected View				mViewShadow;
	
	protected EditText			mEditText;
	
	protected View				mViewSearch;
	
	protected List<View>		mMultiLevelViews = new ArrayList<View>();
	
	protected Scroller			mScroller;
	protected Handler				mHandler;
	protected int					mListLv2MinLeftMargin;
	protected int					mShadowOffset;
	protected boolean				mIsClickLv1;
	protected boolean				mIsBack;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mViewSearch = findViewById(R.id.viewSearch);
		final BaseInfo bi = JHApplication.getBaseInfo();
		if(BaseInfo.ROLE_INNER == bi.getRole()){
		}else{
			mToastManager.show(R.string.toast_org_not_use);
			mViewSearch.setVisibility(View.GONE);
		}
		
		mLayoutListView = (RelativeLayout)findViewById(R.id.layoutListView);
		mViewShadow = findViewById(R.id.viewShadow);
		
		findViewById(R.id.ivClear).setOnClickListener(this);
		findViewById(R.id.btnSearch).setOnClickListener(this);
		mEditText = (EditText)findViewById(R.id.etSearch);
		
		setViewShadowLeftMargin(XApplication.getScreenWidth());
		
		ListView listView = (ListView)findViewById(R.id.lv);
		listView.setOnItemClickListener(this);
		listView.setDivider(null);
		mRootAdapter = new RootOrgAdapter(this,this);
		mRootAdapter.setIsLv1Back(true);
		mRootAdapter.setShowInfoBtn(true);
		mRootAdapter.setIsCheck(mIsCheck);
		mRootAdapter.setOnCheckCallback(this);
		listView.setAdapter(mRootAdapter);
		listView.setTag(mRootAdapter);
		mMultiLevelViews.add(listView);
		
		mScroller = new Scroller(this);
		mHandler = XApplication.getMainThreadHandler();
		mListLv2MinLeftMargin = SystemUtils.dipToPixel(this, 115);
		mShadowOffset = SystemUtils.dipToPixel(this, 7);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		final BaseInfo bi = JHApplication.getBaseInfo();
		if(BaseInfo.ROLE_INNER == bi.getRole()){
			pushEvent(JHEventCode.HTTP_GetOrg, "0");
		}else{
			mToastManager.show(R.string.toast_org_not_use);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	protected Runnable 	mRunnableMoveList = new Runnable() {
		@Override
		public void run() {
			if(mScroller.computeScrollOffset()){
				if(mIsBack){
					final View lv1 = mMultiLevelViews.get(mMultiLevelViews.size() - 2);
					final View lv2 = mMultiLevelViews.get(mMultiLevelViews.size() - 1);
					RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)
							lv2.getLayoutParams();
					lp.leftMargin = mScroller.getCurrX();
					lv2.setLayoutParams(lp);
					
					if(mMultiLevelViews.size() == 2){
						lp = (RelativeLayout.LayoutParams)lv1.getLayoutParams();
						lp.rightMargin = XApplication.getScreenWidth() - mScroller.getCurrX();
						lv1.setLayoutParams(lp);
						
						setViewShadowLeftMargin(mScroller.getCurrX() - mShadowOffset);
					}else{
						lp = (RelativeLayout.LayoutParams)lv1.getLayoutParams();
						lp.leftMargin = mScroller.getCurrY();
						lp.rightMargin = 0;
						lv1.setLayoutParams(lp);
						
						setViewShadowLeftMargin(mScroller.getCurrY() - mShadowOffset);
					}
					
					mHandler.post(this);
				}else{
					if(mIsClickLv1){
						final View lv1 = mMultiLevelViews.get(mMultiLevelViews.size() - 2);
						final View lv2 = mMultiLevelViews.get(mMultiLevelViews.size() - 1);
						final int curX = mScroller.getCurrX();
						RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)
								lv2.getLayoutParams();
						lp.leftMargin = curX;
						lv2.setLayoutParams(lp);
						
						lp = (RelativeLayout.LayoutParams)lv1.getLayoutParams();
						lp.rightMargin = XApplication.getScreenWidth() - curX;
						lv1.setLayoutParams(lp);
						
						setViewShadowLeftMargin(curX - mShadowOffset);
						
						mHandler.post(this);
					}else{
						final View lv2 = mMultiLevelViews.get(mMultiLevelViews.size() - 2);
						final View lv3 = mMultiLevelViews.get(mMultiLevelViews.size() - 1);
						RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)
								lv2.getLayoutParams();
						lp.leftMargin = mScroller.getCurrX();
						lp.rightMargin = XApplication.getScreenWidth() - mScroller.getCurrY();
						lv2.setLayoutParams(lp);
						
						lp = (RelativeLayout.LayoutParams)lv3.getLayoutParams();
						lp.leftMargin = mScroller.getCurrY();
						lv3.setLayoutParams(lp);
						
						setViewShadowLeftMargin(mScroller.getCurrY() - mShadowOffset);
						
						mHandler.post(this);
					}
				}
			}else{
				if(mIsBack){
					removeMultiLevelView(mMultiLevelViews.size() - 1);
					mIsBack = false;
				}
			}
		}
	};
	
	protected void removeMultiLevelView(int index){
		View view = mMultiLevelViews.get(index);
		mLayoutListView.removeView(view);
		mMultiLevelViews.remove(index);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(mMultiLevelViews.size() > 1){
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(mMultiLevelViews.size() > 1){
				onBackPressed();
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
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
			super.onBackPressed();
		}
	}
	
	protected void onWillBackFirstList(){
		
	}

	protected void setViewShadowLeftMargin(int margin){
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)mViewShadow.getLayoutParams();
		lp.leftMargin = margin;
		mViewShadow.setLayoutParams(lp);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		final int id = v.getId();
		if(id == R.id.ivClear){
			mEditText.setText(null);
		}else if(id == R.id.btnSearch){
			final String key = mEditText.getEditableText().toString();
			if(!TextUtils.isEmpty(key)){
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
				pushEvent(JHEventCode.HTTP_SearchOrg, key);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		if(mScroller.isFinished()){
			mIsBack = false;
			
			RelativeLayout.LayoutParams lp = 
					(RelativeLayout.LayoutParams)parent.getLayoutParams();
			if(lp.leftMargin == 0){
				mIsClickLv1 = true;
			}else{
				mIsClickLv1 = false;
			}
			
			Object item = parent.getItemAtPosition(position);
			OrgListAdapter adapter = getAdapter(mMultiLevelViews.indexOf(parent));
			if(item.equals(adapter.getSelectItem())){
				return;
			}
			
			if(item instanceof Departmember){
				Departmember dm = (Departmember)item;
					
				if(dm.isUser()){
					onClickUser(dm);
				}else{
					pushEvent(JHEventCode.HTTP_GetOrg, dm.getId(),dm,adapter);
				}
			}else{
				onHandleClickOtherItem(item,adapter);
			}
		}
	}
	
	protected void onClickUser(Object obj){
		
	}
	
	@SuppressWarnings("rawtypes")
	protected void onHandleClickOtherItem(Object obj,OrgListAdapter adapter){
		if(obj instanceof PluginItem){
			PluginItem pItem = (PluginItem)obj;
			if(pItem.mPluginId == PLUGID_GROUP){
				pushEvent(EventCode.IM_GetGroupChatList, pItem);
			}
		}else if(obj instanceof IMGroup){
			final IMGroup group = (IMGroup)obj;
			showGroupMember(group, adapter, false);
		}else if(obj instanceof IMContact){
			onClickUser(obj);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == JHEventCode.HTTP_GetOrg){
			final String id = (String)event.getParamAtIndex(0);
			if("0".equals(id)){
				if(event.isSuccess()){
					mRootAdapter.clear();
					//mRootAdapter.addPluginItem(new PluginItem(PLUGID_GROUP, 
					//		R.drawable.multilevel_group, R.string.groups));
					List<Departmember> departs = 
							(List<Departmember>)event.getReturnParamAtIndex(0);
					DepartmemberAdapter adapter = (DepartmemberAdapter)getAdapter(0);
					adapter.replaceAll(departs);
				}else{
					mToastManager.show(R.string.toast_disconnect);
				}
			}else{
				if(event.isSuccess()){
					Object arg1 = event.getParamAtIndex(1);
					Object arg2 = event.getParamAtIndex(2);
					if(arg1 != null && arg2 != null && 
							arg1 instanceof Departmember && 
							arg2 instanceof DepartmemberAdapter){
						final Departmember dm = (Departmember)arg1;
						final DepartmemberAdapter adapter = (DepartmemberAdapter)arg2;
						final List<Departmember> departs = (List<Departmember>)event.getReturnParamAtIndex(0);
						
						mMapIdToDepartChilds.put(dm.getId(), departs);
						
						Object arg3 = event.getParamAtIndex(3);
						if(arg3 != null && arg3 instanceof Boolean){
							if(((Boolean)arg3).booleanValue()){
								addCheckDepartmember(dm.getId());
							}
						}
						
						if(isMultiViewAdapter(adapter)){
							boolean bHasSelect = adapter.getSelectItem() != null;
							adapter.setSelectItem(dm);
							
							if(mIsClickLv1){
								if(bHasSelect){
									OrgListAdapter adapter2 = getAdapter(mMultiLevelViews.size() - 1);
									if(adapter2 instanceof DepartmemberAdapter){
										DepartmemberAdapter dAdapter = (DepartmemberAdapter)adapter2;
										dAdapter.replaceAll(departs);
									}else{
										while(mMultiLevelViews.size() > 2){
											removeMultiLevelView(mMultiLevelViews.size() - 1);
										}
										DepartmemberAdapter dAdapter = new OrgDepartmemberAdapter(this,this);
										setNewOrgAdapterAttr(dAdapter);
										dAdapter.replaceAll(departs);
										changeAdapter(mMultiLevelViews.size() - 1, dAdapter);
									}
								}else{
									addNewListView(departs);
								}
							}else{
								addNewListView(departs);
							}
						}else{
							notifyLastTwoAdapter();
						}
					}
				}
			}
		}else if(code == EventCode.IM_GetGroupChatList){
			if(event.isSuccess()){
				final Object arg0 = event.getParamAtIndex(0);
				if(arg0 != null && arg0 instanceof PluginItem){
					final PluginItem pItem = (PluginItem)arg0;
					boolean bHasSelect = mRootAdapter.getSelectItem() != null;
					
					if(pItem != null){
						mRootAdapter.setSelectItem(pItem);
					}
					
					final Collection<IMGroup> groups = (Collection<IMGroup>)event.getReturnParamAtIndex(0);
					OrgGroupAdapter adapter = new OrgGroupAdapter(this,this);
					adapter.addAll(groups);
					
					if(bHasSelect){
						setNewOrgAdapterAttr(adapter);
						changeAdapter(mMultiLevelViews.size() - 1, adapter);
					}else{
						addNewListViewWithAdapter(adapter);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected void	setNewOrgAdapterAttr(OrgListAdapter adapter){
		adapter.setShowInfoBtn(true);
		adapter.setIsCheck(mIsCheck);
		adapter.setOnChildViewClickListener(this);
		adapter.setOnCheckCallback(this);
	}
	
	@SuppressWarnings("rawtypes")
	protected void changeAdapter(int index,OrgListAdapter adapter){
		ListView lv = (ListView)mMultiLevelViews.get(index);
		lv.setAdapter(adapter);
		lv.setTag(adapter);
	}
	
	protected ListView	addNewListView(List<Departmember> departs){
		DepartmemberAdapter adapter = new OrgDepartmemberAdapter(this,this);
		adapter.addAllItem(departs);
		
		return addNewListViewWithAdapter(adapter);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected ListView addNewListViewWithAdapter(OrgListAdapter adapter){
		mButtonBack.setVisibility(View.VISIBLE);
		
		setNewOrgAdapterAttr(adapter);
		
		OrgListAdapter<Object> adapterPrev = getAdapter(mMultiLevelViews.size() - 1);
		adapterPrev.setShowInfoBtn(false);
		
		ListView listView = new ListView(this);
		listView.setSelector(new ColorDrawable(0x00000000));
		listView.setCacheColorHint(0x00000000);
		listView.setDivider(null);
		listView.setBackgroundColor(0xffffffff);
		listView.setOnItemClickListener(this);
		listView.setAdapter(adapter);
		listView.setTag(adapter);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, 
				RelativeLayout.LayoutParams.MATCH_PARENT);
		lp.leftMargin = XApplication.getScreenWidth();
		mLayoutListView.addView(listView, mMultiLevelViews.size(),lp);
		mMultiLevelViews.add(listView);
		
		if(mIsClickLv1){
			mScroller.startScroll(XApplication.getScreenWidth(), 0, 
					mListLv2MinLeftMargin - XApplication.getScreenWidth(), 0, 1000);
			mHandler.post(mRunnableMoveList);
		}else{
			adapterPrev.setIsLv1Back(true);
			mScroller.startScroll(mListLv2MinLeftMargin, XApplication.getScreenWidth(), 
					0 - mListLv2MinLeftMargin, 
					mListLv2MinLeftMargin - XApplication.getScreenWidth(), 1000);
			mHandler.post(mRunnableMoveList);
		}
		
		return listView;
	}
	
	@SuppressWarnings("rawtypes")
	protected OrgListAdapter getAdapter(int viewIndex){
		return (OrgListAdapter)mMultiLevelViews.get(viewIndex).getTag();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onChildViewClicked(OrgListAdapter adapter,Object item, int viewId,View v) {
		if(viewId == R.id.ivInfo){
			JHUtils.launchDetails(this,item);
		}else if(viewId == R.id.cb){
			CheckBox cb = (CheckBox)v;
			if(cb.isChecked()){
				if(item instanceof Departmember){
					Departmember dm = (Departmember)item;
					if(dm.isUser()){
						addCheckUser(dm.getId(),dm.getName(),true);
						notifyLastTwoAdapter();
					}else{
						if(mMultiLevelViews.size() == 1){
							mIsClickLv1 = true;
							pushEvent(JHEventCode.HTTP_GetOrg, dm.getId(),dm,adapter,true);
						}else{
							OrgListAdapter lastAdapter = getAdapter(mMultiLevelViews.size() - 1);
							if(adapter == lastAdapter){
								mIsClickLv1 = false;
								pushEvent(JHEventCode.HTTP_GetOrg, dm.getId(),dm,adapter,true);
							}else{
								mIsClickLv1 = true;
								pushEvent(JHEventCode.HTTP_GetOrg, dm.getId(),dm,adapter,true);
							}
						}
					}
				}else if(item instanceof IMGroup){
					IMGroup g = (IMGroup)item;
					if(mMultiLevelViews.size() == 1){
						mIsClickLv1 = true;
						showGroupMember(g,adapter,true);
					}else{
						OrgListAdapter lastAdapter = getAdapter(mMultiLevelViews.size() - 1);
						if(adapter == lastAdapter){
							mIsClickLv1 = false;
							showGroupMember(g, adapter, true);
						}else{
							mIsClickLv1 = true;
							showGroupMember(g, adapter, true);
						}
					}
				}else if(item instanceof IMContact){
					final IMContact m = (IMContact)item;
					addCheckUser(m.getId(),m.getName(),true);
					notifyLastTwoAdapter();
				}
			}else{
				if(item instanceof Departmember){
					Departmember dm = (Departmember)item;
					if(dm.isUser()){
						removeCheckUser(dm.getId(),true);
						notifyLastTwoAdapter();
					}else{
						removeCheckDepartmember(dm.getId());
						notifyLastTwoAdapter();
					}
				}else if(item instanceof IMGroup){
					final IMGroup g = (IMGroup)item;
					removeCheckGroup(g.getId());
					notifyLastTwoAdapter();
				}else if(item instanceof IMContact){
					IMContact m = (IMContact)item;
					removeCheckUser(m.getId(),true);
					notifyLastTwoAdapter();
				}
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected void showGroupMember(IMGroup group,OrgListAdapter adapter,boolean bCheck){
		final Collection<IMContact> groupMembers = group.getMembers();
		boolean bHasSelect = adapter.getSelectItem() != null;
		adapter.setSelectItem(group);
		
		mMapIdToGroupMember.put(group.getId(), groupMembers);
		
		if(bCheck){
			addCheckGroup(group.getId());
		}
		
		if(bHasSelect){
			OrgGroupMemberAdapter gAdapter = (OrgGroupMemberAdapter)
					getAdapter(mMultiLevelViews.size() - 1);
			gAdapter.replaceAll(groupMembers);
		}else{
			OrgGroupMemberAdapter newAdapter = new OrgGroupMemberAdapter(this,this);
			newAdapter.addAll(groupMembers);
			addNewListViewWithAdapter(newAdapter);
		}
	}
	
	@Override
	protected void onChooseViewRemoved(Object tag) {
		super.onChooseViewRemoved(tag);
		notifyLastTwoAdapter();
	}

	@SuppressWarnings("rawtypes")
	protected void notifyLastTwoAdapter(){
		OrgListAdapter adapter = getAdapter(mMultiLevelViews.size() - 1);
		adapter.notifyDataSetChanged();
		if(mMultiLevelViews.size() > 1){
			adapter = getAdapter(mMultiLevelViews.size() - 2);
			adapter.notifyDataSetChanged();
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected boolean isMultiViewAdapter(OrgListAdapter adapter){
		int count = mMultiLevelViews.size();
		for(int index = 0;index < count;++index){
			if(getAdapter(index) == adapter){
				return true;
			}
		}
		return false;
	}
}
