package com.barleysoft.blitzn.delay;

import com.barleysoft.blitzn.BlitznChessClock.Player;

public interface Delay {

	void setDelay(long delay);

	long getDelay();

	void tickForPlayer(Player player);

	void resetDelayForPlayer(Player player);

	void startDelayForPlayer(Player player);

	void stopDelayForPlayer(Player player);

	long getDelayLeftForPlayer(Player player);

}
