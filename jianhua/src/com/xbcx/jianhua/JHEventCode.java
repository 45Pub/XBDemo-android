package com.xbcx.jianhua;

import com.xbcx.core.EventCode;

public class JHEventCode extends EventCode {
	
	public static final int MainActivityLaunched 	= ++CODE_INC;
	
	/**
	 * params[0]:user
	 * </br>params[1]:pwd
	 * </br>reParams[0]:BaseInfo
     */
	public static final int HTTP_Login				= ++CODE_INC;
	
	/**
	 * params[0]:user
	 * </br>params[1]:pwd
	 * </br>params[2]:name
	 * </br>params[3]:avatar
	 * </br>params[4]:department
	 * </br>params[5]:mobilephone
	 * </br>params[6]:fixphone
	 * </br>reParams[0]:BaseInfo
     */
	public static final int HTTP_Register			= ++CODE_INC;
	
	/**
	 * params[0]:id
	 * </br>reParams[0]:List(Departmember)
     */
	public static final int HTTP_GetOrg				= ++CODE_INC;
	
	/**
	 * params[0]:searchKey
	 * </br>reParams[0]:List(Departmember)
     */
	public static final int HTTP_SearchOrg			= ++CODE_INC;
	
	/**
	 * params[0]:imUser
	 * </br>reParams[0]:UserInfo
     */
	public static final int HTTP_GetUserInfo		= ++CODE_INC;
	
	/**
	 * params[0]:BaseInfo
     */
	public static final int HTTP_ChangeUserInfo		= ++CODE_INC;
}
