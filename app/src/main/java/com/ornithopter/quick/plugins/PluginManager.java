//package com.ornithopter.quick.plugins;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Stack;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ThreadPoolExecutor;
//
//import android.content.Context;
//import android.util.Log;
//
//public class PluginManager extends PluginBase{
//	private final static String TAG = "PluginManager";
//	
//	PluginQueueManager manager = new PluginQueueManager();
//	ArrayList<SearchPluginBase<?>> plugins;
//	Stack<SearchRequest<?>> pendingRequests;
//	ArrayList<SearchRequest<?>> workingRequests;
//	OnFindListener onFind;
//	ArrayList<PluginResultItem<?>> resultsCache;
//
//	
//	private ThreadPoolExecutor mLoaderThreadPool = (ThreadPoolExecutor)Executors.newFixedThreadPool(4);
//	
//	public PluginManager(Context c) {
//		super(c);
//		plugins = new ArrayList<SearchPluginBase<?>>();
//		workingRequests = new ArrayList<SearchRequest<?>>();
//		pendingRequests = new Stack<SearchRequest<?>>();
//		onFind = new OnFindListener(){
//			@Override
//			public void onFind(Object sender, PluginResultItem<?>... items) {
//				if(((SearchRequest<?>)sender).isCancelled()){
//					Log.v(TAG, "A cancelled search request has done : " + sender.toString());
//					return;
//				}
//				
//				if(resultsCache == null)
//					resultsCache = new ArrayList<PluginResultItem<?>>();
//				for(PluginResultItem<?> item : items){
//					resultsCache.add(item);
//				}
//				Log.v(TAG, "finished " + sender);
//				workingRequests.remove(sender);
//				if(workingRequests.size() == 0){
//					Log.v(TAG, "all finished");
//					
//					Collections.sort(resultsCache ,Collections.reverseOrder());
//					
//					PluginResultItem<?>[] results = new PluginResultItem<?>[resultsCache.size()];
//					resultsCache.toArray(results);
//					resultsCache.clear();
//					onFindResults(results);
//				}
//			}};
//	}
//
//	public void addPlugin(SearchPluginBase<?> plugin){
//		if(plugin != null){
//			plugins.add(plugin);
//			plugin.setOnFindListener(onFind);
//		}
//	}
//	
//	public void handle(String query) {
//		
//// Replace the codes below with this block 
//// to search in single thread.
////		for(SearchPluginBase<?> p:plugins){
////			p.createRequest(query).run();
////		}
//		
//		synchronized(workingRequests){
//			if(workingRequests.size() != 0){
//				Log.v(TAG, "workingRequests.size() = " + workingRequests.size());
//				for(SearchRequest<?> request : workingRequests){
//					Log.v(TAG, "cancel " + request);
//					request.cancel();
//				}
//			}
//			workingRequests.clear();
//		}
//		
//		synchronized(pendingRequests){
//			if(pendingRequests.size() != 0){
//				Log.v(TAG, "pendingRequests.size() = " + pendingRequests.size() + ". clear it.");
//				pendingRequests.clear();
//			}
//			
//			for(SearchPluginBase<?> p : plugins){
//				SearchRequest<?> request = p.createRequest(query);
//				pendingRequests.add(request);
//				Log.v(TAG, "new request added " + request);
//			}
//			
//			pendingRequests.notifyAll();
//
//			for(int i = pendingRequests.size(); i != 0; i--)
//				mLoaderThreadPool.execute(manager);
//		}
//	}
//	
//	class PluginQueueManager implements Runnable{
//		@Override
//		public void run() {
//			while(true){
//				try{
//					synchronized(pendingRequests){
//						while(pendingRequests.size() == 0)
//							pendingRequests.wait();
//					}
//					
//					if(pendingRequests.size() != 0){
//						SearchRequest<?> sr = null;
//						synchronized(pendingRequests){
//							sr = pendingRequests.pop();
//						}
//						Log.v(TAG, "starting " + sr);
//						workingRequests.add(sr);
//						sr.run();
//					}
//					
//					if(Thread.interrupted())
//						break;
//				}catch(Exception e){
//					e.printStackTrace();
//				}
//				
//			}
//		}
//		
//	}
//}

package com.ornithopter.quick.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import android.content.Context;
import android.util.Log;

public class PluginManager extends PluginBase{
	private final static String TAG = "PluginManager";
	
	private RequestRunner runner = new RequestRunner();
	private ArrayList<SearchPluginBase<?>> plugins;
	private BlockingQueue<SearchRequest<?>> pendingRequests;
	private BlockingQueue<SearchRequest<?>> workingRequests;
	private OnFindListener onFind;
	private ArrayList<PluginResultItem<?>> resultsCache;
	private CyclicBarrier barrier;

	private ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(4);
	
	public PluginManager(Context c) {
		super(c);
		plugins = new ArrayList<SearchPluginBase<?>>();
		workingRequests = new LinkedBlockingQueue<SearchRequest<?>>();
		pendingRequests = new LinkedBlockingQueue<SearchRequest<?>>();
		onFind = new OnFindListener(){
			@Override
			public void onFind(Object sender, PluginResultItem<?>... items) {
				if(((SearchRequest<?>)sender).isCancelled()){
					Log.v(TAG, "A cancelled search request has done : " + sender.toString());
					return;
				}
				
				if(resultsCache == null)
					resultsCache = new ArrayList<PluginResultItem<?>>(items.length);
				for(PluginResultItem<?> item : items){
					resultsCache.add(item);
				}
				Log.v(TAG, "finished " + sender);
				workingRequests.remove(sender);
				
				try {
					barrier.await();
				} catch (InterruptedException e) {
					Log.e(TAG, "Error in barrier.await." , e);
				} catch (BrokenBarrierException e) {
					Log.e(TAG, "Error in barrier.await.", e);
				}
			}};
	}

	public void addPlugin(SearchPluginBase<?> plugin){
		if(plugin != null){
			plugins.add(plugin);
			plugin.setOnFindListener(onFind);
		}
	}
	
	public void handle(String query) {
		
// Replace the codes below with this block 
// to search in single thread.
//		for(SearchPluginBase<?> p:plugins){
//			p.createRequest(query).run();
//		}
		
		if(workingRequests.size() != 0){
			Log.v(TAG, "workingRequests.size() = " + workingRequests.size());
			for(SearchRequest<?> request : workingRequests){
				Log.v(TAG, "cancel " + request);
				request.cancel();
			}
			workingRequests.clear();
		}
		
		if(pendingRequests.size() != 0){
			Log.v(TAG, "pendingRequests.size() = " + pendingRequests.size() + ". clear it.");
			pendingRequests.clear();
		}
		
		for(SearchPluginBase<?> p : plugins){
			SearchRequest<?> request = p.createRequest(query);
			pendingRequests.add(request);
			Log.v(TAG, "new request added " + request);
		}
		
		if (pendingRequests.size() == 0)
			return;
		
		barrier = new CyclicBarrier(pendingRequests.size(), new Runnable(){
			@Override
			public void run() {
				Log.v(TAG, "all finished");
				
				Collections.sort(resultsCache ,Collections.reverseOrder());
				
				PluginResultItem<?>[] results = new PluginResultItem<?>[resultsCache.size()];
				resultsCache.toArray(results);
				resultsCache.clear();
				onFindResults(results);
			}});
		for(int i = pendingRequests.size(); i != 0; i--)
			executor.execute(runner);
	}
	
	class RequestRunner implements Runnable{
		@Override
		public void run() {
			while(true){
				try{
					SearchRequest<?> sr = pendingRequests.take();
					while (sr != null){
						Log.v(TAG, "starting " + sr);
						workingRequests.add(sr);
						sr.run();
						sr = pendingRequests.take();
					}
					
					if(Thread.interrupted())
						break;
				}catch(Exception e){
					e.printStackTrace();
				}
				
			}
		}
		
	}
}
