package com.xbcx.im;

public interface RecentChatProvider {

	public String	getId(Object obj);
	
	public long		getTime(Object obj);
	
	public void		handleRecentChat(RecentChat rc,Object obj);
	
	public boolean	isUnread(Object obj);
}
