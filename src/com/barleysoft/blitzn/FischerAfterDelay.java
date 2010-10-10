package com.barleysoft.blitzn;

import com.barleysoft.blitzn.BlitznChessClock.Player;

public class FischerAfterDelay extends BaseDelay {

	FischerAfterDelay(BlitznChessClock chessClock) {
		super(chessClock);
	}

	@Override
	public void tickForPlayer(Player player) {
		if (getDelayLeftForPlayer(player) > 0) {
			adjustDelayLeftForPlayer(player, -getClockResolution());
			if (getDelayLeftForPlayer(player) < 0)
				setDelayLeftForPlayer(player, 0);
		}
	}

	@Override
	public void startDelayForPlayer(Player player) {
		resetDelayForPlayer(player);
	}

	@Override
	public void stopDelayForPlayer(Player player) {
		// If the time hasn't run out, add remaining delay back to
		// player's clock
		if (getTimeLeftForPlayer(player) > 0)
			adjustTimeForPlayer(player, getDelayLeftForPlayer(player));
	}

}
