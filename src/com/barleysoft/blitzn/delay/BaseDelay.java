package com.barleysoft.blitzn.delay;

import com.barleysoft.blitzn.BlitznChessClock;
import com.barleysoft.blitzn.BlitznChessClock.Player;

public abstract class BaseDelay implements Delay {
	private long mDelay;
	private long mPlayer1DelayLeft;
	private long mPlayer2DelayLeft;
	protected BlitznChessClock mChessClock;

	BaseDelay(BlitznChessClock chessClock) {
		mChessClock = chessClock;
	}

	public void setDelay(long delay) {
		mDelay = delay;
	}

	public long getDelay() {
		return mDelay;
	}

	public long getDelayLeftForPlayer(Player player) {
		if (player == Player.ONE)
			return mPlayer1DelayLeft;
		else
			return mPlayer2DelayLeft;
	}

	public void resetDelayForPlayer(Player player) {
		if (player == Player.ONE)
			mPlayer1DelayLeft = getDelay();
		else
			mPlayer2DelayLeft = getDelay();
	}

	abstract public void tickForPlayer(Player player);

	abstract public void startDelayForPlayer(Player player);

	abstract public void stopDelayForPlayer(Player player);

	protected void setDelayLeftForPlayer(Player player, long delayLeft) {
		if (player == Player.ONE)
			mPlayer1DelayLeft = delayLeft;
		else
			mPlayer2DelayLeft = delayLeft;
	}

	protected void adjustDelayLeftForPlayer(Player player, long adjustment) {
		setDelayLeftForPlayer(player, getDelayLeftForPlayer(player)
				+ adjustment);
	}

	protected long getClockResolution() {
		return BlitznChessClock.CLOCK_RESOLUTION;
	}

	protected long getTimeLeftForPlayer(Player player) {
		return mChessClock.getTimeLeftForPlayer(player);
	}

	protected void adjustTimeForPlayer(Player player, long adjustment) {
		mChessClock.adjustTimeLeftForPlayer(player, adjustment);
	}

}
