package com.ornithopter.quick.plugins;

public class Stopwatch {
	private String mDescription;
	private long mStartNanoseconds;
	
	public static Stopwatch start(String description){
		Stopwatch s = new Stopwatch();
		s.mDescription = description;
		s.start();
		return s;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String mDescription) {
		this.mDescription = mDescription;
	}
	
	public void start(){
		mStartNanoseconds = System.nanoTime();
	}
	
	public double stop(){
		double comsumed = (System.nanoTime() - mStartNanoseconds ) / 1000000.0;
		System.out.println(mDescription + " cost " + comsumed + " millseconds");
		return comsumed;
	}
}
