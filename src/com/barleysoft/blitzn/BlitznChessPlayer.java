package com.barleysoft.blitzn;

public class BlitznChessPlayer implements ChessPlayer {
	public static final long TIME_PRESSURE_THRESHOLD = 10 * 1000; // ms
	private long mClockResolution = 0L;
	private long mTimeLeft = 0L;

	public void tick() {
		mTimeLeft -= mClockResolution;		
	}

	public void setTimeLeft(long timeLeft) {
		mTimeLeft = timeLeft;		
	}

	public long getTimeLeft() {
		return mTimeLeft;
	}

	public boolean isUnderTimePressure() {
		return mTimeLeft <= TIME_PRESSURE_THRESHOLD;
	}

	public void setClockResolution(long clockResolution) {
		mClockResolution = clockResolution;		
	}

	public long getClockResolution() {
		return mClockResolution;
	}

	public boolean hasTimeExpired() {
		return mTimeLeft < mClockResolution;
	}

}
