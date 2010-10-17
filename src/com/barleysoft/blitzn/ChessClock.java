package com.barleysoft.blitzn;


public interface ChessClock {
	// NOTE(sirp); Android optimization document[1] considers enum bad since
	// they add overhead. They recommend using a static final int instead.
	// I am purposefully ignoring this advice since performance isn't
	// critical here, and writing correct code is.
	//
	// [1]
	// http://developer.android.com/guide/practices/design/performance.html#avoid_enums
	public enum Player {
		ONE, TWO
	}
	
	public enum ClockState {
		NOSTATE, READY, PLAYER1_RUNNING, PLAYER2_RUNNING, PLAYER1_PAUSED, PLAYER2_PAUSED, STOPPED
	}
	
	public enum DelayMode {
		NODELAY, FISCHER, BRONSTEIN;
		
		static public DelayMode fromOrdinal(int ordinal) {
			switch(ordinal) {
			case 1:
				return FISCHER;
			case 2:
				return BRONSTEIN;
			default:
				return NODELAY;
			}
		}
	}
	
	void tick();
	
	void setChessPlayer(Player player, ChessPlayer chessPlayer);
	
	ChessPlayer getChessPlayer(Player player);
	
	void setDuration(long duration);
	
	long getDuration();
	
	void setDelayMode(DelayMode delayMode);
	
	DelayMode getDelayMode();
	
	void setDelayTime(long delayTime);
	
	long getDelayTime();
	
	void initialize();
	
	void reset();
	
	long getTicks();
	
	void deactivatePlayer(Player player);
	
	void pause();
	
	void unpause();
	
	boolean isPaused();
	
	boolean isStarted();
	
	boolean isReady();
	
	boolean isRunning();
	
	boolean isRunningForPlayer(Player player);
	
	void setClockResolution(long clockResolution);
	
	long getClockResolution();
	
	void setOnChessClockStopListener(OnChessClockStopListener listener);

}
