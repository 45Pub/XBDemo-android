package com.xbcx.adapter;

import java.util.ArrayList;
import java.util.List;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class SectionAdapter extends BaseAdapter {
	
	protected List<BaseAdapter> mListAdapter;
	
	public SectionAdapter(){
		mListAdapter = new ArrayList<BaseAdapter>();
	}

	public int getCount() {
		int nCount = 0;
		int nAdapterCount = mListAdapter.size();
		for(int nIndex = 0;nIndex < nAdapterCount;++nIndex){
			nCount += mListAdapter.get(nIndex).getCount();
		}
		return nCount;
	}

	public Object getItem(int position) {
		final int nAdapterCount = mListAdapter.size();
		int nCount = 0;
		for(int nIndex = 0;nIndex < nAdapterCount;++nIndex){
			BaseAdapter adapter = mListAdapter.get(nIndex);
			nCount = adapter.getCount();
			if(position >= nCount){
				position -= nCount;
			}else{
				return adapter.getItem(position);
			}
		}
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public int getItemViewType(int position) {
		int nAdapterCount = mListAdapter.size();
		int nItemViewType = 0;
		int nCount = 0;
		for(int nIndex = 0;nIndex < nAdapterCount;++nIndex){
			BaseAdapter adapter = mListAdapter.get(nIndex);
			nCount = adapter.getCount();
			if(position >= nCount){
				position -= nCount;
				nItemViewType += adapter.getViewTypeCount();
			}else{
				nItemViewType += adapter.getItemViewType(position);
				break;
			}
		}
		return nItemViewType;
	}

	@Override
	public int getViewTypeCount() {
		int nAdapterCount = mListAdapter.size();
		int nTypeCount = 0;
		for(int nIndex = 0;nIndex < nAdapterCount;++nIndex){
			nTypeCount += mListAdapter.get(nIndex).getViewTypeCount();
		}
		return nTypeCount;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		for(BaseAdapter adapter : mListAdapter){
			adapter.registerDataSetObserver(observer);
		}
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		for(BaseAdapter adapter : mListAdapter){
			adapter.unregisterDataSetObserver(observer);
		}
	}

	@Override
	public void notifyDataSetChanged() {
		for(BaseAdapter adapter : mListAdapter){
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void notifyDataSetInvalidated() {
		for(BaseAdapter adapter : mListAdapter){
			adapter.notifyDataSetInvalidated();
		}
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		int nAdapterCount = mListAdapter.size();
		int nCount = 0;
		for(int nIndex = 0;nIndex < nAdapterCount;++nIndex){
			BaseAdapter adapter = mListAdapter.get(nIndex);
			nCount = adapter.getCount();
			if(position >= nCount){
				position -= nCount;
			}else{
				return adapter.getView(position, convertView, parent);
			}
		}
		return null;
	}

	public int 			getSectionCount(){
		return mListAdapter.size();
	}
	
	public void 		addSection(BaseAdapter adapter){
		mListAdapter.add(adapter);
	}
	
	public BaseAdapter	getAdapter(int position){
		final int nAdapterCount = mListAdapter.size();
		int nCount = 0;
		for(int nIndex = 0;nIndex < nAdapterCount;++nIndex){
			BaseAdapter adapter = mListAdapter.get(nIndex);
			nCount = adapter.getCount();
			if(position >= nCount){
				position -= nCount;
			}else{
				return adapter;
			}
		}
		return null;
	}
	
	public void 		removeSection(BaseAdapter adapter){
		mListAdapter.remove(adapter);
	}
	
	public void 		clear(){
		mListAdapter.clear();
	}
}
