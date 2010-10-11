package com.barleysoft.blitzn.delay;

public class DelayStrategyFactory {

	public static DelayStrategy getDelayStrategyById(int strategyId) {
		switch (strategyId) {
		case 1:
			return new FischerAfterDelayStrategy();
		case 0:
		default:
			return new NoneDelayStrategy();

		}
	}

	public static int getIdForDelayStrategy(DelayStrategy delayStrategy) {
		if (delayStrategy instanceof FischerAfterDelayStrategy)
			return 1;
		else
			return 0;
	}
}
