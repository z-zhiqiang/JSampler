package edu.uci.jsampler.assist;

public class GlobalCountdown {
	
	private static int countdown = 1;
	

	public static int getCountdown() {
		return countdown;
	}

	public static void setCountdown(int countdown) {
		GlobalCountdown.countdown = countdown;
	}
	
	public static int getNextCountdown(int opportunities){
		countdown = opportunities;
		return countdown;
	}

	
}
