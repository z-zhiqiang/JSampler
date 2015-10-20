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

import edu.uci.jsampler.site.AbstractSite;
import edu.uci.jsampler.transformer.PCounter;
import edu.uci.jsampler.transformer.PInstrumentor;
import soot.PackManager;
import soot.Transform;
import soot.options.Options;

public class JSampler {
	public static final String counts_output = "./output.counts";
	
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

	
	public static void main(String[] args) {
		//parse arguments
		List<String> soot_parameters = new ArrayList<String>();
		String sootClassPath = parseParameters(args, soot_parameters);
		System.out.println(sootClassPath);
		System.out.println(soot_parameters);

		
		/*---------------------------------------------------------------------------------------------*/
		
		Options.v().set_soot_classpath(sootClassPath);
		Options.v().setPhaseOption("jb", "use-original-names:true");
		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_keep_line_number(true);
		Options.v().set_prepend_classpath(true);

		PCounter counter = new PCounter(branches_flag, returns_flag, scalarpairs_flag, methodentries_flag, methods_instrument);
		PackManager.v().getPack("jtp").add(new Transform("jtp.counter", counter));

		soot.Main.main(soot_parameters.toArray(new String[soot_parameters.size()]));

		// export static instrumentation information into files
		exportCounts();
		
		//clear all static state of Soot
		soot.G.reset();
		
		//clear all the jimple files generated
		removeJimpleFromDirectory(new File(sootClassPath));
		
		/*---------------------------------------------------------------------------------------------*/

		Options.v().set_soot_classpath(sootClassPath);
		Options.v().setPhaseOption("jb", "use-original-names:true");
//		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_keep_line_number(true);
		Options.v().set_prepend_classpath(true);

		PInstrumentor instrumentor = new PInstrumentor(branches_flag, returns_flag, scalarpairs_flag, methodentries_flag,
				sample_flag, methods_instrument, PCounter.counts_branch, PCounter.counts_return, PCounter.counts_scalarPair, PCounter.counts_methodEntry);
		PackManager.v().getPack("jtp").add(new Transform("jtp.instrumenter", instrumentor));

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
	private static String parseParameters(String[] args, List<String> soot_parameters) {
		if (args.length == 0) {
			System.err.println("Usage: java JSampler sampler-options [soot-options] classname");
			System.exit(0);
		}
		
		//default values
		output_file_sites = "./output.sites";
		
		methods_instrument = new HashSet<String>();
		
		String classPath = null;
		int index = -2;
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
				if(option.equals("-cp")|| option.equals("-soot-class-path")|| option.equals("-soot-classpath")){
					classPath = args[i + 1];
					index = i;
				}
				else if (i != (index + 1)){
					soot_parameters.add(option);
				}
				
			}
		}
		
		return classPath;
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
			printStaticInstInfoForEachScheme(out, PInstrumentor.branch_staticInfo, unit_signature, "branches");
			assert(PInstrumentor.branch_staticInfo.size() == PCounter.counts_branch);

			/* returns */
			printStaticInstInfoForEachScheme(out, PInstrumentor.return_staticInfo, unit_signature, "returns");
			assert(PInstrumentor.return_staticInfo.size() == PCounter.counts_return);

			/* scalar-pairs */
			printStaticInstInfoForEachScheme(out, PInstrumentor.scalarPair_staticInfo, unit_signature, "scalar-pairs");
			assert(PInstrumentor.scalarPair_staticInfo.size() == PCounter.counts_scalarPair);

			/* method-entries */
			printStaticInstInfoForEachScheme(out, PInstrumentor.methodEntry_staticInfo, unit_signature, "method-entries");
			assert(PInstrumentor.methodEntry_staticInfo.size() == PCounter.counts_methodEntry);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}
	}

	/**
	 * print out static instrumentation information for each instrumentation scheme
	 * 
	 * @param out
	 * @param sitesInfo
	 * @param unit_signature
	 * @param scheme
	 */
	private static void printStaticInstInfoForEachScheme(PrintWriter out, List sitesInfo, String unit_signature, String scheme){
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
	
	
//	/**
//	 * print out the static instrumentation information in sitesInfo to a file
//	 * 
//	 * @param sitesInfo
//	 * @param unit_signature
//	 * @param file
//	 */
//	private static void writeOutStaticInstrumentationInfoForEachScheme(List sitesInfo, String unit_signature, File file) {
//		PrintWriter out = null;
//		try {
//			if (!file.getParentFile().exists()) {
//				file.getParentFile().mkdirs();
//			}
//			// write the passing inputs
//			out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
//
//			// tag headers
//			out.printf("<sites unit=\"%s\" scheme=\"%s\">\n", unit_signature,
//					((AbstractSite) sitesInfo.get(0)).getSchemeName());
//			// content
//			for (int i = 0; i < sitesInfo.size(); i++) {
//				AbstractSite site = (AbstractSite) sitesInfo.get(i);
//				out.println(site.printToString());
//			}
//			// tag close
//			out.println("</sites>");
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			out.close();
//		}
//	}
	
	/**
	 * export counts of sites
	 */
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
	 * remove all jimple files under directory
	 * 
	 * @param directory
	 */
	public static void removeJimpleFromDirectory(File directory) {
		String[] list = directory.list();

		// Some JVMs return null for File.list() when the directory is empty.
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				File entry = new File(directory, list[i]);
				
				if (entry.isDirectory()) {
					removeJimpleFromDirectory(entry);
				} else if(entry.getName().endsWith(".jimple")){
					entry.delete();
				}
			}
		}

	}
	
}
