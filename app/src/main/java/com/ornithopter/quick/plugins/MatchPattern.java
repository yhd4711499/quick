package com.ornithopter.quick.plugins;

public class MatchPattern{
	final static int LOOSE = 0;
	final static int INITIAL = 10;
	final static int STARTWITH = 20;
	int priority;
	int type;
	
	MatchPattern(int type){
		this.type = type;
		this.priority = type;
	}
	
	/**
	 * 是否匹配
	 * @param keyword
	 * @param content
	 * @return 自信度，0为不匹配，最大为10
	 */
	int isMatch(String keyword, String content){
		if(keyword.length() > content.length())
			return 0;
		int result = 0;
		
		switch(type){
			case STARTWITH:
				result = NativeMethods.startWith(keyword, content);
				return result;
			case INITIAL:
				if(keyword.length() > 1){
					result = NativeMethods.initialWith(keyword, content);
					return result;
				}
				break;
			case LOOSE:
				if(keyword.length() > 1){
					result = NativeMethods.looseWith(keyword, content);
					return result;
				}
				break;
		}
		return result;
	}
}