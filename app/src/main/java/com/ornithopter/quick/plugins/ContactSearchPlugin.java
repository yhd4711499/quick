package com.ornithopter.quick.plugins;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;

public class ContactSearchPlugin extends SearchPluginBase<ContactInfo> {
	private final static String CACHE_NAME = "contactsearch";
	
	private static final String[] PROJECTION = new String[] {
		Contacts._ID,
		Contacts.DISPLAY_NAME,
		Contacts.LOOKUP_KEY
		}; 
	
	public ContactSearchPlugin(Context c) {
		super(c);
	}

	@Override
	protected SearchableField[] getSearchableFileds(ContactInfo obj) {
		SearchableField name = new SearchableField(obj.Name, 0, ContactInfo.SEARCH_FIELD_TAG_NAME);
		return new SearchableField[]{ name };
	}

	@Override
	protected PluginResultItem<ContactInfo> getResultItem(ContactInfo item, int priority, String hitSearchFieldTag) {
		return new ContactSearchResultItem(this, item, priority, hitSearchFieldTag);
	}
	
	@Override
	protected ContactInfo[] doGetItems() {
		Cursor phoneCursor;
		try {
			phoneCursor = context.getContentResolver().query(Contacts.CONTENT_URI, PROJECTION, Contacts.HAS_PHONE_NUMBER + " = 1", null, null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		ArrayList<ContactInfo> array = new ArrayList<ContactInfo>(phoneCursor.getCount());
		
		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {

				String name = phoneCursor.getString(1);
				if(name == null || name.isEmpty())
					continue;
				
				long contactId = phoneCursor.getLong(0);
				Uri photoUri = ContentUris.withAppendedId(Contacts.CONTENT_URI,contactId);
				ContactInfo info = new ContactInfo();
				info._id = contactId;
				info.LookupKey = phoneCursor.getString(2);
				info.Name = name;
				info.PhotoUri = photoUri.toString();
				array.add(info);
			}
		}
		phoneCursor.close();
		phoneCursor = null;
		ContactInfo[] infos = new ContactInfo[array.size()];
		return array.toArray(infos);
	} 

	@Override
	protected ContactInfo[] getArray(List<ContactInfo> list) {
		ContactInfo[] infos = new ContactInfo[list.size()];
		return list.toArray(infos);
	}

	@Override
	protected ContactInfo readItemCache(Context context, String cache) {
		return new ContactInfo(cache);
	}

	@Override
	protected String getCacheFileName() {
		return CACHE_NAME;
	}
}
