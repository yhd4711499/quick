package com.ornithopter.quick.plugins;

import android.util.Log;


public class PinyinUtils {
	public static String getPinyin(String src, int[] map){
		String result;
		result = NativeMethods.getPinyin(src, map);
		Log.v("PinyinUtils", src + " -> " + result);
		return result;
	}
}