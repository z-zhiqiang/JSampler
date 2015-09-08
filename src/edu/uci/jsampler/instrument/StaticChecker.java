package edu.uci.jsampler.instrument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.jsampler.report.AbstractSiteValue;

public class StaticChecker {
	
	//dynamic feedback reports
	private static Map<Integer, byte[]> return_reports = new HashMap<Integer, byte[]>();
	
	private static Map<Integer, byte[]> branch_reports = new HashMap<Integer, byte[]>();
	
	private static Map<Integer, byte[]> scalarPair_reports = new HashMap<Integer, byte[]>();
	
	private static Map<Integer, byte[]> methodEntry_reports = new HashMap<Integer, byte[]>();
	

	
	/**checking code for returns
	 * @param returned
	 * 			returned scalar value
	 * @param index
	 */
	public static synchronized void checkReturns(Object returned, int index){
		double returned_value = (double) returned;
		if(!return_reports.containsKey(index)){
			return_reports.put(index, new byte[3]);
		}
		//counts = {negative, zero, positive}
		byte[] counts = return_reports.get(index);
		if(returned_value < 0){
			increaseCount(counts, 0);
		}
		else if(returned_value == 0){
			increaseCount(counts, 1);
		}
		else{
			increaseCount(counts, 2);
		}
	}

	
	/**checking code for method entries
	 * @param index
	 */
	public static synchronized void checkMethodEntries(int index){
		if(!methodEntry_reports.containsKey(index)){
			methodEntry_reports.put(index, new byte[1]);
		}
		byte[] counts = methodEntry_reports.get(index);
		increaseCount(counts, 0);
	}

	
	/**checking code for branches
	 * @param conditional
	 * @param index
	 */
	public static synchronized void checkBranches(boolean conditional, int index){
		if(!branch_reports.containsKey(index)){
			branch_reports.put(index, new byte[2]);
		}
		//counts = {false, true}
		byte[] counts = branch_reports.get(index);
		if(!conditional){
			increaseCount(counts, 0);
		}
		else{
			increaseCount(counts, 1);
		}
	}
	
	
	public static synchronized void checkScalarPairs(Object assigned, Object local, int index){
		assert(assigned.getClass() == local.getClass());
		double assigned_value = (double) assigned;
		double local_value = (double) local;
		if(!scalarPair_reports.containsKey(index)){
			scalarPair_reports.put(index, new byte[3]);
		}
		//counts = {<, ==, >}
		byte[] counts = scalarPair_reports.get(index);
		if(assigned_value < local_value){
			increaseCount(counts, 0);
		}
		else if(assigned_value == local_value){
			increaseCount(counts, 1);
		}
		else{
			increaseCount(counts, 2);
		}
		
	}
	
	private static void increaseCount(byte[] counts, int i) {
		// TODO Auto-generated method stub
		if(counts[i] != Byte.MAX_VALUE){
			counts[i]++;
		}
	}

}
