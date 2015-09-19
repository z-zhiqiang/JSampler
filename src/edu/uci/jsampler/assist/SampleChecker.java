package edu.uci.jsampler.assist;

import java.util.Random;

public class SampleChecker {
	private static final int opportunities = 1000;
	
	public static synchronized boolean toSample(){
		Random random = new Random();
		int randomInt = random.nextInt(opportunities);
		if(randomInt == 0){
			return true;
		}
		return false;
	}

}
