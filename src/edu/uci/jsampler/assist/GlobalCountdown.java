package edu.uci.jsampler.assist;

public class GlobalCountdown {
	
	private final static int sparsity = getEnvCountdown();

	private static int countdown = sparsity;
	

	public static int getCountdown() {
		return countdown;
	}

	/**
	 * get the sampling sparsity through environment variable
	 * 
	 * @return
	 */
	private static int getEnvCountdown() {
		int countdown = 1000;
		String env = System.getenv("SAMPLER_SPARSITY");
		if(env != null){
			countdown = Integer.parseInt(env.trim());
		}
		
		return countdown;
	}

	public static void setCountdown(int countdown) {
		GlobalCountdown.countdown = countdown;
	}
	
	public static int getNextCountdown(){
		return sparsity;
	}

	
}
