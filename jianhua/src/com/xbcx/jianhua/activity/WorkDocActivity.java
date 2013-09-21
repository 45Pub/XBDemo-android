package com.xbcx.jianhua.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;

import com.xbcx.adapter.SetBaseAdapter;
import com.xbcx.core.EventCode;
import com.xbcx.core.XApplication;
import com.xbcx.im.folder.FileItem;
import com.xbcx.im.ui.XBaseActivity;
import com.xbcx.jianhua.R;
import com.xbcx.utils.FileHelper;
import com.xbcx.utils.SystemUtils;

public class WorkDocActivity extends XBaseActivity implements 
														View.OnClickListener,
														AdapterView.OnItemClickListener{
	
	private HashMap<FileItem, FileItem> mMapCheckFileItems = new HashMap<FileItem, FileItem>();
	
	private List<FileItem> 	mFileItems = new LinkedList<FileItem>();
	
	private ListView		mListView;
	private FileItemAdapter	mAdapter;
	
	private Button			mButtonSend;
	
	private TabIndicatorMoveRunnable	mTabIndicator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mListView = (ListView)findViewById(R.id.lv);
		mEventManager.runEvent(EventCode.DB_ReadFolder,mFileItems);
		mAdapter = new FileItemAdapter();
		mAdapter.replaceAll(mFileItems);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		
		mButtonSend = (Button)findViewById(R.id.btnSend);
		mButtonSend.setVisibility(View.GONE);
		mButtonSend.setOnClickListener(this);
		
		mTabIndicator = new TabIndicatorMoveRunnable(
				findViewById(R.id.viewIndicator), 
				SystemUtils.dipToPixel(this, 78),
				XApplication.getScreenWidth() / 2);
		changeTab(0);
		
		findViewById(R.id.tvReceive).setOnClickListener(this);
		findViewById(R.id.tvSend).setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
		ba.mTitleTextStringId = R.string.work_doc;
	}
	
	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if(id == R.id.tvReceive){
			changeTab(0);
		}else if(id == R.id.tvSend){
			changeTab(1);
		}else if(id == R.id.cb){
			CheckBox cb = (CheckBox)v;
			final FileItem fi = (FileItem)cb.getTag();
			if(cb.isChecked()){
				mMapCheckFileItems.put(fi, fi);
				fi.setSelect(true);
			}else{
				mMapCheckFileItems.remove(fi);
				fi.setSelect(false);
			}
			if(mMapCheckFileItems.size() > 0){
				mButtonSend.setVisibility(View.VISIBLE);
				mButtonSend.setText(getString(R.string.work_doc_send_doc) + 
						"(" + mMapCheckFileItems.size() + ")");
			}else{
				mButtonSend.setVisibility(View.GONE);
			}
		}else if(id == R.id.btnSend){
			Intent data = new Intent();
			ArrayList<FileItem> fileItems = new ArrayList<FileItem>(mMapCheckFileItems.keySet());
			data.putExtra("fileitems", fileItems);
			setResult(RESULT_OK, data);
			finish();
		}
	}
	
	protected void changeTab(int index){
		mTabIndicator.onTabChanged(index);
		mAdapter.clear();
		if(index == 0){
			for(FileItem item : mFileItems){
				if(!item.isFromSelf()){
					mAdapter.addItem(item);
				}
			}
		}else if(index == 1){
			for(FileItem item : mFileItems){
				if(item.isFromSelf()){
					mAdapter.addItem(item);
				}
			}
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Object item = parent.getItemAtPosition(position);
		if(item != null && item instanceof FileItem){
			final FileItem fi = (FileItem)item;
			String ext = FileHelper.getFileExt(fi.getName(), "");
			openFile(fi.getPath(), ext);
		}
	}

	private class FileItemAdapter extends SetBaseAdapter<FileItem>{
		
		private SimpleDateFormat df = new SimpleDateFormat("yyyy-M-d",Locale.getDefault()); 
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if(convertView == null){
				convertView = LayoutInflater.from(WorkDocActivity.this).inflate(R.layout.adapter_fileitem, null);
				viewHolder = new ViewHolder();
				viewHolder.mTextViewName = (TextView)convertView.findViewById(R.id.tvName);
				viewHolder.mTextViewTime = (TextView)convertView.findViewById(R.id.tvTime);
				viewHolder.mCheckBox = (CheckBox)convertView.findViewById(R.id.cb);
				viewHolder.mCheckBox.setOnClickListener(WorkDocActivity.this);
				viewHolder.mCheckBox.setFocusable(false);
				convertView.setTag(viewHolder);
			}else{
				viewHolder = (ViewHolder)convertView.getTag();
			}
			
			final FileItem fileItem = (FileItem)getItem(position);
			
			viewHolder.mCheckBox.setTag(fileItem);
			
			viewHolder.mTextViewName.setText(fileItem.getName());
			try{
				viewHolder.mTextViewTime.setText(df.format(new Date(fileItem.getTime())));
			}catch(Exception e){
				viewHolder.mTextViewTime.setText(null);
			}
			viewHolder.mCheckBox.setChecked(fileItem.isSelect());
			
			return convertView;
		}
		
		private class ViewHolder{
			TextView 	mTextViewName;
			TextView	mTextViewTime;
			CheckBox	mCheckBox;
		}
	}
	
	protected static class TabIndicatorMoveRunnable implements Runnable{
		
		protected final View 		mView;
		protected final int			mViewWidth;
		protected final int 		mTabWidth;
		protected final Scroller 	mScroller;
		
		public TabIndicatorMoveRunnable(View view,int nViewWidth,int nTabWidth){
			mView = view;
			mViewWidth = nViewWidth;
			mTabWidth = nTabWidth;
			mScroller = new Scroller(mView.getContext());
		}
		
		public void onTabChanged(int nTabIndex){
			final int nTargetX = mTabWidth * nTabIndex + 
					(mTabWidth - mViewWidth) / 2;
			final int nCurPadding = mView.getPaddingLeft();
			mScroller.startScroll(nCurPadding, 0, nTargetX - nCurPadding, 0, 500);
			mView.post(this);
		}

		@Override
		public void run() {
			if(mScroller.computeScrollOffset()){
				mView.setPadding(mScroller.getCurrX(), 0, 0, 0);
				mView.post(this);
			}
		}
	}

}
