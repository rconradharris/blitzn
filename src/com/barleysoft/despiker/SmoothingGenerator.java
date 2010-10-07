package com.barleysoft.despiker;

public interface SmoothingGenerator {

	public abstract float addAndCompute(float value) throws NotEnoughData;

}
