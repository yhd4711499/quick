package com.ornithopter.quick.plugins;

import android.content.Context;

public abstract class PluginBase {
	Context context;
//	String[] verbs;
//	String hitVerb;
//	String[] specifiedTargets;
//	String hitTarget;
//	String additionInfo;
	OnFindListener onFindListener;
	public PluginBase(Context c){
		context = c;
	}
	
	public void setOnFindListener(OnFindListener listener){
		onFindListener = listener;
	}
	
	protected void onFindResults(PluginResultItem<?>... items){
		if(onFindListener != null){
			onFindListener.onFind(this, items);
		}
	}
}
