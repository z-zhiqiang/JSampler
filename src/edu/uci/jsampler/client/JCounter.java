package edu.uci.jsampler.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uci.jsampler.transformer.PCounter;
import soot.PackManager;
import soot.Transform;
import soot.options.Options;

public class JCounter {
	public static final String counts_output = "./output.counts";
	
	// instrumentation flag
	private static boolean branches_flag;

	private static boolean returns_flag;

	private static boolean scalarpairs_flag;

	private static boolean methodentries_flag;

	// methods instrumented
	private static Set<String> methods_instrument;

	
	public static void main(String[] args) {
		//parse arguments
		List<String> soot_parameters = new ArrayList<String>();
		parseParameters(args, soot_parameters);

		PCounter counter = new PCounter(branches_flag, returns_flag, scalarpairs_flag, methodentries_flag, methods_instrument);
		PackManager.v().getPack("jtp").add(new Transform("jtp.counter", counter));

		Options.v().setPhaseOption("jb", "use-original-names:true");
		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_keep_line_number(true);
		Options.v().set_prepend_classpath(true);

		soot.Main.main(soot_parameters.toArray(new String[soot_parameters.size()]));

		// export static instrumentation information into files
		exportCounts();
		
	}

	private static void exportCounts() {
		File file = new File(counts_output);
		PrintWriter out = null;
		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			out = new PrintWriter(new BufferedWriter(new FileWriter(file)));

			out.printf("Branches=%d\nReturns=%d\nScalarPairs=%d\nMethodEntries=%d", PCounter.counts_branch, PCounter.counts_return, PCounter.counts_scalarPair, PCounter.counts_methodEntry);
			
			out.close();
		} catch(FileNotFoundException e){ 
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(out != null){
				out.close();
			}
		}
	}

	/**
	 * parse parameters into sampler-options and soot-parameters
	 * 
	 * @param args
	 * @param soot_parameters
	 */
	private static void parseParameters(String[] args, List<String> soot_parameters) {
		if (args.length == 0) {
			System.err.println("Usage: java JCounter sampler-options [soot-options] classname");
			System.exit(0);
		}
		
		methods_instrument = new HashSet<String>();
		
		// TODO Auto-generated method stub
		for (int i = 0; i < args.length; i++) {
			String option = args[i].trim();
			if (option.startsWith("-sampler")) {
				if (option.equals("-sampler-scheme=branches")) {
					branches_flag = true;
				} 
				else if (option.equals("-sampler-scheme=returns")) {
					returns_flag = true;
				} 
				else if (option.equals("-sampler-scheme=scalar-pairs")) {
					scalarpairs_flag = true;
				} 
				else if (option.equals("-sampler-scheme=method-entries")) {
					methodentries_flag = true;
				} 
				else if (option.equals("-sampler")) {
				} 
				else if (option.startsWith("-sampler-include-method=")) {
					methods_instrument.add(option.split("=")[1].trim());
				} 
				else if (option.startsWith("-sampler-out-sites=")) {
				}
				else{
					System.err.println("wrong option!");
				}
			} else {
				soot_parameters.add(option);
			}
		}
	}
	

}
