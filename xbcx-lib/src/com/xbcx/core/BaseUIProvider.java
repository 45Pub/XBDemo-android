package com.xbcx.core;

public class BaseUIProvider {
	
	private static Class<? extends BaseUIFactory> baseUIFactoryClass;
	
	public static void	setBaseUIFactoryClass(Class<? extends BaseUIFactory> c){
		baseUIFactoryClass = c;
	}
	
	public static Class<? extends BaseUIFactory> getBaseUIFactoryClass(){
		return baseUIFactoryClass;
	}
}
