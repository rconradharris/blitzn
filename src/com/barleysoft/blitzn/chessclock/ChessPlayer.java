package com.barleysoft.blitzn.chessclock;

import com.barleysoft.blitzn.chessclock.ChessClock.DelayMode;


public interface ChessPlayer {
	
	void initialize();
	
	void reset();
	
	void activate();
	
	void deactivate();
	
	void tick();
	
	void setClockResolution(long clockResolution);
	
	long getClockResolution();
	
	void setTimeLeft(long timeLeft);
	
	long getTimeLeft();

	boolean isUnderTimePressure();
	
	boolean hasTimeExpired();
	
	void setDelayMode(DelayMode delayMode);
	
	DelayMode getDelayMode();
	
	void setDelayTime(long delayTime);
	
	long getDelayTime();
	
	long getDelayLeft();
	
	void setDuration(long duration);
	
	long getDuration();
	
}
