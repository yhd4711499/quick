package com.ornithopter.quick.plugins;

import android.util.Log;

public abstract class Info {
	public static final String SPERATOR = ";;";
	public static final String SEARCH_FIELD_TAG_NAME = "name";
	public String Name;
	
	private String[] mCachedInfos;
	
	protected Info(){
		
	}
	
	protected Info(String cacheStr){
		Log.v("Info","trying to parse line: "  + cacheStr );
		mCachedInfos = getInfo();
		
        String[] info_str_split = cacheStr.split(";;");

        int extra = mCachedInfos == null?0:mCachedInfos.length;
        if (info_str_split.length < 1 + extra) {
            return;
        }
        
        Name = info_str_split[0];
        
        String[] splits = new String[info_str_split.length - 1];
        for(int i = 1; i < info_str_split.length; i++){
        	splits[i-1] = info_str_split[i];
        }
        
        fillInfoFromCache(splits);
	}
	
	abstract void fillInfoFromCache(String[] splits);
	
	public String toString() {
		String[] strs = getInfo();
		StringBuilder sb = new StringBuilder(Name);
		if(strs!=null){
			for(String str : strs){
				sb.append(SPERATOR);
				sb.append(str);
			}
		}
		return sb.toString();
    }
	
	abstract String[] getInfo();
	
//	protected String conact(String... strs){
//		StringBuilder sb = new StringBuilder();
//		for(int i=0; i < strs.length; i++){
//			sb.append(strs[i]);
//			if(i != strs.length - 1)
//				sb.append(SPERATOR);
//		}
//		return sb.toString();
//	}
}
