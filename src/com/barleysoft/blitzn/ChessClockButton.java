package com.barleysoft.blitzn;

import com.barleysoft.blitzn.chessclock.ChessClock;
import com.barleysoft.blitzn.chessclock.ChessPlayer;

public interface ChessClockButton {
	
	public void setChessClock(ChessClock chessClock);
	
	public ChessClock getChessClock();
	
	public void setChessPlayer(ChessPlayer chessPlayer);
	
	public ChessPlayer getChessPlayer();
	
	public void setIsSoundEnabled(boolean isSoundEnabled);
	
	public boolean getIsSoundEnabled();	
	
	public void setIsTimePressureWarningEnabled(boolean isTimePressureWarningEnabled);
	
	public boolean getIsTimePressureWarningEnabled();
	
	public void setIsFlipped(boolean isFlipped);
	
	public boolean getIsFlipped();
	
	public void initialize();
	
	public void activate();
	
	public void deactivate();
	
	public void reset();
	
	public void stop();
	
	public void tick();
	
	public void setClockResolution(long clockResolution);
	
	public long getClockResolution();
			
}
