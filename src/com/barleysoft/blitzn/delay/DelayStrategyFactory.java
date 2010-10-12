package com.barleysoft.blitzn.delay;

public class DelayStrategyFactory {

	public static DelayStrategy getDelayStrategyById(int strategyId) {
		switch (strategyId) {
		case 2:
			return new BronsteinDelayStrategy();
		case 1:
			return new FischerDelayStrategy();
		case 0:
		default:
			return new NoneDelayStrategy();

		}
	}

	public static int getIdForDelayStrategy(DelayStrategy delayStrategy) {
		if (delayStrategy instanceof BronsteinDelayStrategy)
			return 2;
		else if (delayStrategy instanceof FischerDelayStrategy)
			return 1;
		else
			return 0;
	}
}
