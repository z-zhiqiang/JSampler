package edu.uci.jsampler.client;

import java.util.ArrayList;
import java.util.List;

import edu.uci.jsampler.instrument.PInstrumentor;
import soot.PackManager;
import soot.Transform;
import soot.options.Options;

public class JSampler {

	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Usage: java JSampler sampler-options [soot-options] classname");
			System.exit(0);
		}

		List<String> sampler_options = new ArrayList<String>();
		List<String> soot_parameters = new ArrayList<String>();
		parseParameters(args, sampler_options, soot_parameters);

		PackManager.v().getPack("jtp").add(new Transform("jtp.instrumenter", new PInstrumentor(sampler_options)));

		Options.v().setPhaseOption("jb", "use-original-names:true");
		Options.v().set_keep_line_number(true);

		soot.Main.main(soot_parameters.toArray(new String[soot_parameters.size()]));
	}

	/**
	 * separate parameters into sampler-options and soot-parameters
	 * 
	 * @param args
	 * @param sampler_options
	 * @param soot_parameters
	 */
	private static void parseParameters(String[] args, List<String> sampler_options, List<String> soot_parameters) {
		// TODO Auto-generated method stub
		for (int i = 0; i < args.length; i++) {
			String para = args[i];
			if (para.startsWith("-sampler")) {
				sampler_options.add(para);
			} else {
				soot_parameters.add(para);
			}
		}
	}
}
