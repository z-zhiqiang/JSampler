package edu.uci.jsampler.assist;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

public class PredicateCheckerReporter {

	// dynamic feedback reports
	private static Map<Integer, byte[]> return_reports = new TreeMap<Integer, byte[]>();

	private static Map<Integer, byte[]> branch_reports = new TreeMap<Integer, byte[]>();

	private static Map<Integer, byte[]> scalarPair_reports = new TreeMap<Integer, byte[]>();

	private static Map<Integer, byte[]> methodEntry_reports = new TreeMap<Integer, byte[]>();
	
	
	//filename of reports
	private final static String output_file_reports = getReportFilename();
	

	/**
	 * export dynamic reports (i.e., traces)
	 * 
	 * @param unit_signature
	 * @param counts_branch
	 * @param counts_return
	 * @param counts_scalarPair
	 * @param counts_methodEntry
	 */
	public static synchronized void exportReports(String unit_signature, int counts_branch, int counts_return, int counts_scalarPair, int counts_methodEntry) {
		File file = new File(output_file_reports);
		PrintWriter out = null;

		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			
			out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			
			out.println("<report id=\"samples\">");

			printReportsForEachScheme(branch_reports, "branches", unit_signature, out, counts_branch);

			printReportsForEachScheme(return_reports, "returns", unit_signature, out, counts_return);

			printReportsForEachScheme(scalarPair_reports, "scalar-pairs", unit_signature, out, counts_scalarPair);

			printReportsForEachScheme(methodEntry_reports, "method-entries", unit_signature, out, counts_methodEntry);
			
			out.println("</report>");
			
			out.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(out != null){
				out.close();
			}
		}
	}

	/**
	 * print out dynamic reports for each instrumentation scheme
	 * 
	 * @param reports
	 * @param scheme
	 * @param unit_signature
	 * @param out
	 * @param count
	 */
	private static void printReportsForEachScheme(Map<Integer, byte[]> reports, String scheme,
			String unit_signature, PrintWriter out, int count) {
		// TODO Auto-generated method stub
		if(count > 0){
			out.printf("<samples unit=\"%s\" scheme=\"%s\">\n", unit_signature, scheme);
			int current = 0;
			int next;
			for(int index: reports.keySet()){
				next = index;
				while(current++ < next){
					out.println(emptyArrays(scheme));
				}
				out.println(toString(reports.get(next)));
			}
			
			while(current < count){
				out.println(emptyArrays(scheme));
				current++;
			}
			
			out.println("</samples>");
		}
	}

	private static String toString(byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < bytes.length; i++){
			builder.append(bytes[i]).append("\t");
		}
		return builder.toString();
	}

	/** all-zero values
	 * @param scheme
	 * @return
	 */
	private static String emptyArrays(String scheme) {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder();
		int length = -1;
		if(scheme.equals("branches")){
			length = 2;
		}
		else if(scheme.equals("returns")){
			length = 3;
		}
		else if(scheme.equals("scalar-pairs")){
			length = 3;
		}
		else if(scheme.equals("method-entries")){
			length = 1;
		}
		else{
			System.err.println("Wrong scheme!");
		}
		
		for(int i = 0; i < length; i++){
			builder.append(0).append("\t");
		}
		
		return builder.toString();
	}

//	private static void printDynamicReportsInfoForEachScheme(Map<Integer, byte[]> reports, String scheme,
//			String unit_signature, PrintWriter out, int count) {
//		out.printf("<samples unit=\"%s\" scheme=\"%s\">\n", unit_signature, scheme);
//		for(Entry<Integer, byte[]> entry: reports.entrySet()){
//			out.println(toString(entry));
//		}
//		out.println("</samples>");
//	}
//
//	private static String toString(Entry<Integer, byte[]> entry) {
//		StringBuilder builder = new StringBuilder();
//		builder.append(entry.getKey()).append("\t");
//		byte[] bytes = entry.getValue();
//		for(int i = 0; i < bytes.length; i++){
//			builder.append(bytes[i]).append("\t");
//		}
//		return builder.toString();
//	}

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

	
	public static synchronized void checkBranches(Object left, Object right, String symbol, int index){
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
		else{
			System.err.println("wrong operator!");
		}
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
//		System.err.println("file: " + output_report);
		return output_report;
	}

}
