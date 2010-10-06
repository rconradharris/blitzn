package com.barleysoft.runningmean;

import java.util.LinkedList;
import java.util.Queue;

public class RunningMeanWindow {

	private int mWindowSize;
	private Queue<Float> mWindow;
	private float mSum = 0;

	public RunningMeanWindow(int windowSize) {
		mWindowSize = windowSize;
		mWindow = new LinkedList<Float>();
	}

	private void addValue(float value) {
		mWindow.add(value);
		mSum += value;
	}

	private float removeValue() {
		float value = mWindow.remove();
		mSum -= value;
		return value;
	}

	public float addAndComputeMean(float value) throws WindowTooSmall {
		addValue(value);
		int n = mWindow.size();
		if (n > mWindowSize) {
			removeValue();
		} else if (n < mWindowSize) {
			throw new WindowTooSmall();
		}
		return (mSum / n);
	}
}
