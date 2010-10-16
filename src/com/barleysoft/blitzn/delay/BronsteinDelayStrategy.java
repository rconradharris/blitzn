package com.barleysoft.blitzn.delay;

import com.barleysoft.blitzn.Blitzn.Player;

public class BronsteinDelayStrategy implements DelayStrategy {

	public boolean shouldClockTickForPlayer(DelayContext context, Player player) {
		return true;
	}

	public void tickForPlayer(DelayContext context, Player player) {
		if (context.getDelayLeftForPlayer(player) > 0) {
			context.decrementDelayLeftForPlayer(player);

			if (context.getDelayLeftForPlayer(player) < 0)
				context.setDelayLeftForPlayer(player, 0);
		}
	}

	public void startDelayForPlayer(DelayContext context, Player player) {
		// TODO Auto-generated method stub

	}

	public void stopDelayForPlayer(DelayContext context, Player player) {
		// If the time hasn't run out, add delay back to
		// player's clock
		if (context.getTimeLeftForPlayer(player) > 0) {
			long delay = context.getDelay();
			long delayLeft = context.getDelayLeftForPlayer(player);
			long delayUsed = delay - delayLeft;
			context.adjustTimeLeftForPlayer(player, delayUsed);
			context.setDelayLeftForPlayer(player, delay);
		}
	}

	public void resetDelayForPlayer(DelayContext context, Player player) {
		context.setDelayLeftForPlayer(player, context.getDelay());
	}

}
