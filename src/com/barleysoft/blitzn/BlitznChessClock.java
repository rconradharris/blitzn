package com.barleysoft.blitzn;

public class BlitznChessClock implements ChessClock {

	public static final int CLOCK_RESOLUTION = 100; // ms

	private ClockState mClockState = ClockState.NOSTATE;
	private ChessPlayer mPlayer1 = new BlitznChessPlayer();
	private ChessPlayer mPlayer2 = new BlitznChessPlayer();
	private long mDuration = 0L;
	private long mTicks = 0L;

	private void setState(ClockState state) {
		mClockState = state;
		// TODO callback onChessClockStateChange(ClockState state)
	}

	public void tick() {
		if (!isStarted()) {
			return;
		}

		if (mPlayer1.hasTimeExpired() || mPlayer2.hasTimeExpired()) {
			setState(ClockState.STOPPED);
			return;
		}

		mTicks++;

		switch (mClockState) {
		case PLAYER1_RUNNING:
			mPlayer1.tick();
		case PLAYER2_RUNNING:
			mPlayer2.tick();
		}
	}

	public void setDuration(long duration) {
		mDuration = duration;
	}

	public long getDuration() {
		return mDuration;
	}

	public void initialize() {
		mPlayer1.setClockResolution(CLOCK_RESOLUTION);
		mPlayer2.setClockResolution(CLOCK_RESOLUTION);
	}

	public void reset() {
		mTicks = 0L;
		mPlayer1.setTimeLeft(mDuration);
		mPlayer2.setTimeLeft(mDuration);
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
			case PLAYER2_RUNNING:
				setState(ClockState.PLAYER1_RUNNING);
			}
		} else {
			switch (mClockState) {
			case READY:
				setState(ClockState.PLAYER1_RUNNING);
			case PLAYER1_RUNNING:
				setState(ClockState.PLAYER2_RUNNING);
			}
		}
	}

}
