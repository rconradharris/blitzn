package com.barleysoft.blitzn;

public interface IChessClockButton {
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
	
	public void tick();
			
}
