package com.ornithopter.quick.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * 
 * @author 浩东
 *
 * @param <T> 搜索数据的类型
 */
public abstract class SearchPluginBase<T extends Info> extends PluginBase {
	final static boolean useRecords = true;
	final static String PREF_NAME = "pref_records";
	final static HashMap<String,String> records = new HashMap<String,String>();
	final static String SEPRATOR = "\n";
	static SharedPreferences preferences;
	
	private int mPriorityThreshold = 1;
	
	private SharedPreferences getPreferences(){
		if(preferences == null)
			preferences = context.getSharedPreferences(
					PREF_NAME, Context.MODE_PRIVATE);
		return preferences;
	}
	
	File mIndexFile;
	private File getIndexFile(){
		if(mIndexFile == null){
			mIndexFile = new File(context.getCacheDir(), getCacheFileName() + "_cache.csv");
		}
		return mIndexFile;
	}
	
	String lastQuery;
	T[] mCachedItems;
	Object cacheItemLockObj = new Object();

	
	void readRecords(){
		if(!useRecords)
			return;

        Map<String, ?> map = getPreferences().getAll();
        for(Entry<String,?> item : map.entrySet()){
        	records.put(item.getKey().toString(), item.getValue().toString());
        }
	}
	
	void writeRecords(){
		if(!useRecords)
			return;
		
		SharedPreferences.Editor editor = getPreferences().edit();
		for(Entry<String, String> item : records.entrySet()){
			editor.putString(item.getKey(), item.getValue());
		}
		editor.commit();
	}
	
	void recordItem(String query, String name){
		if(!useRecords)
			return;

		records.put(query, name);
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putString(query, name);
		editor.commit();
		Log.v("SearchPluginBase","recordItem(" + query + "," + name + ")");
	}
	
	static String getRecord(String query){
		String name = records.get(query);
		if(useRecords){
			return name;
		}
		else
			return null;
	}
	
	public void refreshItems(){
		synchronized(cacheItemLockObj){
			mCachedItems = doGetItems();
		}
		if(mCachedItems == null)
			return;
		StringBuilder cacheStr = new StringBuilder();
		for(T item : mCachedItems){
			cacheStr.append(item.toString());
			cacheStr.append(SEPRATOR);
		}
		writeIndex(cacheStr.toString());
	}
	
	private void writeIndex(String cacheStr){
		File f = getIndexFile();
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(f);
			stream.write(cacheStr.getBytes());
			Log.v("SearchPluginBase", "items indexed successfully.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(stream != null){
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void loadIndex(){
		try {
			T[] items = readIndexString(file2String(getIndexFile()));
			if(items != null){
				mCachedItems = items;
				Log.v("SearchPluginBase", "load index successfully.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private static String file2String(File file) throws IOException {
		if(!file.exists())
			return null;
        FileInputStream stream = new FileInputStream(file);
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
                    fc.size());
            fc.close();
            stream.close();
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }

    }
	
	private T[] readIndexString(String inString){
        if (inString == null) {
            return null;
        }
        List<T> res = new ArrayList<T>();
        String[] lines = inString.split(SEPRATOR);
        for (String line : lines) {
            if (line.length() > 0) {
                T item = readItemCache(context, line);
                if(item != null)
                	res.add(item);
            }

        }

        return getArray(res);
	}
	
	protected abstract T[] getArray(List<T> list);
	protected abstract T readItemCache(Context context, String cache);
	protected abstract String getCacheFileName();
	
// Patterns are moved into C lib.
//	final static MatchPattern[] patterns = new MatchPattern[]{
//		new MatchPattern(MatchPattern.STARTWITH),
//		new MatchPattern(MatchPattern.INITIAL),
//		new MatchPattern(MatchPattern.LOOSE),
//		};
	
	public SearchPluginBase(Context c) {
		super(c);
		readRecords();
	}

	public SearchRequest<T> createRequest(String query){
		lastQuery = query;
		SearchRequest<T> sr = new SearchRequest<T>(this, getItems(), lastQuery);
		sr.setOnFindListener(onFindListener);
		return sr;
	}
	
	/**
	 * 从搜索数据的类型的对象中获取搜索字段
	 * @param 结果对象
	 * @return 搜索字段
	 */
	protected abstract SearchableField[] getSearchableFileds(T obj);

	/**
	 * 获取搜索的范围
	 * @return 搜索的范围
	 */
	protected abstract T[] doGetItems();
	
	public T[] getItems(){
		if(mCachedItems == null)
			mCachedItems = doGetItems();
		return mCachedItems;
	}
	
	/**
	 * 将匹配结果转换为{@link PluginResultItem<T>}
	 * @param item
	 * @return 转换后的{@link PluginResultItem<T>}
	 */
	protected abstract PluginResultItem<T> getResultItem(T item, int priority, String hitSearchFieldTag);

	public int getPriorityThreshold() {
		return mPriorityThreshold;
	}

	public void setPriorityThreshold(int mPriorityThreshold) {
		this.mPriorityThreshold = mPriorityThreshold;
	}
	
	
}
