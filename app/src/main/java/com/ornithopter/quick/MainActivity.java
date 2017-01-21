package com.ornithopter.quick;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.ornithopter.quick.plugins.NativeMethods;
import com.ornithopter.quick.plugins.OnFindListener;
import com.ornithopter.quick.plugins.PluginManager;
import com.ornithopter.quick.plugins.QuickAdapter;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EActivity
public class MainActivity extends Activity{
	public static MainActivity instance;
	
	PluginManager manager;
	OnFindListener onFind;
	
	@Pref
	ApplicationPrefs_ myPrefs;
	
	@Bean
	QuickAdapter mAdapter;
	
	//@ViewById
	EditText editText_Search;
	
	@ViewById
	AbsListView listView_SearchResults;

	@ViewById
	View layout_keyboard;

	boolean isDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(myPrefs.theme_dialog().get()){
			isDialog = true;
			this.setContentView(R.layout.activity_main_dialog);
			editText_Search = (EditText) this.findViewById(R.id.editText_Search);
		}else{
			isDialog = false;
			this.setTheme(R.style.AppBaseTheme);
			this.setContentView(R.layout.activity_main);
			editText_Search = new EditText(this);
			editText_Search.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			editText_Search.setHint(R.string.loading_IndexCache);
			this.getActionBar().setDisplayShowCustomEnabled(true);  
			this.getActionBar().setCustomView(editText_Search);
		}
		initiate();
		instance = this;
	}
	
	//@AfterViews
	void initiate(){
		if(!isDialog)
			mAdapter.setMaxCount(999);
		
		mAdapter.loadIndexedItems(new Runnable(){
			@Override
			public void run() {
				editText_Search.setEnabled(true);
				editText_Search.setHint(MainActivity.this.getText(R.string.menu_search));
			}});

		if(myPrefs.is_first_run().get()) {
			myPrefs.edit().is_first_run().put(false).apply();
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            View v = this.getLayoutInflater().inflate(R.layout.view_welcom, null);
            alert.setView(v)
            	.setTitle("Quick")
            	.setPositiveButton(this.getString(android.R.string.ok), null)
            	.create()
            	.show();
        }

		listView_SearchResults.setAdapter(mAdapter);
	    editText_Search.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				mAdapter.setActQuery(NativeMethods.removeSpace(s.toString()));
			}
	    });
	    editText_Search.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
					if(!mAdapter.isEmpty())
						mAdapter.getItem(0).performSelect();
				}
				return false;
			}});
	    initiateKeyboard();
	}

	void initiateKeyboard(){
		if(myPrefs.theme_dialog().get()){
			
		}else{
			KeyboardListener k = new KeyboardListener();
			this.findViewById(R.id.key_2).setOnClickListener(k);
			this.findViewById(R.id.key_3).setOnClickListener(k);
			this.findViewById(R.id.key_4).setOnClickListener(k);
			this.findViewById(R.id.key_5).setOnClickListener(k);
			this.findViewById(R.id.key_6).setOnClickListener(k);
			this.findViewById(R.id.key_7).setOnClickListener(k);
			this.findViewById(R.id.key_8).setOnClickListener(k);
			this.findViewById(R.id.key_9).setOnClickListener(k);
			
			this.findViewById(R.id.key_delete).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					editText_Search.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
				}});
			
			
			this.findViewById(R.id.key_delete).setOnLongClickListener(new OnLongClickListener(){

				@Override
				public boolean onLongClick(View arg0) {
					editText_Search.getText().clear();
					return true;
				}});
			this.findViewById(R.id.key_clear).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					editText_Search.getText().clear();
				}});
			this.findViewById(R.id.key_enter).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					if(!mAdapter.isEmpty())
						mAdapter.getItem(0).performSelect();
				}});
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@OptionsItem(R.id.action_settings)
	void openSettingActivity() {
        this.startActivity(new Intent(this, SettingsActivity.class));
	}
	
	@Override
	public void onResume(){
		mAdapter.refreshItems(null);
		mAdapter.setSearchDelay(Integer.parseInt(myPrefs.search_delay().get()));
		mAdapter.setThreshold(Integer.parseInt(myPrefs.search_threshold().get()));
		
		if(isDialog){
			editText_Search.postDelayed(new Runnable() {
	            @Override
	            public void run() {
	                InputMethodManager keyboard = (InputMethodManager)
	                getSystemService(Context.INPUT_METHOD_SERVICE);
	                keyboard.showSoftInput(editText_Search, 0);
	            }
	        },200);
		}
		super.onResume();
	}
	
	public static void focusOn(View v){
		MotionEvent me1 = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0);
    	MotionEvent me2 = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0);
    	v.dispatchTouchEvent(me1);
    	v.dispatchTouchEvent(me2);
    	if(v instanceof EditText)
    		((EditText)v).selectAll();
    	me1.recycle();
    	me2.recycle();
    	me1 = null;
    	me2 = null;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);

		if (layout_keyboard == null) {
			return;
		}
		if (newConfig.keyboardHidden == Configuration.KEYBOARDHIDDEN_NO) {
			layout_keyboard.setVisibility(View.GONE);
	    } else if (newConfig.keyboardHidden == Configuration.KEYBOARDHIDDEN_YES) {
			layout_keyboard.setVisibility(View.VISIBLE);
	    }
	}
	
	class KeyboardListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			editText_Search.getText().append(v.getTag().toString());
		}
	}
}
