package com.barleysoft.despiker;

import java.util.LinkedList;
import java.util.Queue;

public class MovingAverageGenerator implements SmoothingGenerator {

	private int mMaxSize;
	private Queue<Float> mQueue;
	private float mSum = 0;
	private int mNumItems = 0;

	public MovingAverageGenerator(int maxSize) {
		mMaxSize = maxSize;
		mQueue = new LinkedList<Float>();
	}

	private void addRight(float value) {
		mQueue.offer(value);
		mSum += value;
		mNumItems++;
	}

	private float popLeft() {
		float value = mQueue.poll();
		mSum -= value;
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
		return (mSum / mNumItems);
	}
}
