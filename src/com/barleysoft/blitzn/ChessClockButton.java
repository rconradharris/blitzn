package com.barleysoft.blitzn;

import com.barleysoft.blitzn.chessclock.ChessClock;
import com.barleysoft.blitzn.chessclock.ChessPlayer;

public interface ChessClockButton {
	
	void setChessClock(ChessClock chessClock);
	
	ChessClock getChessClock();
	
	void setChessPlayer(ChessPlayer chessPlayer);
	
	ChessPlayer getChessPlayer();
	
	void setIsSoundEnabled(boolean isSoundEnabled);
	
	boolean getIsSoundEnabled();	
	
	void setIsTimePressureWarningEnabled(boolean isTimePressureWarningEnabled);
	
	boolean getIsTimePressureWarningEnabled();
	
	void setIsFlipped(boolean isFlipped);
	
	boolean getIsFlipped();
	
	void initialize();
	
	void activate();
	
	void deactivate();
	
	void reset();
	
	void stop();
	
	void tick();
	
	void setClockResolution(long clockResolution);
	
	long getClockResolution();
			
}
