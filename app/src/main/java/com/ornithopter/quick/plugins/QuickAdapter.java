package com.ornithopter.quick.plugins;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.ornithopter.quick.ApplicationPrefs_;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Timer;
import java.util.TimerTask;


@EBean
public class QuickAdapter extends ArrayAdapter<PluginResultItem<?>> {
	private int mMaxItemsCount = 4;
    //private static List<PluginResultItem<?>> pkgAppsListAll;
    //private String act_query = "";
	@Pref
	ApplicationPrefs_ myPrefs;
    PluginManager manager;
	OnFindListener onFind; 
	ContactSearchPlugin plugin_contact;
	AppSearchPlugin plugin_app;
	Timer mTimer;
	long mDelay;
    public QuickAdapter(Context _ctx) {
		super(_ctx, -1);
		setNotifyOnChange(false);
		myPrefs = new ApplicationPrefs_(_ctx);
		manager = new PluginManager(this.getContext());
		manager.setOnFindListener(new OnFindListener(){
			@Override
			public void onFind(Object sender, PluginResultItem<?>... items) {
				updateItems(items);
			}});
		if(myPrefs.search_contact().get()){
			plugin_contact = new ContactSearchPlugin(this.getContext());
			manager.addPlugin(plugin_contact);
		}
		
		plugin_app = new AppSearchPlugin(this.getContext());
		manager.addPlugin(plugin_app);
	}
    
    public void setMaxCount(int count){
    	mMaxItemsCount = count;
    }
    
    public void setSearchDelay(long millseconds){
    	mDelay = millseconds;
    }
    
    public void setThreshold(int value){
    	plugin_app.setPriorityThreshold(value);
    	Log.v("QuickAdapter", "Priority threshold set to " + value);
    }
    
	@Background
	public void refreshItems(Runnable callback){
		if(plugin_app != null)
			plugin_app.refreshItems();
		if(plugin_contact != null)
			plugin_contact.refreshItems();
		safeRaise(callback);
	}
	
	@UiThread
	void safeRaise(Runnable callback){
		if(callback != null)
			callback.run();
	}
	
	@Background
	public void loadIndexedItems(Runnable callback){
		if(plugin_app != null)
			plugin_app.loadIndex();
		if(plugin_contact != null)
			plugin_contact.loadIndex();
		safeRaise(callback);
	}
	
	public void setActQuery(String query){
		if(query.length() < 2){
			if(mTimer != null)
				mTimer.cancel();
			return;
		}
		
		if(mDelay == 0){
			queryNow(query);
		}else{
			if(mTimer != null)
				mTimer.cancel();
			mTimer = new Timer();
			mTimer.schedule(getTimerTask(query), mDelay);
		}
	}
	
	@Background
	public void queryNow(String query){
		if(query == null || query.isEmpty()){
			return;
		}
		//act_query = query;
		manager.handle(query);
	}
	
	
	TimerTask getTimerTask(final String query){
		return new TimerTask(){
			public void run() {
				queryNow(query);
			}};
	}
	
	@UiThread
	void updateItems(PluginResultItem<?>... items){
		this.clear();
		int couns = Math.min(mMaxItemsCount, items.length);
		for(int i = 0; i < couns; i++){
			this.add(items[i]);
		}
		this.notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getItem(position).getView();
	}

}
