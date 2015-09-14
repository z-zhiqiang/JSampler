package edu.uci.jsampler.instrument;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

public class StaticCheckerReporter {

	// dynamic feedback reports
	private static Map<Integer, byte[]> return_reports = new LinkedHashMap<Integer, byte[]>();

	private static Map<Integer, byte[]> branch_reports = new LinkedHashMap<Integer, byte[]>();

	private static Map<Integer, byte[]> scalarPair_reports = new LinkedHashMap<Integer, byte[]>();

	private static Map<Integer, byte[]> methodEntry_reports = new LinkedHashMap<Integer, byte[]>();
	
	

	public static synchronized void exportReports(String output_file) {
		File file = new File(output_file);
		String unit_signature = PInstrumentor.unit_signature;

		PrintWriter out = null;

		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(file)));

			printDynamicReportsInfoForEachScheme(branch_reports, "branches", unit_signature, out);

			printDynamicReportsInfoForEachScheme(return_reports, "returns", unit_signature, out);

			printDynamicReportsInfoForEachScheme(scalarPair_reports, "scalar-pairs", unit_signature, out);

			printDynamicReportsInfoForEachScheme(methodEntry_reports, "method-entries", unit_signature, out);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			out.close();
		}
	}

	private static void printDynamicReportsInfoForEachScheme(Map<Integer, byte[]> reports, String scheme,
			String unit_signature, PrintWriter out) {
		out.printf("<samples unit=\"%s\" scheme=\"%s\">\n", unit_signature, scheme);
		for (int index : reports.keySet()) {
			byte[] bytes = reports.get(index);
			out.println(toString(bytes));
		}
		out.println("</samples>");
	}

	private static String toString(byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < bytes.length; i++){
			builder.append(bytes[i]).append("\t");
		}
		return builder.toString();
	}

	/**
	 * checking code for returns
	 * 
	 * @param returned
	 *            returned scalar value
	 * @param index
	 */
	public static synchronized void checkReturns(Object returned, int index) {
		double returned_value = (double) returned;
		if (!return_reports.containsKey(index)) {
			return_reports.put(index, new byte[3]);
		}

		// counts = {negative, zero, positive}
		byte[] counts = return_reports.get(index);
		if (returned_value < 0) {
			increaseCount(counts, 0);
		} else if (returned_value == 0) {
			increaseCount(counts, 1);
		} else {
			increaseCount(counts, 2);
		}
	}

	/**
	 * checking code for method entries
	 * 
	 * @param index
	 */
	public static synchronized void checkMethodEntries(int index) {
		if (!methodEntry_reports.containsKey(index)) {
			methodEntry_reports.put(index, new byte[1]);
		}
		byte[] counts = methodEntry_reports.get(index);
		increaseCount(counts, 0);
	}

	/**
	 * checking code for branches
	 * 
	 * @param conditional
	 * @param index
	 */
	public static synchronized void checkBranches(boolean conditional, int index) {
		if (!branch_reports.containsKey(index)) {
			branch_reports.put(index, new byte[2]);
		}
		// counts = {false, true}
		byte[] counts = branch_reports.get(index);
		if (!conditional) {
			increaseCount(counts, 0);
		} else {
			increaseCount(counts, 1);
		}
	}

	public static synchronized void checkScalarPairs(Object assigned, Object local, int index) {
		assert(assigned.getClass() == local.getClass());
		double assigned_value = (double) assigned;
		double local_value = (double) local;
		if (!scalarPair_reports.containsKey(index)) {
			scalarPair_reports.put(index, new byte[3]);
		}
		// counts = {<, ==, >}
		byte[] counts = scalarPair_reports.get(index);
		if (assigned_value < local_value) {
			increaseCount(counts, 0);
		} else if (assigned_value == local_value) {
			increaseCount(counts, 1);
		} else {
			increaseCount(counts, 2);
		}

	}

	private static void increaseCount(byte[] counts, int i) {
		// TODO Auto-generated method stub
		if (counts[i] != Byte.MAX_VALUE) {
			counts[i]++;
		}
	}

}
