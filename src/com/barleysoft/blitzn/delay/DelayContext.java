package com.barleysoft.blitzn.delay;

import com.barleysoft.blitzn.BlitznChessClock;
import com.barleysoft.blitzn.BlitznChessClock.Player;

public class DelayContext {
	private DelayStrategy mDelayStrategy;
	private long mDelay;
	private long mPlayer1DelayLeft;
	private long mPlayer2DelayLeft;
	protected BlitznChessClock mChessClock;

	public DelayContext(BlitznChessClock chessClock, DelayStrategy delayStrategy) {
		mChessClock = chessClock;
		mDelayStrategy = delayStrategy;
	}

	public void setStrategy(DelayStrategy delayStrategy) {
		mDelayStrategy = delayStrategy;
	}

	public boolean isDelayEnabled() {
		return !(mDelayStrategy instanceof NoneDelayStrategy);
	}

	public DelayStrategy getStrategy() {
		return mDelayStrategy;
	}

	public void setDelay(long delay) {
		mDelay = delay;
	}

	public long getDelay() {
		return mDelay;
	}

	public void setDelayLeftForPlayer(Player player, long delayLeft) {
		if (player == Player.ONE)
			mPlayer1DelayLeft = delayLeft;
		else
			mPlayer2DelayLeft = delayLeft;
	}

	public long getDelayLeftForPlayer(Player player) {
		if (player == Player.ONE)
			return mPlayer1DelayLeft;
		else
			return mPlayer2DelayLeft;
	}

	public void adjustDelayLeftForPlayer(Player player, long adjustment) {
		if (player == Player.ONE)
			mPlayer1DelayLeft += adjustment;
		else
			mPlayer2DelayLeft += adjustment;
	}

	public void decrementDelayLeftForPlayer(Player player) {
		// Convenience method to apply one tick to a player
		if (player == Player.ONE)
			mPlayer1DelayLeft -= BlitznChessClock.CLOCK_RESOLUTION;
		else
			mPlayer2DelayLeft -= BlitznChessClock.CLOCK_RESOLUTION;
	}

	// Chess Clock dispatchers

	public long getClockResolution() {
		return BlitznChessClock.CLOCK_RESOLUTION;
	}

	public long getTimeLeftForPlayer(Player player) {
		return mChessClock.getTimeLeftForPlayer(player);
	}

	public void adjustTimeLeftForPlayer(Player player, long adjustment) {
		mChessClock.adjustTimeLeftForPlayer(player, adjustment);
	}

	// Strategy dispatchers

	public void tickForPlayer(Player player) {
		mDelayStrategy.tickForPlayer(this, player);
	}

	public void startDelayForPlayer(Player player) {
		mDelayStrategy.startDelayForPlayer(this, player);
	}

	public void stopDelayForPlayer(Player player) {
		mDelayStrategy.stopDelayForPlayer(this, player);
	}

	public void resetDelayForPlayer(Player player) {
		mDelayStrategy.resetDelayForPlayer(this, player);
	}

}
