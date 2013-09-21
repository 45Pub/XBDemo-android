package com.xbcx.im;

import java.io.Serializable;
import java.util.HashMap;

import com.xbcx.core.IDObject;

public class BaseVCard extends IDObject implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private HashMap<String, String> mAttributes;
	
	public BaseVCard(String id){
		super(id);
	}

	public void addAttribute(String name,String value){
		if(mAttributes == null){
			mAttributes = new HashMap<String, String>();
		}
		mAttributes.put(name, value);
	}
	
	public String getAttribute(String name){
		if(mAttributes == null){
			return "";
		}
		String value = mAttributes.get(name);
		if(value == null){
			value = "";
		}
		return value;
	}
}
