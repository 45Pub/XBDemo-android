package com.xbcx.im;

import java.io.Serializable;

public class IMLoginInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private final String mUser;
	
	private final String mPwd;
	
	private final String mServer;
	
	private final String mIP;
	
	private final int	 mPort;
	
	public IMLoginInfo(String user,String pwd,String server,String ip,int port){
		mUser = user;
		mPwd = pwd;
		mServer = server;
		mIP = ip;
		mPort = port;
	}
	
	public String	getUser(){
		return mUser;
	}
	
	public String	getPwd(){
		return mPwd;
	}

	public String getServer() {
		return mServer;
	}

	public String getIP() {
		return mIP;
	}

	public int getPort() {
		return mPort;
	}
}
