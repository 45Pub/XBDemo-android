package com.xbcx.im.ui.simpleimpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.xbcx.adapter.SectionIndexerAdapter;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.IDObject;
import com.xbcx.core.NameObject;
import com.xbcx.im.IMContact;
import com.xbcx.im.IMGroup;
import com.xbcx.im.IMKernel;
import com.xbcx.library.R;
import com.xbcx.utils.PinyinUtils;
import com.xbcx.view.SectionIndexerView;

public class AddressBooksActivity extends BaseUserChooseActivity implements 
													View.OnClickListener,
													OnItemClickListener,
													OnItemLongClickListener,
													SectionIndexerView.OnSectionListener,
													TextWatcher,
													OnChildViewClickListener,
													OnCheckCallBack{
	
	protected static final int MENUID_DELETE = 1;
	
	protected ListView					mListView;
	protected SectionIndexerView		mSectionIndexerView;
	protected TextView					mTextViewLetter;
	protected EditText					mEditText;
	
	protected SectionIndexerAdapter		mSectionAdapter;
	protected IMGroupAdapter			mGroupAdapter;
	protected AdbSectionAdapter			mGroupSectionAdapter;
	
	protected Collection<IMGroup> 		mGroups;
	protected Collection<IMContact>  	mContacts;
	
	protected boolean					mIsCheck;
	protected HashMap<String, Object> 	mMapCheckIdToItem = new HashMap<String, Object>();
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mIsCheck = getIntent().getBooleanExtra("ischeck", false);
		
		mListView = (ListView)findViewById(R.id.lv);
		mListView.setDivider(null);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		registerForContextMenu(mListView);
		mSectionIndexerView = (SectionIndexerView)findViewById(R.id.si);
		mSectionIndexerView.setOnSectionListener(this);
		mTextViewLetter = (TextView)findViewById(R.id.tvLetter);
		mSectionIndexerView.setTextViewPrompt(mTextViewLetter);
		
		mSectionAdapter = new SectionIndexerAdapter();
		mGroupAdapter = new IMGroupAdapter(this);
		mGroupAdapter.setOnCheckCallBack(this);
		mGroupAdapter.setOnChildViewClickListener(this);
		mGroupAdapter.setIsCheck(mIsCheck);
		mGroupSectionAdapter = new AdbSectionAdapter(this, getString(R.string.groups));
		
		findViewById(R.id.ivClear).setOnClickListener(this);
		mEditText = (EditText)findViewById(R.id.etSearch);
		mEditText.addTextChangedListener(this);
		
		final Event e1 = mEventManager.runEvent(EventCode.IM_GetFriendList);
		final Event e2 = mEventManager.runEvent(EventCode.IM_GetGroupChatList);
		if(e1.isSuccess()){
			mContacts = (Collection<IMContact>)e1.getReturnParamAtIndex(0);
		}
		if(e2.isSuccess()){
			mGroups = (Collection<IMGroup>)e2.getReturnParamAtIndex(0);
		}
		
		handleSections();
		
		addAndManageEventListener(EventCode.IM_FriendListChanged);
		addAndManageEventListener(EventCode.IM_GroupChatListChanged);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mActivityLayoutId = R.layout.activity_addressbooks;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		final Object item = parent.getItemAtPosition(position);
		if(item != null){
			if(item instanceof IMGroup ||
					item instanceof IMContact){
				setTag(item);
			}
		}
		return false;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		final Object tag = getTag();
		if(tag != null){
			if(tag instanceof IMGroup){
				final IMGroup group = (IMGroup)tag;
				menu.setHeaderTitle(group.getName());
				if(IMGroup.ROLE_ADMIN.equals(group.getMemberRole(IMKernel.getLocalUser()))){
					menu.add(0, MENUID_DELETE, 0, R.string.delete_group);
				}else{
					menu.add(0, MENUID_DELETE, 0, R.string.quit_group);
				}
			}else if(tag instanceof IMContact){
				final IMContact c = (IMContact)tag;
				menu.setHeaderTitle(c.getName())
				.add(0, MENUID_DELETE, 0, R.string.delete_friend);
			}
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int id = item.getItemId();
		if(id == MENUID_DELETE){
			final Object tag = getTag();
			if(tag != null){
				if(tag instanceof IMGroup){
					final IMGroup g = (IMGroup)tag;
					if(IMGroup.ROLE_ADMIN.equals(g.getMemberRole(IMKernel.getLocalUser()))){
						pushEvent(EventCode.IM_DeleteGroupChat, g.getId());
					}else{
						pushEvent(EventCode.IM_QuitGroupChat, g.getId());
					}
				}else if(tag instanceof IMContact){
					final IMContact c = (IMContact)tag;
					pushEvent(EventCode.IM_DeleteFriend, c.getId());
				}
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		//super.onClick(v);
		final int id = v.getId();
		if(id == R.id.ivClear){
			mEditText.setText(null);
		}
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		handleSections();
	}

	@Override
	public void afterTextChanged(Editable s) {
	}
	
	@Override
	public void onSectionSelected(SectionIndexerView view, int section) {
		int pos = mSectionAdapter.getPositionForSection(section);
		mListView.setSelection(pos);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == EventCode.IM_FriendListChanged){
			mContacts = (Collection<IMContact>)event.getParamAtIndex(0);
			handleSections();
		}else if(code == EventCode.IM_GroupChatListChanged){
			mGroups = (Collection<IMGroup>)event.getParamAtIndex(0);
			handleSections();
		}else if(code == EventCode.IM_DeleteGroupChat){
			if(!event.isSuccess()){
				final Exception e = event.getFailException();
				if(e != null && e instanceof XMPPException){
					final XMPPException xe = (XMPPException)e;
					XMPPError error = xe.getXMPPError();
					if(error != null){
						if(error.getCode() == 405){
							mToastManager.show(R.string.toast_delete_group_fail_by_permission);
						}
					}
				}
			}
		}
	}
	
	protected void	handleSections(){
		final String key = mEditText.getText().toString();
		mListView.setAdapter(null);
		final Collection<IMGroup> groups = filterGroups(mGroups, key);
		final Collection<IMContact> contacts = filterContacts(mContacts, key);
		
		mGroupAdapter.replaceAll(groups);
		
		if(mGroupAdapter.getCount() > 0){
			mGroupSectionAdapter.setVisible(true);
		}else{
			mGroupSectionAdapter.setVisible(false);
		}
		
		mSectionAdapter.clear();
		mSectionAdapter.addSection(mGroupSectionAdapter);
		mSectionAdapter.addSection(mGroupAdapter);
		
		final HashMap<String, List<IMContact>> map = new HashMap<String, List<IMContact>>();
		final List<String> sections = new LinkedList<String>();
		
		for(IMContact user : contacts){
			String firstSpell = PinyinUtils.getFirstSpell(user.getName());
			if(firstSpell.length() == 0){
				firstSpell = "#";
			}else{
				if(!Character.isLetter(firstSpell.charAt(0))){
					firstSpell = "#";
				}
			}
			
			List<IMContact> us = map.get(firstSpell);
			if(us == null){
				us = new LinkedList<IMContact>();
				map.put(firstSpell, us);
				sections.add(firstSpell);
			}
			us.add(user);
		}
		
		Collections.sort(sections);
		
		for(String section : sections){
			mSectionAdapter.addSection(section,new AdbSectionAdapter(this, section));
			IMContactAdapter adapter = new IMContactAdapter(this);
			adapter.setIsCheck(mIsCheck);
			adapter.setOnCheckCallBack(this);
			adapter.setOnChildViewClickListener(this);
			adapter.addAll(map.get(section));
			mSectionAdapter.addSection(adapter);
		}
		
		mSectionIndexerView.setSections(sections);
		
		mListView.setAdapter(mSectionAdapter);
	}
	
	protected Collection<IMGroup> filterGroups(Collection<IMGroup> groups,String key){
		if(groups == null){
			return Collections.emptySet();
		}
		if(TextUtils.isEmpty(key)){
			return groups;
		}
		Collection<IMGroup> filters = new ArrayList<IMGroup>();
		for(IMGroup group : groups){
			if(nameFilter(group, key)){
				filters.add(group);
			}
		}
		return filters;
	}
	
	protected Collection<IMContact> filterContacts(Collection<IMContact> contacts,String key){
		if(contacts == null){
			return Collections.emptySet();
		}
		if(TextUtils.isEmpty(key)){
			return contacts;
		}
		Collection<IMContact> filters = new ArrayList<IMContact>();
		for(IMContact contact : contacts){
			if(nameFilter(contact, key)){
				filters.add(contact);
			}
		}
		return filters;
	}

	@Override
	public boolean isCheck(Object item) {
		if(item instanceof IDObject){
			final String id = ((IDObject)item).getId();
			return mMapCheckIdToItem.containsKey(id);
		}
		return false;
	}

	@Override
	public void onChildViewClicked(BaseAdapter adapter, Object item,int viewId, View v) {
		if(viewId == R.id.cb){
			final CheckBox cb = (CheckBox)v;
			if(cb.isChecked()){
				addCheckItem(item);
			}else{
				removeCheckItem(item);
			}
		}
	}
	
	protected void addCheckItem(Object item){
		if(item instanceof IDObject){
			final String id = ((IDObject)item).getId();
			mMapCheckIdToItem.put(id, item);
			if(item instanceof IMContact){
				addCheckUser((NameObject)item);
			}else if(item instanceof IMGroup){
				final IMGroup group = (IMGroup)item;
				for(IMContact c : group.getMembers()){
					addCheckUser(c);
				}
			}
			onAddChecked(item);
		}
	}
	
	protected void removeCheckItem(Object item){
		if(item instanceof IDObject){
			final String id = ((IDObject)item).getId();
			mMapCheckIdToItem.remove(id);
			if(item instanceof IMContact){
				mMapCheckUserIds.remove(id);
			}else if(item instanceof IMGroup){
				final IMGroup group = (IMGroup)item;
				for(IMContact c : group.getMembers()){
					if(!mMapCheckIdToItem.containsKey(c.getId())){
						mMapCheckUserIds.remove(c.getId());
					}
				}
			}
			onRemoveChecked(item);
		}
	}
	
	protected void onAddChecked(Object item){
	}
	
	protected void onRemoveChecked(Object item){
	}
}
