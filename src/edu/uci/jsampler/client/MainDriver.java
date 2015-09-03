package edu.uci.jsampler.client;

import edu.uci.jsampler.instrument.PInstrumentor;
import soot.PackManager;
import soot.Transform;

public class MainDriver {

	public static void main(String[] args) {
		if(args.length == 0){
			System.err.println("Usage: java MainDriver [options] classname");
			System.exit(0);
		}
		
		PackManager.v().getPack("jtp").add(new Transform("jtp.instrumenter", new PInstrumentor()));
		
		soot.Main.main(args);
	}
}
