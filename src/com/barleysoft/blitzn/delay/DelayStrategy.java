package com.barleysoft.blitzn.delay;

import com.barleysoft.blitzn.BlitznChessClock.Player;

public interface DelayStrategy {

	void tickForPlayer(DelayContext context, Player player);

	void startDelayForPlayer(DelayContext context, Player player);

	void stopDelayForPlayer(DelayContext context, Player player);

	void resetDelayForPlayer(DelayContext context, Player player);

}
