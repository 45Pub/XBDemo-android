package com.xbcx.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.xbcx.core.IDObject;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class SetBaseAdapter<E extends Object> extends BaseAdapter {
	
	protected List<E> mListObject;
	
	public SetBaseAdapter(){
		mListObject = new ArrayList<E>();
	}

	public int getCount() {
		return mListObject.size();
	}

	public Object getItem(int position) {
		return mListObject.get(position);
	}

	public long getItemId(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	public abstract View getView(int position, View convertView, ViewGroup parent);
	
	public void replaceAll(Collection<E> collection){
		mListObject.clear();
		if(collection != null){
			mListObject.addAll(collection);
		}
		notifyDataSetChanged();
	}
	
	public void addAll(Collection<E> collection){
		if(collection != null){
			mListObject.addAll(collection);
		}
		notifyDataSetChanged();
	}

	public void addItem(E e){
		mListObject.add(e);
		notifyDataSetChanged();
	}
	
	public void addAllItem(List<E> list){
		mListObject.addAll(list);
		notifyDataSetChanged();
	}
	
	public void removeItem(E e){
		mListObject.remove(e);
		notifyDataSetChanged();
	}
	
	public void removeItemById(String id){
		if(mListObject.size() > 0){
			Object item = getItem(0);
			if(item instanceof IDObject){
				int index = 0;
				for(Object o : mListObject){
					IDObject ido = (IDObject)o;
					if(ido.getId().equals(id)){
						mListObject.remove(index);
						notifyDataSetChanged();
						break;
					}
					++index;
				}
			}
		}
	}
	
	public Object getItemById(String id){
		if(mListObject.size() > 0){
			Object item = getItem(0);
			if(item instanceof IDObject){
				for(Object o : mListObject){
					IDObject ido = (IDObject)o;
					if(ido.getId().equals(id)){
						return o;
					}
				}
			}
		}
		return null;
	}
	
	public void removeAllItem(List<E> list){
		mListObject.removeAll(list);
		notifyDataSetChanged();
	}
	
	public List<E>	getAllItem(){
		return mListObject;
	}
	
	public void clear(){
		mListObject.clear();
		notifyDataSetChanged();
	}

}
