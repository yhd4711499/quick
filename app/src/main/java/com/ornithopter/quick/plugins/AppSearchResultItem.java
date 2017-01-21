package com.ornithopter.quick.plugins;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.ornithopter.quick.R;

public class AppSearchResultItem extends PluginResultItem<AppInfo> {
	
	public AppSearchResultItem(SearchPluginBase<?> owner, AppInfo item, int priority, String hitSearchFieldTag) {
		super(owner, item, priority, hitSearchFieldTag);
	}

	@Override
	public View getView() {
		LayoutInflater inflator = LayoutInflater.from(context);
		int itemResId = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_packageName", false)?R.layout.item_app:R.layout.item_app_only_name;
		View itemView = inflator.inflate(itemResId, null);
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
				Intent i = new Intent(
						android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, 
						Uri.parse("package:" + mItem.getPackageName()));
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				v.getContext().startActivity(i);
				return true;
			}});
		
		TextView v = (TextView) itemView.findViewById(R.id.txt_Name);
		TextView v_pn = (TextView) itemView.findViewById(R.id.txt_packageName);
		
		if(getHitSearchFieldTag() == AppInfo.SEARCH_FIELD_TAG_NAME){
			v.setText(Html.fromHtml(getHtmlString(mItem.getName())));
			if(v_pn != null)
				v_pn.setText(mItem.getPackageName());
		}else{
			v.setText(mItem.getName());
			if(v_pn != null)
				v_pn.setText(Html.fromHtml(getHtmlString(mItem.getPackageName())));
		}
		
		ImageView image = (ImageView) itemView.findViewById(R.id.imageView1);
		Drawable icon = mItem.getIcon();
		image.setImageDrawable(icon);
		return itemView;
	}
	
	/*class IntPair{
		int start;
		int end;
		
		@Override
		public String toString(){
			return "{" + start +"," + end + "}";
		}
	}
	
	private String getHtmlString(String source){
		char[] chars = source.toCharArray();
		List<IntPair> pairs = new ArrayList<IntPair>();
		IntPair currentPair = null;
		List<Integer> correctedMap = new ArrayList<Integer>();
		for(int i:mMap){
			if(i == -1)
				break;
			correctedMap.add(i);
		}
		int lastHitValue = -1;
		for(int hitValue : mHitMap){
			if(currentPair == null){
				currentPair = new IntPair();
				currentPair.start = hitValue;
				currentPair.end = hitValue;
			}

			if(lastHitValue == -1){
				lastHitValue = hitValue;
				continue;
			}
			
			if(hitValue == lastHitValue){
				
			}
			else if(hitValue != lastHitValue + 1){
				pairs.add(currentPair);
				currentPair = new IntPair();
				currentPair.start = hitValue;
				currentPair.end = hitValue;
			}
			else{
				currentPair.end = hitValue;
			}
			
			lastHitValue = hitValue;
		}
		
		if(currentPair != null){
			pairs.add(currentPair);
			currentPair = null;
		}
		
		int i = 0;
		StringBuilder sb = new StringBuilder();
		for(IntPair pair : pairs){
			for(; i < pair.start; i++){
				sb.append(chars[i]);
			}
			
			sb.append(HTML_PREFIX_1);
			sb.append(colorString);
			sb.append(HTML_PREFIX_2);
			for(int ii = pair.start; ii <= pair.end; ii++){
				sb.append(chars[ii]);
			}
			sb.append(HTML_SUBFIX);
			i = pair.end + 1;
		}
		i = pairs.get(pairs.size() - 1).end + 1;
		if(i != chars.length){
			for(; i < chars.length; i++){
				sb.append(chars[i]);
			}
		}
		return sb.toString();
	}*/
	
	@Override
	public String toString(){
		return mItem.getName();
	}

	@Override
	protected void doSelect() {
		Intent i = new Intent();
		PackageManager manager = context.getPackageManager();
		i = manager.getLaunchIntentForPackage(mItem.getPackageName());
		i.addCategory(Intent.CATEGORY_LAUNCHER);
		context.startActivity(i);
		System.exit(0);
	}
}
