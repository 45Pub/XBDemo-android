package com.xbcx.jianhua;

import com.xbcx.core.NameObject;

public class Member extends NameObject {

	private static final long serialVersionUID = 1L;

	public Member(String id,String name) {
		super(id);
		mName = name;
	}
}
