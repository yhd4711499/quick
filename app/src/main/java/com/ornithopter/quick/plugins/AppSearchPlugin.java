package com.ornithopter.quick.plugins;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.List;

public class AppSearchPlugin extends SearchPluginBase<AppInfo> {

	private final static String CACHE_NAME = "appsearch";
	
	SharedPreferences mPref;
	
	public AppSearchPlugin(Context c) {
		super(c);
		mPref = PreferenceManager.getDefaultSharedPreferences(c);
	}
	
//	Object lockObj = new Object();

	
	@Override
	protected SearchableField[] getSearchableFileds(AppInfo obj) {
		if(mPref.getBoolean("search_packageName", false)){
			return new SearchableField[]{
					new SearchableField(obj.getName() ,0, AppInfo.SEARCH_FIELD_TAG_NAME),
					new SearchableField(obj.getPackageName() ,0, AppInfo.SEARCH_FIELD_TAG_PACKAGE_NAME)
				};
		}
		else{
			return new SearchableField[]{
					new SearchableField(obj.getName() ,0, AppInfo.SEARCH_FIELD_TAG_NAME)
				};
		}
	}

	@Override
	protected AppInfo[] doGetItems() {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        AppInfo[] userApps = null;
        try {
            List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(mainIntent, 0);
            int appCount = resolveInfos.size();
            userApps = new AppInfo[appCount];
            for(int i = 0; i < appCount; i++){
            	userApps[i] = new AppInfo(resolveInfos.get(i), context.getPackageManager());
            }
        } catch (Exception e) {
            Log.d("AppSearchPlugin","获取程序列表失败：" + e.getMessage());

        }

		return userApps;
	}

	@Override
	protected PluginResultItem<AppInfo> getResultItem(AppInfo item, int priority, String hitSearchFieldTag) {
		return new AppSearchResultItem(this, item, priority, hitSearchFieldTag);
	}

	@Override
	protected AppInfo[] getArray(List<AppInfo> list) {
		AppInfo[] infos = new AppInfo[list.size()];
		return list.toArray(infos);
	}

	@Override
	protected AppInfo readItemCache(Context context, String cache) {
		return new AppInfo(cache, context.getPackageManager());
	}

	@Override
	protected String getCacheFileName() {
		return CACHE_NAME;
	}

}
