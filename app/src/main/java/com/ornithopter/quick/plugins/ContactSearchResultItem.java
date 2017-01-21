package com.ornithopter.quick.plugins;

import android.net.Uri;
import android.provider.ContactsContract;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.ornithopter.quick.R;

public class ContactSearchResultItem extends PluginResultItem<ContactInfo> {
	public ContactSearchResultItem(SearchPluginBase<?> owner, ContactInfo item, int priority, String hitSearchFieldTag) {
		super(owner, item, priority, hitSearchFieldTag);
	}
	View mItemView;
	@Override
	public View getView() {
		LayoutInflater inflator = LayoutInflater.from(context);
		View itemView = inflator.inflate(R.layout.item_app, null);
		itemView.setClickable(true);
		itemView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				performSelect();
			}});
		itemView.setLongClickable(true);
		itemView.setOnLongClickListener(new OnLongClickListener(){

			@Override
			public boolean onLongClick(View v) {
				performSelect();
				return true;
			}});
		TextView v = (TextView) itemView.findViewById(R.id.txt_Name);
		v.setText(Html.fromHtml(getHtmlString(mItem.Name)));
		
		ImageView image = (ImageView) itemView.findViewById(R.id.imageView1);
//		UrlImageViewHelper.setUrlDrawable(image, mItem.PhotoUri, R.drawable.contact_photo_dummy_male);
		mItemView = itemView;
		return itemView;
	}

	@Override
	public String toString(){
		return mItem.Name;
	}

	@Override
	protected void doSelect() {
		ContactsContract.QuickContact.showQuickContact(context, mItemView, Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, mItem.LookupKey), 2, null);
	}
}
