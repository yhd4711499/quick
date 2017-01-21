package com.ornithopter.quick;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class AccessibilityUtils {
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
}
