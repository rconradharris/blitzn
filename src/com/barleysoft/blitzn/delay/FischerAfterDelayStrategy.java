package com.barleysoft.blitzn.delay;

import com.barleysoft.blitzn.BlitznChessClock.Player;

public class FischerAfterDelayStrategy implements DelayStrategy {

	public void tickForPlayer(DelayContext context, Player player) {
		if (context.getDelayLeftForPlayer(player) > 0) {
			context.decrementDelayLeftForPlayer(player);

			if (context.getDelayLeftForPlayer(player) < 0)
				context.setDelayLeftForPlayer(player, 0);
		}
	}

	public void startDelayForPlayer(DelayContext context, Player player) {
		resetDelayForPlayer(context, player);
	}

	public void stopDelayForPlayer(DelayContext context, Player player) {
		// If the time hasn't run out, add remaining delay back to
		// player's clock
		if (context.getTimeLeftForPlayer(player) > 0) {
			long extraDelay = context.getDelayLeftForPlayer(player);
			context.adjustTimeLeftForPlayer(player, extraDelay);
		}
	}

	public void resetDelayForPlayer(DelayContext context, Player player) {
		context.setDelayLeftForPlayer(player, context.getDelay());
	}

}
