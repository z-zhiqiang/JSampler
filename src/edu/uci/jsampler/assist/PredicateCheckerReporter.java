package edu.uci.jsampler.assist;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class PredicateCheckerReporter {

	// dynamic feedback reports
	private static Map<Integer, byte[]> return_reports = new TreeMap<Integer, byte[]>();

	private static Map<Integer, byte[]> branch_reports = new TreeMap<Integer, byte[]>();

	private static Map<Integer, byte[]> scalarPair_reports = new TreeMap<Integer, byte[]>();

	private static Map<Integer, byte[]> methodEntry_reports = new TreeMap<Integer, byte[]>();
	
	
	//filename of reports
	private final static String output_file_reports = getReportFilename();
	

	public static synchronized void exportReports(String unit_signature) {
		File file = new File(output_file_reports);
		PrintWriter out = null;

		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			
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
		for(Entry<Integer, byte[]> entry: reports.entrySet()){
			out.println(toString(entry));
		}
		out.println("</samples>");
	}


	private static String toString(Entry<Integer, byte[]> entry) {
		StringBuilder builder = new StringBuilder();
		builder.append(entry.getKey()).append("\t");
		byte[] bytes = entry.getValue();
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
	public static synchronized void checkReturns(int returned_value, int index) {
		checkReturns((long) returned_value, index);
	}
	
	public static synchronized void checkReturns(long returned_value, int index) {
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
	
	public static synchronized void checkReturns(float returned_value, int index) {
		checkReturns((double) returned_value, index);
	}
	
	/**
	 * checking code for returns
	 * 
	 * @param returned
	 *            returned scalar value
	 * @param index
	 */
	public static synchronized void checkReturns(double returned_value, int index) {
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
	 * @param left
	 * @param right
	 * @param symbol
	 * @param index
	 */
	public static synchronized void checkBranches(int left, int right, String symbol, int index){
		if (!branch_reports.containsKey(index)) {
			branch_reports.put(index, new byte[2]);
		}
		// counts = {false, true}
		byte[] counts = branch_reports.get(index);
		
		if(symbol.equals("==")){
			if(left == right){
				increaseCount(counts, 1);
			}
			else{
				increaseCount(counts, 0);
			}
		}
		else if(symbol.equals("!=")){
			if(left != right){
				increaseCount(counts, 1);
			}
			else{
				increaseCount(counts, 0);
			}
		}
		else if(symbol.equals("<=")){
			if(left <= right){
				increaseCount(counts, 1);
			}
			else{
				increaseCount(counts, 0);
			}
		}
		else if(symbol.equals(">=")){
			if(left >= right){
				increaseCount(counts, 1);
			}
			else{
				increaseCount(counts, 0);
			}
		}
		else if(symbol.equals("<")){
			if(left < right){
				increaseCount(counts, 1);
			}
			else{
				increaseCount(counts, 0);
			}
		}
		else if(symbol.equals(">")){
			if(left > right){
				increaseCount(counts, 1);
			}
			else{
				increaseCount(counts, 0);
			}
		}
		else{
			System.err.println("wrong operator!");
		}
	}
	
	
	/** checking code for scalar-pairs
	 * @param assigned
	 * @param local
	 * @param index
	 */
	public static synchronized void checkScalarPairs(int assigned, int local, int index){
		checkScalarPairs((long) assigned, (long) local, index);
	}
	
	public static synchronized void checkScalarPairs(long assigned_value, long local_value, int index){
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
	
	public static synchronized void checkScalarPairs(float assigned, float local, int index){
		checkScalarPairs((double) assigned, (double) local, index);
	}

	public static synchronized void checkScalarPairs(double assigned_value, double local_value, int index) {
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
	
	
	/**
	 * get the filename of reports via environment variable
	 * 
	 * @return
	 */
	private static String getReportFilename(){
		String output_report = System.getenv("SAMPLER_FILE");
		if(output_report == null){
			output_report = "./output.reports";
		}
		return output_report;
	}

}