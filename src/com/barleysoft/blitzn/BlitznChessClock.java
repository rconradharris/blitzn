package com.barleysoft.blitzn;

public class BlitznChessClock implements ChessClock {


	private ClockState mClockState = ClockState.NOSTATE;
	private DelayMode mDelayMode = DelayMode.NODELAY;
	
	private ChessPlayer mChessPlayer1;
	private ChessPlayer mChessPlayer2;

	private long mClockResolution;
	private long mDuration = 0L;
	private long mTicks = 0L;
	
	private long mDelayTime;

	private void setState(ClockState state) {
		mClockState = state;
		// TODO callback onChessClockStateChange(ClockState state)
	}

	public void tick() {
		if (!isStarted()) {
			return;
		}

		if (mChessPlayer1.hasTimeExpired() || mChessPlayer2.hasTimeExpired()) {
			setState(ClockState.STOPPED);
			return;
		}

		mTicks++;

		switch (mClockState) {
		case PLAYER1_RUNNING:
			mChessPlayer1.tick();
		case PLAYER2_RUNNING:
			mChessPlayer2.tick();
		}
	}

	public void setDuration(long duration) {
		mDuration = duration;
		mChessPlayer1.setDuration(duration);
		mChessPlayer2.setDuration(duration);
	}

	public long getDuration() {
		return mDuration;
	}

	public void initialize() {
		mChessPlayer1.initialize();		
		mChessPlayer2.initialize();
		reset();
	}

	public void reset() {
		mTicks = 0L;
		mChessPlayer1.reset();
		mChessPlayer2.reset();
		setState(ClockState.READY);
	}

	public long getTicks() {
		return mTicks;
	}

	public void pause() {
		switch (mClockState) {
		case PLAYER1_RUNNING:
			setState(ClockState.PLAYER1_PAUSED);
		case PLAYER2_RUNNING:
			setState(ClockState.PLAYER2_PAUSED);
		}
	}

	public void unpause() {
		switch (mClockState) {
		case PLAYER1_PAUSED:
			setState(ClockState.PLAYER1_RUNNING);
		case PLAYER2_PAUSED:
			setState(ClockState.PLAYER2_RUNNING);
		}
	}

	public boolean isPaused() {
		switch (mClockState) {
		case PLAYER1_PAUSED:
		case PLAYER2_PAUSED:
			return true;
		}
		return false;
	}

	public boolean isStarted() {
		switch (mClockState) {
		case NOSTATE:
		case READY:
		case STOPPED:
			return false;
		}
		return true;
	}

	public boolean isReady() {
		return mClockState == ClockState.READY;
	}

	public void activatePlayer(Player player) {
		if (player == Player.ONE) {
			switch (mClockState) {
			case READY:
				// The first move is untimed, so when player 1 clicks, we
				// actually begin counting down for player 2
				setState(ClockState.PLAYER2_RUNNING);
				mChessPlayer2.activate();
			case PLAYER2_RUNNING:
				setState(ClockState.PLAYER1_RUNNING);
				mChessPlayer2.deactivate();
				mChessPlayer1.activate();
			}
		} else {
			switch (mClockState) {
			case READY:
				setState(ClockState.PLAYER1_RUNNING);
				mChessPlayer1.activate();
			case PLAYER1_RUNNING:
				setState(ClockState.PLAYER2_RUNNING);
				mChessPlayer1.deactivate();
				mChessPlayer2.activate();
			}
		}
	}

	public void setChessPlayer(Player player, ChessPlayer chessPlayer) {
		if (player == Player.ONE) {
			mChessPlayer1 = chessPlayer;
		} else {
			mChessPlayer2 = chessPlayer;
		}		
	}

	public ChessPlayer getChessPlayer(Player player) {
		if (player == Player.ONE) {
			return mChessPlayer1;
		} else {
			return mChessPlayer2;
		}
	}

	public void setDelayMode(DelayMode delayMode) {
		mDelayMode = delayMode;
		mChessPlayer1.setDelayMode(delayMode);
		mChessPlayer2.setDelayMode(delayMode);
	}

	public DelayMode getDelayMode() {
		return mDelayMode;
	}

	public void setDelayTime(long delayTime) {
		mDelayTime = delayTime;
		mChessPlayer1.setDelayTime(delayTime);
		mChessPlayer2.setDelayTime(delayTime);
	}

	public long getDelayTime() {
		return mDelayTime;

	}

	public long getClockResolution() {
		return mClockResolution;
	}

	public void setClockResolution(long clockResolution) {
		mClockResolution = clockResolution;
		mChessPlayer1.setClockResolution(clockResolution);
		mChessPlayer2.setClockResolution(clockResolution);		
	}

}
