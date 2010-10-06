package com.barleysoft.runningmean;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class RunningMeanWindow {
	
	private int mWindowSize;
	private Queue<Float> mWindow;
	
	public RunningMeanWindow(int windowSize) {
		mWindowSize = windowSize;
		mWindow = new LinkedList<Float>();
	}
	
	public float addAndComputeMean(float value) throws WindowTooSmall {
		int n = mWindow.size();
		int cutoff = mWindowSize - 1;
		if (n < cutoff) {
			mWindow.add(value);
			throw new WindowTooSmall();
		} else if (n == cutoff) {
			mWindow.add(value);
		} else {
			mWindow.remove();
			mWindow.add(value);
		}
		
		float sum = 0;
		Iterator<Float> iter = mWindow.iterator();
		while (iter.hasNext())
			sum += iter.next();
		
		return (sum / n);			
	}
}
