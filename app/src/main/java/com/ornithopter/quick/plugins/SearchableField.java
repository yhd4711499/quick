package com.ornithopter.quick.plugins;

public class SearchableField {
	private int mPrioriry;
	private String mField;
	private String mTag;
	
	public SearchableField(String field, int priority, String tag){
		mField = field;
		mPrioriry = priority;
		mTag = tag;
	}

	public int getPrioriry() {
		return mPrioriry;
	}

	public void setPrioriry(int mPrioriry) {
		this.mPrioriry = mPrioriry;
	}

	public String getField() {
		return mField;
	}

	public void setField(String mField) {
		this.mField = mField;
	}
	
	public String getTag() {
		return mTag;
	}

	public void setTag(String tag) {
		this.mTag = tag;
	}
}
