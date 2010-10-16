package com.barleysoft.blitzn;

public interface ChessPlayer {
	
	void tick();
	
	void setClockResolution(long clockResolution);
	
	long getClockResolution();
	
	void setTimeLeft(long timeLeft);
	
	long getTimeLeft();

	boolean isUnderTimePressure();
	
	boolean hasTimeExpired();
	
}
