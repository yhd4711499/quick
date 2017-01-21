package com.ornithopter.quick;


import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(value= SharedPref.Scope.APPLICATION_DEFAULT)
public interface ApplicationPrefs {

	@DefaultBoolean(false)
	boolean show_packagename();
	
	@DefaultBoolean(false)
	boolean search_packagename();
	
	@DefaultString("300")
	String search_delay();
	
	@DefaultBoolean(true)
	boolean is_first_run();
	
	@DefaultBoolean(false)
	boolean theme_dialog();
	
	@DefaultString("1")
	String search_threshold();
	
	@DefaultBoolean(false)
	boolean search_contact();
}
