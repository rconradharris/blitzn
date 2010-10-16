package com.barleysoft.blitzn.delay;

import com.barleysoft.blitzn.Blitzn.Player;

public class FischerDelayStrategy implements DelayStrategy {

	public void tickForPlayer(DelayContext context, Player player) {
		// NOOP
	}

	public void startDelayForPlayer(DelayContext context, Player player) {
		context.adjustTimeLeftForPlayer(player, context.getDelay());
	}

	public void stopDelayForPlayer(DelayContext context, Player player) {
		// NOOP
	}

	public void resetDelayForPlayer(DelayContext context, Player player) {
		// NOOP
	}

	public boolean shouldClockTickForPlayer(DelayContext context, Player player) {
		return true;
	}

}
