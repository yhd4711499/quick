package com.ornithopter.quick.plugins;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 
 * @author 浩东
 *
 * @param <T> 搜索数据的类型
 */
public class SearchRequest<T extends Info> implements Runnable{
	OnFindListener onFindListener;
	SearchPluginBase<T> pluginOwener;
	private boolean mIsCancelled;
	T[] items;
	String keyword;
	
	SearchRequest(
			SearchPluginBase<T> owner,
			T[] searchItems,
			String keyword){
		this.pluginOwener = owner;
		this.items = searchItems;
		this.keyword = keyword;
	}
	
	@Override
	public void run() {
		if(keyword.isEmpty())
			return;
		if(items == null)
			return;
		
		Stopwatch s = Stopwatch.start("SearchRequest.run()");
		final ArrayList<PluginResultItem<T>> matchResults = new ArrayList<PluginResultItem<T>>();
		// slower when using ThreadPool
		//final ThreadPool threadPool = new ThreadPool(100);
		
		int[] map_proto = new int[256], hitMap_proto = new int[keyword.length()];
		
		for(final T item : items){
			//threadPool.execute(new MyDelegate(item, pluginOwener, keyword, matchResults));
			
			// Using local parameter
			SearchPluginBase<T> owner = pluginOwener;
			
			SearchableField[] fields = owner.getSearchableFileds(item);
			
			// Search only valid fields
			for(SearchableField field: fields){
				if(field.getField() != null && !field.getField().isEmpty()){
					
					// Using prototypes to create instance is faster then new()
					int[] map = map_proto.clone(), hitMap = hitMap_proto.clone();

					int p = NativeMethods.isMatch(keyword, field.getField(), map, hitMap);
					
					// Filter
					if(p >= owner.getPriorityThreshold()){
						// Get history item
						String name = SearchPluginBase.getRecord(keyword);
						// Increase the priority if this item is associated to history keyword
						p = field.getPrioriry() + p + (name == null? 0 :name.equals(field.getField())? 20:0);
						
						PluginResultItem<T> result = owner.getResultItem(item, p, field.getTag());
						
						if(result != null){
							// For presenting
							result.setMap(map, hitMap);
							
							matchResults.add(result);
						}
						break;
					}
				}
			}
		}
		//threadPool.waitFinish();
		s.stop();
		
		// Sort by priority
		Collections.sort(matchResults,Collections.reverseOrder());
		
		// Get an array copy and notify
		PluginResultItem<?>[] results = new PluginResultItem<?>[matchResults.size()];
		matchResults.toArray(results);
		onFindResults(results);
		
		
	}
	
	public void setOnFindListener(OnFindListener listener){
		onFindListener = listener;
	}
	
	protected void onFindResults(PluginResultItem<?>... items){
		if(onFindListener != null){
			onFindListener.onFind(this, items);
		}
	}

	public boolean isCancelled() {
		return mIsCancelled;
	}

	public void cancel() {
		this.mIsCancelled = true;
	}
	
	@Override
	public String toString(){
		return "query : " + keyword;
	}
	
//	class MyDelegate implements Runnable{
//		T mItem;
//		SearchPluginBase<T> mPluginOwner;
//		PluginResultItem<T> mResult;
//		String mKeyword;
//		ArrayList<PluginResultItem<T>> mMatchResults;
//		public MyDelegate(T item, SearchPluginBase<T> pluginOwner, String keyword, ArrayList<PluginResultItem<T>> matchResults){
//			mItem = item;
//			mPluginOwner = pluginOwner;
//			mKeyword = keyword;
//			mMatchResults = matchResults;
//		}
//		
//		@Override
//		public void run(){
//			SearchableField[] fields = mPluginOwner.getSearchableFileds(mItem);
//			for(SearchableField field: fields){
//				if(field.getField() != null && !field.getField().isEmpty()){
//					int[] map = new int[256], hitMap = new int[mKeyword.length()];
//					int p = NativeMethods.isMatch(mKeyword, field.getField(), map, hitMap);
//					if(p >= mPluginOwner.getPriorityThreshold()){
//						String name = SearchPluginBase.getRecord(mKeyword);
//						p = field.getPrioriry() + p + (name == null? 0 :name.equals(field.getField())? 20:0);
//						mResult = mPluginOwner.getResultItem(mItem, p, field.getTag());
//						if(mResult != null){
//							mResult.setMap(map, hitMap);
//							mMatchResults.add(mResult);
//							break;
//						}else{
//							Log.w("SearchRequest", "result null! key word is :" + mKeyword);
//						}
//						break;
//					}
//				}
//			}
//		}
//		
//		public PluginResultItem<T> getResult(){
//			return mResult;
//		}
//		
//	}
}
