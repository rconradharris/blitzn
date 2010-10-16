package com.barleysoft.blitzn;

import com.barleysoft.blitzn.ChessClock.DelayMode;

public class BlitznChessPlayer implements ChessPlayer {
	public static final long TIME_PRESSURE_THRESHOLD = 10 * 1000; // ms
	
	private long mClockResolution = 0L;
	private long mDuration = 0L;
	private DelayMode mDelayMode = DelayMode.NODELAY;
	private long mDelayTime = 0L;

	private long mTimeLeft = 0L;
	private long mDelayLeft = 0L;
	
	public void tick() {
		if ((mDelayLeft >= mClockResolution) && (mDelayMode != DelayMode.NODELAY)) {
			mDelayLeft -= mClockResolution;
			if (mDelayLeft < mClockResolution) {
				mDelayLeft = 0;
			}
			return;
		}
		
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

	public void activate() {
		if (mDelayMode == DelayMode.FISCHER) {
			mTimeLeft += mDelayTime;
		}		
	}

	public void deactivate() {
		if (mDelayMode == DelayMode.BRONSTEIN) {
			// Add back to player's clock any remaining delay
			mTimeLeft += (mDelayTime - mDelayLeft);
			mDelayLeft = mDelayTime;
		}		
	}

	public void setDelayMode(DelayMode delayMode) {
		mDelayMode = delayMode;		
	}

	public DelayMode getDelayMode() {
		return mDelayMode;
	}

	public void setDelayTime(long delayTime) {
		mDelayTime = delayTime;		
	}

	public long getDelayTime() {
		return mDelayTime;
	}

	public long getDelayLeft() {
		return mDelayLeft;
	}

	public void reset() {
		mTimeLeft = mDuration;
		mDelayLeft = mDelayTime;		
	}

	public void setDuration(long duration) {
		mDuration = duration;
		
	}

	public long getDuration() {
		return mDuration;
	}

	public void initialize() {
		// TODO Auto-generated method stub
		
	}

}
