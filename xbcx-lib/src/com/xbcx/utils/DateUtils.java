package com.xbcx.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	
	public static long getTimeNextDay(long lTimeMillis){
		Date date = new Date(lTimeMillis);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		Date dateNext = cal.getTime();
		return dateNext.getTime();
	}
	
	public static boolean isToday(long lTime){
		return isDateDayEqual(lTime, System.currentTimeMillis());
	}
	
	public static boolean isTomorrow(long lTime){
		return isDateDayEqual(lTime, getTimeNextDay(lTime));
	}
	
	public static boolean isDateDayEqual(long lTime1,long lTime2){
		Calendar cal1 = Calendar.getInstance();
		cal1.setTimeInMillis(lTime1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTimeInMillis(lTime2);
		
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
				cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
	}
	
	public static boolean isInLastWeek(long lTime){
		Calendar calToday = Calendar.getInstance();
		Calendar calUnknown = Calendar.getInstance();
		calToday.setTimeInMillis(System.currentTimeMillis());
		calUnknown.setTimeInMillis(lTime);
		int nDayOfWeek = calToday.get(Calendar.DAY_OF_WEEK);
		if(nDayOfWeek == Calendar.SUNDAY){
			nDayOfWeek = Calendar.SATURDAY + 1;
		}
		nDayOfWeek -= 1;
		calToday.add(Calendar.DAY_OF_YEAR, -(nDayOfWeek - 1));
		if(calUnknown.after(calToday)){
			return false;
		}
		calToday.add(Calendar.DAY_OF_YEAR, -7);
		if(calUnknown.before(calToday)){
			return false;
		}
		return true;
	}
	
	public static boolean isInCurrentWeek(long lTime) {
		Calendar calToday = Calendar.getInstance();
		Calendar calUnknown = Calendar.getInstance();
		calToday.setTimeInMillis(System.currentTimeMillis());
		calUnknown.setTimeInMillis(lTime);
		int nDayOfWeek = calToday.get(Calendar.DAY_OF_WEEK);
		if (nDayOfWeek == Calendar.SUNDAY) {
			nDayOfWeek = Calendar.SATURDAY + 1;
		}
		nDayOfWeek -= 1;
		calToday.add(Calendar.DAY_OF_YEAR, -(nDayOfWeek - 1));
		if (calUnknown.before(calToday)) {
			return false;
		}
		calToday.add(Calendar.DAY_OF_YEAR, 7);
		if (calUnknown.after(calToday)) {
			return false;
		}
		return true;
	}
	
	public static boolean isInNextWeek(long lTime) {
		Calendar calToday = Calendar.getInstance();
		Calendar calUnknown = Calendar.getInstance();
		calToday.setTimeInMillis(System.currentTimeMillis());
		calUnknown.setTimeInMillis(lTime);
		int nDayOfWeek = calToday.get(Calendar.DAY_OF_WEEK);
		if (nDayOfWeek == Calendar.SUNDAY) {
			nDayOfWeek = Calendar.SATURDAY + 1;
		}
		nDayOfWeek -= 1;
		calToday.add(Calendar.DAY_OF_YEAR, 8 - nDayOfWeek);
		if (calUnknown.before(calToday)) {
			return false;
		}
		calToday.add(Calendar.DAY_OF_YEAR, 7);
		if (calUnknown.after(calToday)) {
			return false;
		}
		return true;
	}
	
	public static boolean isBeyondNextWeek(long lTime) {
		Calendar calToday = Calendar.getInstance();
		Calendar calUnknown = Calendar.getInstance();
		calToday.setTimeInMillis(System.currentTimeMillis());
		calUnknown.setTimeInMillis(lTime);
		int nDayOfWeek = calToday.get(Calendar.DAY_OF_WEEK);
		if (nDayOfWeek == Calendar.SUNDAY) {
			nDayOfWeek = Calendar.SATURDAY + 1;
		}
		nDayOfWeek -= 1;
		calToday.add(Calendar.DAY_OF_YEAR, 8 - nDayOfWeek);
		if (calUnknown.before(calToday)) {
			return false;
		}
		calToday.add(Calendar.DAY_OF_YEAR, 7);
		if (calUnknown.after(calToday)) {
			return true;
		}
		return false;
	}
	
	public static boolean isInCurrentYear(long lTime) {
		Calendar cal = Calendar.getInstance();
		Calendar calUnknown = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		calUnknown.setTimeInMillis(lTime);
		int nDayOfYear = cal.get(Calendar.DAY_OF_YEAR);

		cal.add(Calendar.DAY_OF_YEAR, 0 - nDayOfYear);
		if (calUnknown.before(cal)) {
			return false;
		}
		cal.add(Calendar.DAY_OF_YEAR, 365);
		if (calUnknown.after(cal)) {
			return false;
		}
		return true;
	}
}
