package com.xbcx.jianhua.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.im.IMContact;
import com.xbcx.im.IMGroup;
import com.xbcx.im.IMKernel;
import com.xbcx.im.VCardProvider;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.im.ui.simpleimpl.OnCheckCallBack;
import com.xbcx.jianhua.Departmember;
import com.xbcx.jianhua.R;

public class UserChooseBaseActivity extends ChooseBaseActivity implements 
															OnCheckCallBack{
	
	protected HashMap<String, String> 	mMapCheckUsers = new HashMap<String, String>();
	protected HashMap<String, String> 	mMapCheckDeparts = new HashMap<String, String>();
	protected HashMap<String, String> 	mMapCheckGroups = new HashMap<String, String>();
	protected HashMap<String, String> 	mMapCheckDiscussions = new HashMap<String, String>();
	protected HashMap<String, String>  	mMapCheckShowUsers = new HashMap<String, String>();
	protected HashMap<String, String>	mMapIdToName = new HashMap<String, String>();
	
	protected HashMap<String, List<Departmember>> 		mMapIdToDepartChilds = new HashMap<String, List<Departmember>>();
	protected HashMap<String, Collection<IMContact>> 	mMapIdToGroupMember = new HashMap<String, Collection<IMContact>>();
	
	protected boolean	mChooseAssociateUser = true;
	
	protected String	mId;
	protected String	mDefaultUserId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mId = getIntent().getStringExtra("id");
		mDefaultUserId = getIntent().getStringExtra("defaultUserId");
		mMapIdToName.put(IMKernel.getLocalUser(),
				VCardProvider.getInstance().loadUserName(IMKernel.getLocalUser()));
		if(!TextUtils.isEmpty(mDefaultUserId)){
			mMapIdToName.put(mDefaultUserId, getIntent().getStringExtra("defaultUserName"));
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		final int id = v.getId();
		if(id == R.id.btnOK){
			onOKButtonClicked(v);
		}
	}
	
	protected void onOKButtonClicked(View v){
		if(mMapCheckUsers.size() > 0){
			if(TextUtils.isEmpty(mId)){
				final List<String> ids = new ArrayList<String>(mMapCheckUsers.keySet());
				if(mDefaultUserId != null){
					if(!mMapCheckUsers.containsKey(mDefaultUserId)){
						ids.add(mDefaultUserId);
					}
				}
				if(!mMapCheckUsers.containsKey(IMKernel.getLocalUser())){
					ids.add(IMKernel.getLocalUser());
				}
				StringBuffer buf = new StringBuffer();
				int index = 0;
				for(String uid : ids){
					if(index == 0){
						buf.append(mMapIdToName.get(uid));
					}else{
						buf.append(",").append(mMapIdToName.get(uid));
					}
					++index;
					if(index == 3){
						break;
					}
				}
				
				pushEvent(EventCode.IM_CreateGroupChat,buf.toString(),ids);
			}else{
				pushEvent(EventCode.IM_AddGroupChatMember, mId,mMapCheckUsers.keySet());
			}
		}
	}
	
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == EventCode.IM_CreateGroupChat){
			if(event.isSuccess()){
				final String id = (String)event.getReturnParamAtIndex(0);
				final String name = (String)event.getParamAtIndex(0);
				ActivityType.launchChatActivity(this, ActivityType.GroupChat,id,name);
				finish();
			}
		}else if(code == EventCode.IM_AddGroupChatMember){
			if(event.isSuccess()){
				finish();
			}
		}
	}

	@Override
	public boolean isCheck(Object item) {
		if(item instanceof Departmember){
			Departmember dm = (Departmember)item;
			if(dm.isUser()){
				return mMapCheckUsers.containsKey(dm.getId());
			}else{
				return mMapCheckDeparts.containsKey(dm.getId());
			}
		}else if(item instanceof IMGroup){
			final IMGroup g = (IMGroup)item;
			return mMapCheckGroups.containsKey(g.getId());
		}else if(item instanceof IMContact){
			final IMContact c = (IMContact)item;
			return mMapCheckUsers.containsKey(c.getId());
		}
		return false;
	}
	
	protected void addCheckUser(String id,String name,boolean bAddView){
		if(!mMapCheckUsers.containsKey(id)){
			mMapCheckUsers.put(id, id);
			mMapIdToName.put(id, name);
		}
		if(bAddView){
			mMapCheckShowUsers.put(id, id);
			ImageView iv = new ImageView(this);
			iv.setScaleType(ImageView.ScaleType.FIT_XY);
			iv.setImageBitmap(VCardProvider.getInstance().loadAvatar(id));
			
			addChooseView(iv,id);
		}
	}
	
	protected void removeCheckUser(String id,boolean bRemoveView){
		String name = null;
		if(mMapCheckUsers.remove(id) != null){
			name = mMapIdToName.remove(id);
			updateButton();
		}
		if(bRemoveView){
			mMapCheckShowUsers.remove(id);
			removeChooseViewByTag(id);
		}else{
			if(mMapCheckShowUsers.containsKey(id)){
				mMapCheckUsers.put(id, id);
				mMapIdToName.put(id, name);
			}
		}
	}
	
	@Override
	protected void onChooseViewRemoved(Object tag) {
		super.onChooseViewRemoved(tag);
		if(tag instanceof String){
			final String id = (String )tag;
			removeCheckDepartmember(id);
			removeCheckDiscussion(id);
			removeCheckGroup(id);
			removeCheckUser(id,true);
		}
	}

	protected void addCheckDepartmember(String id){
		if(mChooseAssociateUser){
			final Collection<Departmember> departs = mMapIdToDepartChilds.get(id);
			if(departs != null){
				for(Departmember child : departs){
					if(child.isUser()){
						addCheckUser(child.getId(),child.getName(),false);
					}
				}
			}
		}
		
		if(!mMapCheckDeparts.containsKey(id)){
			mMapCheckDeparts.put(id, id);
			addChooseImageView(R.drawable.avatar_department,id);
		}
	}
	
	protected void removeCheckDepartmember(String id){
		if(mMapCheckDeparts.remove(id) != null){
			if(mChooseAssociateUser){
				List<Departmember> childs = mMapIdToDepartChilds.get(id);
				if(childs != null){
					for(Departmember child : childs){
						if(child.isUser()){
							removeCheckUser(child.getId(),false);
						}
					}
				}
			}
			removeChooseViewByTag(id);
		}
	}
	
	protected void addCheckGroup(String id){
		if(mChooseAssociateUser){
			final Collection<IMContact> members = mMapIdToGroupMember.get(id);
			if(members != null){
				for(IMContact m : members){
					addCheckUser(m.getId(),m.getName(),false);
				}
			}
		}
		
		if(!mMapCheckGroups.containsKey(id)){
			mMapCheckGroups.put(id, id);
			addChooseImageView(R.drawable.avatar_group, id);
		}
	}
	
	protected void removeCheckGroup(String id){
		if(mMapCheckGroups.remove(id) != null){
			if(mChooseAssociateUser){
				Collection<IMContact> members = mMapIdToGroupMember.get(id);
				if(members != null){
					for(IMContact m : members){
						removeCheckUser(m.getId(),false);
					}
				}
			}
			
			removeChooseViewByTag(id);
		}
	}
	
	protected void addCheckDiscussion(String id){
		/*if(mChooseAssociateUser){
			final Collection<User> users = mMapIdToDiscussionMember.get(id);
			if(users != null){
				for(User u : users){
					addCheckUser(u.getId(),u.getName(),false);
				}
			}
		}
		
		if(!mMapCheckDiscussions.containsKey(id)){
			mMapCheckDiscussions.put(id, id);
			addChooseImageView(R.drawable.avatar_discussion, id);
		}*/
	}
	
	protected void removeCheckDiscussion(String id){
		/*if(mMapCheckDiscussions.remove(id) != null){
			if(mChooseAssociateUser){
				Collection<User> users = mMapIdToDiscussionMember.get(id);
				if(users != null){
					for(User u : users){
						removeCheckUser(u.getId(),false);
					}
				}
			}
			
			removeChooseViewByTag(id);
		}*/
	}
	
	@Override
	protected int getChooseShowCount() {
		return mMapCheckUsers.size();
	}
}
