package com.ornithopter.quick.plugins;


public class ContactInfo extends Info{
	public String Address;
	public String PhotoUri;
	public Long _id;
	public String LookupKey;
	
	public ContactInfo(String cacheStr){
		super(cacheStr);
	}
	
	public ContactInfo(){
	}
	
	@Override
	String[] getInfo() {
		return null;
	}

	@Override
	void fillInfoFromCache(String[] splits) {
	}
}
