package com.barleysoft.blitzn.delay;

import com.barleysoft.blitzn.Blitzn.Player;

public interface DelayStrategy {

	boolean shouldClockTickForPlayer(DelayContext context, Player player);

	void tickForPlayer(DelayContext context, Player player);

	void startDelayForPlayer(DelayContext context, Player player);

	void stopDelayForPlayer(DelayContext context, Player player);

	void resetDelayForPlayer(DelayContext context, Player player);

}
