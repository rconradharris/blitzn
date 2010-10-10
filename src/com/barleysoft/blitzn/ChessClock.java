package com.barleysoft.blitzn;

import com.barleysoft.blitzn.BlitznChessClock.Player;

public interface ChessClock {

	long getTimeLeftForPlayer(Player player);

	void adjustTimeLeftForPlayer(Player player, long adjustment);

}
