package com.barleysoft.despiker;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RunningMedianGenerator implements SmoothingGenerator {
	// This is a fairly inefficient O(n) implementation of Running Median
	// We could do better with skip-lists or deques.
	// See Raymond Hettinger's work on ASPN
	// http://code.activestate.com/recipes/576930/
	// http://code.activestate.com/recipes/577059-running-median/
	private int mNumItems = 0;
	private int mMaxSize;
	private Queue<Float> mQueue;
	private List<Float> mSorted;

	public RunningMedianGenerator(int maxSize) {
		mMaxSize = maxSize;
		mQueue = new LinkedList<Float>();
		mSorted = new LinkedList<Float>();
	}

	private void insertSorted(float value) {
		Iterator<Float> it = mSorted.iterator();
		int i = 0;
		while (it.hasNext() && (it.next() < value))
			i++;
		mSorted.add(i, value);
	}

	private void addRight(float value) {
		mQueue.offer(value);
		insertSorted(value);
		mNumItems++;
	}

	private float popLeft() {
		float value = mQueue.poll();
		mSorted.remove(value);
		mNumItems--;
		return value;
	}

	public float addAndCompute(float value) throws NotEnoughData {
		if (mNumItems < mMaxSize) {
			addRight(value);
			throw new NotEnoughData();
		}
		popLeft();
		addRight(value);
		return mSorted.get(mNumItems / 2);
	}

}
