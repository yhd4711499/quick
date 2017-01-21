package com.ornithopter.quick.plugins;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

public class AppInfo extends Info {
	private Drawable icon;
	private PackageManager pm;
	private String mPackageName;
	
	public static final String SEARCH_FIELD_TAG_PACKAGE_NAME = "packageName";
	
	public AppInfo(ResolveInfo info, PackageManager pm){
		super();
		this.pm = pm;
		this.Name = info.loadLabel(pm).toString();
		this.mPackageName = info.activityInfo.packageName;
	}
	
	public AppInfo(String cacheStr, PackageManager pm){
		super(cacheStr);
		this.pm = pm;
	}
	
	public Drawable getIcon(){
		if(icon == null)
		{
			try {
				icon = pm.getApplicationIcon(mPackageName);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return icon;
	}

	public String getName() {
		return Name;
	}
	
	public String getPackageName(){
		return mPackageName;
	}
	
	@Override
	String[] getInfo() {
		return new String[]{mPackageName};
	}

	@Override
	void fillInfoFromCache(String[] splits) {
		mPackageName = splits[0];
	}
}
