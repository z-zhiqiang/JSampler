package edu.uci.jsampler.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.uci.jsampler.instrument.PInstrumentor;
import edu.uci.jsampler.site.AbstractSite;
import edu.uci.jsampler.util.Translator;
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

	private static int opportunities;

	// methods instrumented
	private static Set<String> methods_instrument;

	private static String output_file_sites;

	private static String output_file_reports;

	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Usage: java JSampler sampler-options [soot-options] classname");
			System.exit(0);
		}

		// List<String> sampler_options = new ArrayList<String>();
		List<String> soot_parameters = new ArrayList<String>();
		// parseParameters(args, sampler_options, soot_parameters);
		parseParameters(args, soot_parameters);

		PackManager.v().getPack("jtp")
				.add(new Transform("jtp.instrumenter",
						new PInstrumentor(branches_flag, returns_flag, scalarpairs_flag, methodentries_flag,
								sample_flag, opportunities, methods_instrument, output_file_sites,
								output_file_reports)));

		Options.v().setPhaseOption("jb", "use-original-names:true");
		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_keep_line_number(true);
		Options.v().set_prepend_classpath(true);

		soot.Main.main(soot_parameters.toArray(new String[soot_parameters.size()]));

		// export static instrumentation information into files
//		System.out.println(Translator.getInstance().toString());
		printStaticSitesInfo(output_file_sites);
	}

	/**
	 * @param args
	 * @param soot_parameters
	 */
	private static void parseParameters(String[] args, List<String> soot_parameters) {
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
				else if (option.equals("-sampler-no")) {
					sample_flag = false;
				} 
				else if (option.startsWith("-sampler-opportunities=")) {
					 opportunities = Integer.parseInt(option.split("=")[1].trim());
				} 
				else if (option.startsWith("-sampler-include-method=")) {
					methods_instrument.add(option.split("=")[1].trim());
				} 
				else if (option.startsWith("-sampler-out-sites=")) {
					output_file_sites = option.split("=")[1].trim();
				}
				else if (option.startsWith("-sampler-out-reports=")) {
					output_file_reports = option.split("=")[1].trim();
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
	private static void printStaticSitesInfo(String sites_file_name) {
		File file = new File(sites_file_name);
		String unit_signature = PInstrumentor.unit_signature;
		List sitesInfo = null;
		PrintWriter out = null;
		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			// write the passing inputs
			out = new PrintWriter(new BufferedWriter(new FileWriter(file)));

			/* branches */
			// tag headers
			out.printf("<sites unit=\"%s\" scheme=\"%s\">\n", unit_signature, "branches");
			// content
			sitesInfo = PInstrumentor.branch_staticInfo;
			for (int i = 0; i < sitesInfo.size(); i++) {
				AbstractSite site = (AbstractSite) sitesInfo.get(i);
				out.println(site.printToString());
			}
			// tag close
			out.println("</sites>");

			/* returns */
			// tag headers
			out.printf("<sites unit=\"%s\" scheme=\"%s\">\n", unit_signature, "returns");
			// content
			sitesInfo = PInstrumentor.return_staticInfo;
			for (int i = 0; i < sitesInfo.size(); i++) {
				AbstractSite site = (AbstractSite) sitesInfo.get(i);
				out.println(site.printToString());
			}
			// tag close
			out.println("</sites>");

			/* scalar-pairs */
			// tag headers
			out.printf("<sites unit=\"%s\" scheme=\"%s\">\n", unit_signature, "scalar-pairs");
			// content
			sitesInfo = PInstrumentor.scalarPair_staticInfo;
			for (int i = 0; i < sitesInfo.size(); i++) {
				AbstractSite site = (AbstractSite) sitesInfo.get(i);
				out.println(site.printToString());
			}
			// tag close
			out.println("</sites>");
			
			/* method-entries */
			// tag headers
			out.printf("<sites unit=\"%s\" scheme=\"%s\">\n", unit_signature, "method-entries");
			// content
			sitesInfo = PInstrumentor.methodEntry_staticInfo;
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

	/**
	 * print out the static instrumentation information in sitesInfo to a file
	 * 
	 * @param sitesInfo
	 * @param unit_signature
	 * @param file
	 */
	private static void printStaticInstrumentationInfoForEachScheme(List sitesInfo, String unit_signature, File file) {
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

}
