package edu.uci.jsampler.transformer;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.jsampler.site.BranchSite;
import edu.uci.jsampler.site.MethodEntrySite;
import edu.uci.jsampler.site.ReturnSite;
import edu.uci.jsampler.site.ScalarPairSite;
import edu.uci.jsampler.util.Translator;
import soot.Body;
import soot.BodyTransformer;
import soot.BooleanType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.IntegerType;
import soot.Local;
import soot.LongType;
import soot.PrimType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.annotation.logic.LoopFinder;
import soot.tagkit.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.InitAnalysis;
import soot.util.Chain;

public class PInstrumentor extends BodyTransformer {

	// internal fields
	static SootClass checkerReporterClass, globalCountdownClass, sampleCheckerClass;
	static SootMethod getCountdown, setCountdown;
	static SootMethod toSample;
	static SootMethod checkReturns_int, checkReturns_long, checkReturns_float, checkReturns_double, 
			checkBranches,
			checkScalarPairs_int, checkScalarPairs_long, checkScalarPairs_float, checkScalarPairs_double, 
			checkMethodEntries;
	static SootMethod report;

	static {
		//checkerReporterClass
		checkerReporterClass = Scene.v().loadClassAndSupport("edu.uci.jsampler.assist.PredicateCheckerReporter");

		checkReturns_int = checkerReporterClass.getMethod("void checkReturns(int,int)");
		checkReturns_long = checkerReporterClass.getMethod("void checkReturns(long,int)");
		checkReturns_float = checkerReporterClass.getMethod("void checkReturns(float,int)");
		checkReturns_double = checkerReporterClass.getMethod("void checkReturns(double,int)");

		checkBranches = checkerReporterClass.getMethod("void checkBranches(int,int,java.lang.String,int)");
		
		checkScalarPairs_int = checkerReporterClass.getMethod("void checkScalarPairs(int,int,int)");
		checkScalarPairs_long = checkerReporterClass.getMethod("void checkScalarPairs(long,long,int)");
		checkScalarPairs_float = checkerReporterClass.getMethod("void checkScalarPairs(float,float,int)");
		checkScalarPairs_double = checkerReporterClass.getMethod("void checkScalarPairs(double,double,int)");
		
		checkMethodEntries = checkerReporterClass.getMethod("void checkMethodEntries(int)");

		report = checkerReporterClass.getMethod("void exportReports(java.lang.String,java.lang.String)");
		
		
		//globalCountdownClass
		globalCountdownClass = Scene.v().loadClassAndSupport("edu.uci.jsampler.assist.GlobalCountdown");
		
		getCountdown = globalCountdownClass.getMethod("int getCountdown()");
		setCountdown = globalCountdownClass.getMethod("void setCountdown(int)");
		
		
		//sampleCheckerClass
		sampleCheckerClass = Scene.v().loadClassAndSupport("edu.uci.jsampler.assist.SampleChecker");
		
		toSample = sampleCheckerClass.getMethod("boolean toSample()");
	}

	
	// instrumentation flag
	private final boolean branches_flag;

	private final boolean returns_flag;

	private final boolean scalarpairs_flag;

	private final boolean methodentries_flag;

	private final boolean sample_flag;// sampling flag

	private final int opportunities;// sampling opportunities

	private final Set<String> methods_instrument;// methods instrumented

	private final String output_file_sites;// output file name storing static sites info

	private final String output_file_reports;// output file name for dynamic reports

	
	// static instrumentation site information
	public static final List<ReturnSite> return_staticInfo = new ArrayList<ReturnSite>();

	public static final List<BranchSite> branch_staticInfo = new ArrayList<BranchSite>();

	public static final List<ScalarPairSite> scalarPair_staticInfo = new ArrayList<ScalarPairSite>();

	public static final List<MethodEntrySite> methodEntry_staticInfo = new ArrayList<MethodEntrySite>();

	public static final String unit_signature = generateUnitSignature();// compilation unit signature: a 128-bit as 32 hexadecimal digits

	/**
	 * constructor mainly for sampler options parsing
	 * 
	 * @param branches_flag
	 * @param returns_flag
	 * @param scalarpairs_flag
	 * @param methodentries_flag
	 * @param sample_flag
	 * @param opportunities
	 * @param methods_instrument
	 * @param output_file_sites
	 * @param output_file_reports
	 */
	public PInstrumentor(boolean branches_flag, boolean returns_flag, boolean scalarpairs_flag,
			boolean methodentries_flag, boolean sample_flag, int opportunities, Set<String> methods_instrument,
			String output_file_sites, String output_file_reports) {
		// TODO Auto-generated constructor stub
		this.branches_flag = branches_flag;
		this.returns_flag = returns_flag;
		this.scalarpairs_flag = scalarpairs_flag;
		this.methodentries_flag = methodentries_flag;
		this.sample_flag = sample_flag;
		this.opportunities = opportunities;
		this.methods_instrument = methods_instrument;
		this.output_file_sites = output_file_sites;
		this.output_file_reports = output_file_reports;
	}

	/** generate a unique compilation unit signature: a 128-bit as 32 hexadecimal digits
	 * @return
	 */
	private static String generateUnitSignature() {
		byte[] unitID = new byte[16];
		SecureRandom random = new SecureRandom();
		random.nextBytes(unitID);

		StringBuilder builder = new StringBuilder();
		for (byte b : unitID) {
			builder.append(String.format("%02X", b));
		}
		return builder.toString();
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.BodyTransformer#internalTransform(soot.Body, java.lang.String,
	 * java.util.Map)
	 */
	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		// get body's units
		Body body_original = (Body) body.clone();
		Chain<Unit> units = body.getUnits();
		Iterator<Unit> stmtIt = units.snapshotIterator();
		Iterator<Unit> faststmtIt = units.snapshotIterator();
		
		
		/*---------------------------------------------- sampling -------------------------------------------------*/
		
		//at functin entry
		//add nop statement at the end of the original code
		NopStmt nopStmt = Jimple.v().newNopStmt();
		units.insertAfter(nopStmt, units.getLast());
		
		//add sample checking code at the very beginning
		Local toSampleLocal = Jimple.v().newLocal("_toSampleLocal_functionentry", BooleanType.v());
		body.getLocals().add(toSampleLocal);
		
		InvokeExpr toSampleExpr = Jimple.v().newStaticInvokeExpr(toSample.makeRef());
		AssignStmt toSampleStmt = Jimple.v().newAssignStmt(toSampleLocal, toSampleExpr);
		units.insertBefore(toSampleStmt, getFirstNonIdentityStmt(units));
		
		ConditionExpr condition = Jimple.v().newEqExpr(toSampleLocal, IntConstant.v(0));
		IfStmt ifStmt = Jimple.v().newIfStmt(condition, nopStmt);
		units.insertAfter(ifStmt, toSampleStmt);	
		
		
		//fast path
		//add cloned code at the end of the original code
		Map<Unit, Unit> map = new HashMap<Unit, Unit>();
		while(faststmtIt.hasNext()){
			Stmt stmt = (Stmt) faststmtIt.next();
			System.out.println(stmt.toString() + "\t" + stmt.hashCode());
			Stmt newStmt = (Stmt) stmt.clone();
			System.out.println(newStmt.toString() + "\t" + newStmt.hashCode());
			if(!(stmt instanceof IdentityStmt)){
				map.put(stmt, newStmt);
				units.insertAfter(newStmt, units.getLast());
			}
		}
		
		for(Unit oldStmt: map.keySet()){
			if(oldStmt instanceof IfStmt){
				IfStmt newIfStmt = (IfStmt) map.get(oldStmt);
				newIfStmt.setTarget(map.get(newIfStmt.getTarget()));
			}
			else if(oldStmt instanceof GotoStmt){
				GotoStmt newGotoStmt = (GotoStmt) map.get(oldStmt);
				newGotoStmt.setTarget(map.get(newGotoStmt.getTarget()));
			}
		}		
				
		System.out.println();
		
		//at loop back edge
		//region weights
//		int weight_function = 0;
//		Map<Unit, Integer> weight_loops = new HashMap<Unit, Integer>();
		LoopNestTree loopNestTree = new LoopNestTree(body);
//		loopNestTree.r
		int i = 0;
		for(Loop loop: loopNestTree){
			System.out.println("a loop with head: " + loop.getHead().toString() + "\t" + loop.getHead().hashCode() + "  \nback: " + loop.getBackJumpStmt().toString());
//			weight_loops.put(loop.getHead(), 0);
			if(map.keySet().contains(loop.getHead())){
				System.out.println(loop.getHead().hashCode());
				Stmt backJumpStmt = loop.getBackJumpStmt();
				
				Local toSampleLocal_loop = Jimple.v().newLocal("_toSampleLocal_loop_" + i++, BooleanType.v());
				body.getLocals().add(toSampleLocal_loop);
				
				InvokeExpr toSampleExpr_loop = Jimple.v().newStaticInvokeExpr(toSample.makeRef());
				AssignStmt toSampleStmt_loop = Jimple.v().newAssignStmt(toSampleLocal_loop, toSampleExpr_loop);
				units.insertAfter(toSampleStmt_loop, backJumpStmt);
				
				ConditionExpr condition_loop = Jimple.v().newEqExpr(toSampleLocal_loop, IntConstant.v(0));
				IfStmt ifStmt_loop = Jimple.v().newIfStmt(condition_loop, map.get(loop.getHead()));
				units.insertAfter(ifStmt_loop, toSampleStmt_loop);	
				
				Stmt fastBackJumpStmt = (Stmt) map.get(backJumpStmt);
				GotoStmt fastGotoStmt = Jimple.v().newGotoStmt(toSampleStmt_loop);
				units.insertAfter(fastGotoStmt, fastBackJumpStmt);
			}
		}
		
		
					
		
		
		
		
//		//export current global countdown into a local countdown
//		Local countdown = Jimple.v().newLocal("countdown", IntType.v());
//		body.getLocals().add(countdown);
//
//		InvokeExpr getCountdownExpr = Jimple.v().newStaticInvokeExpr(getCountdown.makeRef());
//		AssignStmt getCountdownStmt = Jimple.v().newAssignStmt(countdown, getCountdownExpr);
//		units.insertBefore(getCountdownStmt, getFirstNonIdentityStmt(units));
//		
//		
//		//add nop statement at the end of the original code
//		NopStmt nopStmt = Jimple.v().newNopStmt();
//		units.insertAfter(nopStmt, units.getLast());
//		
//		//add sample checking code at the very beginning
//		ConditionExpr condition = Jimple.v().newGtExpr(countdown, IntConstant.v(weight_function));
//		IfStmt ifStmt = Jimple.v().newIfStmt(condition, nopStmt);
//		units.insertAfter(ifStmt, getCountdownStmt);		
//		
//		//fast path
//		//decrease countdown
//		BinopExpr binoExpr = Jimple.v().newSubExpr(countdown, IntConstant.v(weight_function));
//		AssignStmt decreaseStmt = Jimple.v().newAssignStmt(countdown, binoExpr);
//		units.insertAfter(decreaseStmt, nopStmt);		
		
			
		

		/*---------------------------------------------- predicates instrumentation -------------------------------------------------*/
		
		//instrument the specified methods
		if(this.methods_instrument.isEmpty() || this.methods_instrument.contains(body.getMethod().getSignature())){
			// source file name
			String file_name = body.getMethod().getDeclaringClass().getName();
			int file_name_translated = Translator.getInstance().getInteger(file_name);
			
			// body's method
			String method_name = body.getMethod().getSignature();
			int method_name_translated = Translator.getInstance().getInteger(method_name);
			
			//initialized variables
			InitAnalysis analysis = new InitAnalysis(new BriefUnitGraph(body));

			int cfg_number = 0;
			boolean instrumentEntry_flag = true;
			Value def = null;
			
			while (stmtIt.hasNext()) {
				// cast back to a statement
				Stmt stmt = (Stmt) stmtIt.next();
// 			    System.out.println(stmt.toString());
				
				// get source line number
				int line_number = getSourceLineNumber(stmt);
				
				/* add checking code */
				// for method-entries
				if (this.methodentries_flag && instrumentEntry_flag && !(stmt instanceof IdentityStmt)) {
					instrumentMethodEntries(file_name_translated, method_name_translated, line_number, cfg_number, body, units, stmt);
					instrumentEntry_flag = false;
				}
				
				// for branches
				if (this.branches_flag && stmt instanceof IfStmt) {
					instrumentBranches(file_name_translated, method_name_translated, line_number, cfg_number, body, units, stmt);
				}
				// for returns and scalar-pairs
				if (stmt instanceof AssignStmt && (def = ((AssignStmt) stmt).getLeftOp()).getType() instanceof PrimType) {
					// for returns
					if (this.returns_flag && ((Stmt) stmt).containsInvokeExpr()) {
						instrumentReturns(file_name_translated, method_name_translated, line_number, cfg_number, body, units, stmt, def);
					}
					// for scalar-pairs
					else if(this.scalarpairs_flag){
						instrumentScalarPairs(file_name_translated, method_name_translated, line_number, cfg_number, body, units, stmt, def, analysis);
					}
					
				}
				
				/* add reporting code */
				// insert reporting code before exit
				if (stmt instanceof InvokeStmt) {
					InvokeExpr iexpr = stmt.getInvokeExpr();
					if (iexpr instanceof StaticInvokeExpr
							&& iexpr.getMethod().getSignature().equals("<java.lang.System: void exit(int)>")) {
						instrumentReport(units, stmt);
					}
				}
				// insert reporting code before return of main
				if (body.getMethod().getSubSignature().equals("void main(java.lang.String[])") && (stmt instanceof ReturnStmt || stmt instanceof ReturnVoidStmt)) {
					instrumentReport(units, stmt);
				}
				
				
				// trace cfg_number
				cfg_number++;
			}
		}
		else{
			while (stmtIt.hasNext()) {
				// cast back to a statement
				Stmt stmt = (Stmt) stmtIt.next();
				
				/* add reporting code */
				// insert reporting code before exit
				if (stmt instanceof InvokeStmt) {
					InvokeExpr iexpr = stmt.getInvokeExpr();
					if (iexpr instanceof StaticInvokeExpr
							&& iexpr.getMethod().getSignature().equals("<java.lang.System: void exit(int)>")) {
						instrumentReport(units, stmt);
					}
				}
				// insert reporting code before return of main
				if (body.getMethod().getSubSignature().equals("void main(java.lang.String[])") && (stmt instanceof ReturnStmt || stmt instanceof ReturnVoidStmt)) {
					instrumentReport(units, stmt);
				}
			}
		}
		
		
	}


	private Stmt getFirstNonIdentityStmt(Chain<Unit> units) {
		// TODO Auto-generated method stub
		Stmt firstStmt = null;
		Iterator<Unit> stmtIt = units.snapshotIterator();
		while(stmtIt.hasNext()){
			Stmt stmt = (Stmt) stmtIt.next();
			if(!(stmt instanceof IdentityStmt)){
				firstStmt = stmt;
				break;
			}
		}
		return firstStmt;
	}

	private void sampleMethodEntry(Body body, Chain<Unit> units, Stmt stmt, Stmt toStmt) {
		// TODO Auto-generated method stub
		Local tmp = Jimple.v().newLocal("countdown", IntType.v());
		body.getLocals().add(tmp);
		
		ConditionExpr condition = Jimple.v().newGtExpr(tmp, IntConstant.v(2));
		Stmt ifStmt = Jimple.v().newIfStmt(condition, toStmt);
		units.insertBefore(ifStmt, stmt);
	}

	private void instrumentReport(Chain<Unit> units, Stmt stmt) {
		InvokeExpr reportExpr = Jimple.v().newStaticInvokeExpr(report.makeRef(), StringConstant.v(output_file_reports), StringConstant.v(unit_signature));
		Stmt reportStmt = Jimple.v().newInvokeStmt(reportExpr);
		units.insertBefore(reportStmt, stmt);
	}

	/**
	 * @param file_name
	 * @param method_name
	 * @param line_number
	 * @param cfg_number
	 * @param body
	 * @param units
	 * @param stmt
	 * @param def
	 * @param analysis 
	 */
	private void instrumentScalarPairs(int file_name, int method_name, int line_number, int cfg_number, Body body,
			Chain<Unit> units, Stmt stmt, Value def, InitAnalysis analysis) {
		/* static site information for scalar-pair */
		// left info
		String left = def.toString();
		String scope_type_assign = null, container_type = null;
		if (def instanceof soot.Local) {
			scope_type_assign = "local";
			container_type = "direct";
		} else if (def instanceof soot.jimple.InstanceFieldRef) {
			scope_type_assign = "local";
			container_type = "field";
		} else if (def instanceof soot.jimple.StaticFieldRef) {
			scope_type_assign = "global";
			container_type = "field";
		} else if (def instanceof soot.jimple.ArrayRef) {
			scope_type_assign = "mem";
			container_type = "index";
		}
		// right and compared variable info
		String right = ((AssignStmt) stmt).getRightOp().toString();

		if (!(def instanceof soot.Local)) {
			// insert checking code
			Local tmp = Jimple.v().newLocal("tmp" + cfg_number, def.getType());
			body.getLocals().add(tmp);
			Stmt inserted_assign = Jimple.v().newAssignStmt(tmp, def);
			units.insertAfter(inserted_assign, stmt);

			def = tmp;
			stmt = inserted_assign;
		}

		Iterator it = ((FlowSet) analysis.getFlowAfter(stmt)).iterator();
		while(it.hasNext()){
			Local local = (Local) it.next();
			if (local.getType() == def.getType() && local != def) {
				// create static instrumentation site for scalar-pairs
				ScalarPairSite site = new ScalarPairSite(file_name, line_number, method_name, cfg_number, left, scope_type_assign, container_type, right, local.toString());
				scalarPair_staticInfo.add(site);

				// insert checking code
				InvokeExpr checkScalarPair = null;
				if(def.getType() instanceof IntegerType){
					//boolean, byte, char, short, int
					checkScalarPair = Jimple.v().newStaticInvokeExpr(checkScalarPairs_int.makeRef(), def, local, IntConstant.v(scalarPair_staticInfo.size() - 1));
				}
				else if(def.getType() instanceof LongType){
					checkScalarPair = Jimple.v().newStaticInvokeExpr(checkScalarPairs_long.makeRef(), def, local, IntConstant.v(scalarPair_staticInfo.size() - 1));
				}
				else if(def.getType() instanceof FloatType){
					checkScalarPair = Jimple.v().newStaticInvokeExpr(checkScalarPairs_float.makeRef(), def, local, IntConstant.v(scalarPair_staticInfo.size() - 1));
				}
				else if(def.getType() instanceof DoubleType){
					checkScalarPair = Jimple.v().newStaticInvokeExpr(checkScalarPairs_double.makeRef(), def, local, IntConstant.v(scalarPair_staticInfo.size() - 1));
				}
				else{
					System.err.println("wrong prim type!");
				}
				
				Stmt checkScalarPairStmt = Jimple.v().newInvokeStmt(checkScalarPair);
				units.insertAfter(checkScalarPairStmt, stmt);
			}
		}
	}


	/**
	 * @param file_name
	 * @param method_name
	 * @param line_number
	 * @param cfg_number
	 * @param body
	 * @param units
	 * @param stmt
	 */
	private void instrumentBranches(int file_name, int method_name, int line_number, int cfg_number, Body body,
			Chain<Unit> units, Stmt stmt) {
		Value conditional = ((IfStmt) stmt).getCondition();
		assert(conditional instanceof ConditionExpr);

		// create static instrumentation site for branch
		BranchSite site = new BranchSite(file_name, line_number, method_name, cfg_number, conditional.toString());
		branch_staticInfo.add(site);

		// insert checking code
		Value leftop = ((ConditionExpr) conditional).getOp1();
		Value rightop = ((ConditionExpr) conditional).getOp2();
		String symbol = ((ConditionExpr) conditional).getSymbol();
		InvokeExpr checkBranch = Jimple.v().newStaticInvokeExpr(checkBranches.makeRef(), leftop, rightop, StringConstant.v(symbol.trim()), IntConstant.v(branch_staticInfo.size() - 1));
		Stmt checkBranchStmt = Jimple.v().newInvokeStmt(checkBranch);
		units.insertBefore(checkBranchStmt, stmt);
	}

	private int getSourceLineNumber(Stmt stmt) {
		// TODO Auto-generated method stub
		return ((LineNumberTag) stmt.getTag("LineNumberTag")).getLineNumber();
	}

	/**
	 * @param file_name
	 * @param method_name
	 * @param line_number
	 * @param cfg_number
	 * @param body
	 * @param units
	 * @param stmt
	 */
	private void instrumentMethodEntries(int file_name, int method_name, int line_number, int cfg_number, Body body,
			Chain<Unit> units, Unit stmt) {
		// static instrumentation site for method entry
		MethodEntrySite site = new MethodEntrySite(file_name, line_number, method_name, cfg_number);
		methodEntry_staticInfo.add(site);

		// insert checking code
		InvokeExpr checkMethodEntry = Jimple.v().newStaticInvokeExpr(checkMethodEntries.makeRef(), IntConstant.v(methodEntry_staticInfo.size() - 1));
		Stmt checkMethodEntryStmt = Jimple.v().newInvokeStmt(checkMethodEntry);
		units.insertBefore(checkMethodEntryStmt, stmt);
	}

	/**
	 * @param file_name
	 * @param method_name
	 * @param line_number
	 * @param cfg_number
	 * @param body
	 * @param units
	 * @param stmt
	 * @param def
	 */
	private void instrumentReturns(int file_name, int method_name, int line_number, int cfg_number, Body body,
			Chain<Unit> units, Unit stmt, Value def) {
		// callee
		String callee = "static";
		if (!(((Stmt) stmt).getInvokeExpr() instanceof StaticInvokeExpr)) {
			callee = ((ValueBox) ((Stmt) stmt).getInvokeExpr().getUseBoxes().get(0)).getValue().toString();
		}

		// create static instrumentation site information for return
		ReturnSite site = new ReturnSite(file_name, line_number, method_name, cfg_number, callee);
		return_staticInfo.add(site);

		// insert checking code
		InvokeExpr checkReturn = null;
		if(def.getType() instanceof IntegerType){
			//boolean, byte, char, short, int
			checkReturn = Jimple.v().newStaticInvokeExpr(checkReturns_int.makeRef(), def, IntConstant.v(return_staticInfo.size() - 1));
		}
		else if(def.getType() instanceof LongType){
			checkReturn = Jimple.v().newStaticInvokeExpr(checkReturns_long.makeRef(), def, IntConstant.v(return_staticInfo.size() - 1));
		}
		else if(def.getType() instanceof FloatType){
			checkReturn = Jimple.v().newStaticInvokeExpr(checkReturns_float.makeRef(), def, IntConstant.v(return_staticInfo.size() - 1));
		}
		else if(def.getType() instanceof DoubleType){
			checkReturn = Jimple.v().newStaticInvokeExpr(checkReturns_double.makeRef(), def, IntConstant.v(return_staticInfo.size() - 1));
		}
		else{
			System.err.println("wrong prim type!");
		}
		Stmt checkReturnStmt = Jimple.v().newInvokeStmt(checkReturn);
		units.insertAfter(checkReturnStmt, stmt);
	}

	public boolean isBranches_flag() {
		return branches_flag;
	}

	public boolean isReturns_flag() {
		return returns_flag;
	}

	public boolean isScalarpairs_flag() {
		return scalarpairs_flag;
	}

	public boolean isMethodentries_flag() {
		return methodentries_flag;
	}

	public boolean isSample_flag() {
		return sample_flag;
	}

	public int getOpportunities() {
		return opportunities;
	}

	public Set<String> getMethods_instrument() {
		return methods_instrument;
	}

	public String getOutput_file_sites() {
		return output_file_sites;
	}

	public String getOutput_file_reports() {
		return output_file_reports;
	}

	
}
