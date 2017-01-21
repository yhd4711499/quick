package com.ornithopter.quick.plugins;

public class NativeMethods {
	static{
		System.loadLibrary("getpinyin");
	}
	
	public static native String getPinyin(String str, int[] map);
	public static native String getLoosePattern(String str);
	public static native String getInitialPattern(String str);
	public static native String removeSpace(String str);
	
	/**
	 * 匹配开头，忽略空格
	 * @param keyword
	 * @param content
	 * @return 自信度，0为不匹配，最大为10
	 */
	public static native int startWith(String keyword, String content);
	
	/**
	 * 匹配首字母（以空格分开的单词）
	 * @param keyword
	 * @param content
	 * @return 自信度，0为不匹配，最大为10
	 */
	public static native int initialWith(String keyword, String content);
	
	/**
	 * 松散匹配，匹配任意字母
	 * @param keyword
	 * @param content
	 * @return 自信度，0为不匹配，最大为10
	 */
	public static native int looseWith(String keyword, String content);
	
	public static native int isMatch(String keyword, String content, int[] map, int[] hitMap);
}
