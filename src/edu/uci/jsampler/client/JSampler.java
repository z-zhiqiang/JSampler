package edu.uci.jsampler.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uci.jsampler.site.AbstractSite;
import edu.uci.jsampler.transformer.PInstrumentor;
import soot.PackManager;
import soot.Transform;
import soot.options.Options;

public class JSampler {
	// instrumentation flag
	private static boolean branches_flag;

	private static boolean returns_flag;

	private static boolean scalarpairs_flag;

	private static boolean methodentries_flag;

	// sampling flag
	private static boolean sample_flag;

	// methods instrumented
	private static Set<String> methods_instrument;

	private static String output_file_sites;

	
	// counts
	public static int counts_branch;
	
	public static int counts_return;
	
	public static int counts_scalarPair;
	
	public static int counts_methodEntry;
	
	static{
		importCounts();
	}
	
	public static void main(String[] args) {
		//parse arguments
		List<String> soot_parameters = new ArrayList<String>();
		parseParameters(args, soot_parameters);

		PInstrumentor instrumentor = new PInstrumentor(branches_flag, returns_flag, scalarpairs_flag, methodentries_flag,
				sample_flag, methods_instrument, counts_branch, counts_return, counts_scalarPair, counts_methodEntry);
		PackManager.v().getPack("jtp").add(new Transform("jtp.instrumenter", instrumentor));

		Options.v().setPhaseOption("jb", "use-original-names:true");
//		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_keep_line_number(true);
		Options.v().set_prepend_classpath(true);

		soot.Main.main(soot_parameters.toArray(new String[soot_parameters.size()]));

		// export static instrumentation information into files
		writeOutStaticSitesInfo(output_file_sites);
	}

	/**
	 * parse parameters into sampler-options and soot-parameters
	 * 
	 * @param args
	 * @param soot_parameters
	 */
	private static void parseParameters(String[] args, List<String> soot_parameters) {
		if (args.length == 0) {
			System.err.println("Usage: java JSampler sampler-options [soot-options] classname");
			System.exit(0);
		}
		
		//default values
		output_file_sites = "./output.sites";
		
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
					sample_flag = true;
				} 
				else if (option.startsWith("-sampler-include-method=")) {
					methods_instrument.add(option.split("=")[1].trim());
				} 
				else if (option.startsWith("-sampler-out-sites=")) {
					output_file_sites = option.split("=")[1].trim();
				}
				else{
					System.err.println("wrong option!");
				}
			} else {
				soot_parameters.add(option);
			}
		}
	}

	/**
	 * @param sites_file_name
	 */
	private static void writeOutStaticSitesInfo(String sites_file_name) {
		File file = new File(sites_file_name);
		String unit_signature = PInstrumentor.unit_signature;
		PrintWriter out = null;
		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			// write the passing inputs
			out = new PrintWriter(new BufferedWriter(new FileWriter(file)));

			/* branches */
			printStaticInstrumentationInfoForEachScheme(out, PInstrumentor.branch_staticInfo, unit_signature, "branches");
			assert(PInstrumentor.branch_staticInfo.size() == counts_branch);

			/* returns */
			printStaticInstrumentationInfoForEachScheme(out, PInstrumentor.return_staticInfo, unit_signature, "returns");
			assert(PInstrumentor.return_staticInfo.size() == counts_return);

			/* scalar-pairs */
			printStaticInstrumentationInfoForEachScheme(out, PInstrumentor.scalarPair_staticInfo, unit_signature, "scalar-pairs");
			assert(PInstrumentor.scalarPair_staticInfo.size() == counts_scalarPair);

			/* method-entries */
			printStaticInstrumentationInfoForEachScheme(out, PInstrumentor.methodEntry_staticInfo, unit_signature, "method-entries");
			assert(PInstrumentor.methodEntry_staticInfo.size() == counts_methodEntry);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}
	}

	private static void printStaticInstrumentationInfoForEachScheme(PrintWriter out, List sitesInfo, String unit_signature, String scheme){
		if(sitesInfo.size() > 0){
			// tag headers
			out.printf("<sites unit=\"%s\" scheme=\"%s\">\n", unit_signature, scheme);
			// content
			for (int i = 0; i < sitesInfo.size(); i++) {
				AbstractSite site = (AbstractSite) sitesInfo.get(i);
				out.println(site.printToString());
			}
			// tag close
			out.println("</sites>");
		}
	}
	
	
	/**
	 * print out the static instrumentation information in sitesInfo to a file
	 * 
	 * @param sitesInfo
	 * @param unit_signature
	 * @param file
	 */
	private static void writeOutStaticInstrumentationInfoForEachScheme(List sitesInfo, String unit_signature, File file) {
		PrintWriter out = null;
		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			// write the passing inputs
			out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));

			// tag headers
			out.printf("<sites unit=\"%s\" scheme=\"%s\">\n", unit_signature,
					((AbstractSite) sitesInfo.get(0)).getSchemeName());
			// content
			for (int i = 0; i < sitesInfo.size(); i++) {
				AbstractSite site = (AbstractSite) sitesInfo.get(i);
				out.println(site.printToString());
			}
			// tag close
			out.println("</sites>");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}
	}
	
	
	private static void importCounts(){
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(new File(JCounter.counts_output)));
			String line;
			while((line = reader.readLine()) != null){
				String[] entry = line.split("=");
				assert(entry.length == 2);
				String scheme = entry[0].trim();
				int count = Integer.parseInt(entry[1].trim());
				if(scheme.equals("Branches")){
					counts_branch = count;
				}
				else if(scheme.equals("Returns")){
					counts_return = count;
				}
				else if(scheme.equals("ScalarPairs")){
					counts_scalarPair = count;
				}
				else if(scheme.equals("MethodEntries")){
					counts_methodEntry = count;
				}
				else{
					System.err.println("Wrong entry for counts!");
				}
			}
			
			reader.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	

}
