package edu.uci.jsampler.instrument;

public class GlobalCountDown {
	
	public static final int opportunities = 1000;
	
	private static final int[] countdowns = getCountdowns();
	
	private static int countdown;
	

	public static int getCountdown() {
		return countdown;
	}

	private static int[] getCountdowns() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void setCountdown(int countdown) {
		GlobalCountDown.countdown = countdown;
	}
	
	public static int getNextCountdown(){
		return GlobalCountDown.opportunities;
	}

	
}
