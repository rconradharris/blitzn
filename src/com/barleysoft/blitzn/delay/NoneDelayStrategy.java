package com.barleysoft.blitzn.delay;

import com.barleysoft.blitzn.BlitznChessClock.Player;

public class NoneDelayStrategy implements DelayStrategy {
	// All ChessClocks relate to a Delay object even if it doesn't have
	// a delay set. In that null case, the clock is associated with the
	// NoneDelay
	// class which acts as a NOOP

	public void tickForPlayer(DelayContext context, Player player) {
		// TODO Auto-generated method stub

	}

	public void startDelayForPlayer(DelayContext context, Player player) {
		// TODO Auto-generated method stub

	}

	public void stopDelayForPlayer(DelayContext context, Player player) {
		// TODO Auto-generated method stub

	}

	public void resetDelayForPlayer(DelayContext context, Player player) {
		// TODO Auto-generated method stub

	}

}
