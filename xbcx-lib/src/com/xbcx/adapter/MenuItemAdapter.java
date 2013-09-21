package com.xbcx.adapter;

import com.xbcx.adapter.SetBaseAdapter;
import com.xbcx.library.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MenuItemAdapter extends SetBaseAdapter<MenuItemAdapter.MenuItem> {

	private Context mContext;
	
	public MenuItemAdapter(Context context){
		mContext = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			final LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.textview_menuitem, null);
		}
		
		final TextView textView = (TextView)convertView;
		
		final MenuItem item = (MenuItem)getItem(position);
		textView.setText(item.mTextStringId);
		
		return textView;
	}
	
	public void addItem(int nPos,MenuItem item){
		mListObject.add(nPos, item);
	}
	
	public void removeItem(int nId){
		for(MenuItem item : mListObject){
			if(item.getId() == nId){
				mListObject.remove(item);
				break;
			}
		}
	}
	
	public MenuItem getMenuItem(int nId){
		for(MenuItem item : mListObject){
			if(item.getId() == nId){
				return item;
			}
		}
		return null;
	}
	
	public static class MenuItem{
		private final int mId;
		private final int mTextStringId;
		
		public MenuItem(int nId,int nStringId){
			mId = nId;
			mTextStringId = nStringId;
		}
		
		public int getId(){
			return mId;
		}
	}
}
