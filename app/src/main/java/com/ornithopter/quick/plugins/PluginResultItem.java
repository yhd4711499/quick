package com.ornithopter.quick.plugins;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;

public abstract class PluginResultItem<T extends Info> implements Comparable<PluginResultItem<T>>{
	private final static String HTML_PREFIX_1 = "<font color='#";
	private final static String HTML_PREFIX_2 = "'>";
	private final static String HTML_SUBFIX = "</font>";
	private final static String colorString = "6633b5e5";
	
	SearchPluginBase<?> mOwner;
	Context context;
	int mPriority;
	T mItem;
	int[] mMap,mHitMap;
	String mHitSearchFieldTag;
	
	public String getHitSearchFieldTag(){
		return mHitSearchFieldTag;
	}
	
	public PluginResultItem(SearchPluginBase<?> owner, T item, int priority, String hitSearchFieldTag){
		mOwner = owner;
		context = mOwner.context;
		mItem = item;
		mPriority = priority;
		mHitSearchFieldTag = hitSearchFieldTag;
	}
	
	public abstract View getView();
	
	public int getPriority(){
		return mPriority;
	}
	
	public T getItem(){
		return mItem;
	}

	public void performSelect(){
		mOwner.recordItem(mOwner.lastQuery, mItem.Name);
		doSelect();
	}
	
	protected abstract void doSelect();
	
	@Override
    public int compareTo(PluginResultItem<T> o) {

        if (this.getPriority() > o.getPriority()) {
            return 1;
        }
        else if (this.getPriority() < o.getPriority()) {
            return -1;
        }
        else {
            return 0;
        }

    }
	
	public void setMap(int[] map, int[] hitMap){
		mMap = map;
		mHitMap = hitMap;
	}
	
	protected String getHtmlString(String source){
		char[] chars = source.toCharArray();
		List<IntPair> pairs = new ArrayList<IntPair>();
		IntPair currentPair = null;
		List<Integer> correctedMap = new ArrayList<Integer>();
		for(int i:mMap){
			if(i == -1)
				break;
			correctedMap.add(i);
		}
		int lastHitValue = -1;
		for(int hitValue : mHitMap){
			if(currentPair == null){
				currentPair = new IntPair();
				currentPair.start = hitValue;
				currentPair.end = hitValue;
			}

			if(lastHitValue == -1){
				lastHitValue = hitValue;
				continue;
			}
			
			if(hitValue == lastHitValue){
				
			}
			else if(hitValue != lastHitValue + 1){
				pairs.add(currentPair);
				currentPair = new IntPair();
				currentPair.start = hitValue;
				currentPair.end = hitValue;
			}
			else{
				currentPair.end = hitValue;
			}
			
			lastHitValue = hitValue;
		}
		
		if(currentPair != null){
			pairs.add(currentPair);
			currentPair = null;
		}
		
		int i = 0;
		StringBuilder sb = new StringBuilder();
		for(IntPair pair : pairs){
			for(; i < pair.start; i++){
				sb.append(chars[i]);
			}
			
			sb.append(HTML_PREFIX_1);
			sb.append(colorString);
			sb.append(HTML_PREFIX_2);
			for(int ii = pair.start; ii <= pair.end; ii++){
				sb.append(chars[ii]);
			}
			sb.append(HTML_SUBFIX);
			i = pair.end + 1;
		}
		i = pairs.get(pairs.size() - 1).end + 1;
		if(i != chars.length){
			for(; i < chars.length; i++){
				sb.append(chars[i]);
			}
		}
		return sb.toString();
	}
	
	class IntPair{
		int start;
		int end;
		
		@Override
		public String toString(){
			return "{" + start +"," + end + "}";
		}
	}
}
