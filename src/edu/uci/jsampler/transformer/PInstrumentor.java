package edu.uci.jsampler.transformer;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import soot.Trap;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ConditionExpr;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NopStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.tagkit.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.InitAnalysis;
import soot.util.Chain;

public class PInstrumentor extends BodyTransformer {

	public static final int Limit_Site = 15;

	public static final int Threshold_Locals = 35;

	public static final int Limit_ScalarPairs = 600;
	
	// internal fields
	static SootClass checkerReporterClass, globalCountdownClass;
	static SootMethod getCountdown, setCountdown, getNextCountdown;
	static SootMethod toSample;
	static SootMethod checkReturns_int, checkReturns_long, checkReturns_float, checkReturns_double, 
			checkBranches_int, checkBranches_object,
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

		checkBranches_int = checkerReporterClass.getMethod("void checkBranches(int,int,java.lang.String,int)");
		checkBranches_object = checkerReporterClass.getMethod("void checkBranches(java.lang.Object,java.lang.Object,java.lang.String,int)");
		
		checkScalarPairs_int = checkerReporterClass.getMethod("void checkScalarPairs(int,int,int)");
		checkScalarPairs_long = checkerReporterClass.getMethod("void checkScalarPairs(long,long,int)");
		checkScalarPairs_float = checkerReporterClass.getMethod("void checkScalarPairs(float,float,int)");
		checkScalarPairs_double = checkerReporterClass.getMethod("void checkScalarPairs(double,double,int)");
		
		checkMethodEntries = checkerReporterClass.getMethod("void checkMethodEntries(int)");

		report = checkerReporterClass.getMethod("void exportReports(java.lang.String,int,int,int,int)");
		
		
		//globalCountdownClass
		globalCountdownClass = Scene.v().loadClassAndSupport("edu.uci.jsampler.assist.GlobalCountdown");
		
		getCountdown = globalCountdownClass.getMethod("int getCountdown()");
		setCountdown = globalCountdownClass.getMethod("void setCountdown(int)");
		getNextCountdown = globalCountdownClass.getMethod("int getNextCountdown()");
		
	}

	
	// instrumentation flag
	private final boolean branches_flag;

	private final boolean returns_flag;

	private final boolean scalarpairs_flag;

	private final boolean methodentries_flag;

	private final boolean sample_flag;// sampling flag

	private final Set<String> methods_instrument;// methods instrumented

	
	// static instrumentation site information
	public static final List<ReturnSite> return_staticInfo = new ArrayList<ReturnSite>();

	public static final List<BranchSite> branch_staticInfo = new ArrayList<BranchSite>();

	public static final List<ScalarPairSite> scalarPair_staticInfo = new ArrayList<ScalarPairSite>();

	public static final List<MethodEntrySite> methodEntry_staticInfo = new ArrayList<MethodEntrySite>();

	public static final String unit_signature = generateUnitSignature();// compilation unit signature: a 128-bit as 32 hexadecimal digits
	

	// counts
	private final int counts_branch;
	
	private final int counts_return;
	
	private final int counts_scalarPair;
	
	private final int counts_methodEntry;	
	
	
	public static int counts_scalarPair_inst;
		
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
	 * @param counts_methodEntry 
	 * @param counts_scalarPair 
	 * @param counts_return 
	 * @param counts_branch 
	 * @param output_file_sites
	 * @param output_file_reports
	 */
	public PInstrumentor(boolean branches_flag, boolean returns_flag, boolean scalarpairs_flag,
			boolean methodentries_flag, boolean sample_flag, Set<String> methods_instrument, 
			int counts_branch, int counts_return, int counts_scalarPair, int counts_methodEntry) {
		// TODO Auto-generated constructor stub
		this.branches_flag = branches_flag;
		this.returns_flag = returns_flag;
		this.scalarpairs_flag = scalarpairs_flag;
		this.methodentries_flag = methodentries_flag;
		this.sample_flag = sample_flag;
		this.methods_instrument = methods_instrument;
		
		this.counts_branch = counts_branch;
		this.counts_return = counts_return;
		this.counts_scalarPair = counts_scalarPair;
		this.counts_methodEntry = counts_methodEntry;
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
	 * @see soot.BodyTransformer#internalTransform(soot.Body, java.lang.String, java.util.Map)
	 */
	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		//body's units
		Chain<Unit> units = body.getUnits();
		Iterator<Unit> original_stmtIt = units.snapshotIterator();
		Iterator<Unit> stmtIt = units.snapshotIterator();
		
		//body's traps
		Chain<Trap> traps = body.getTraps();
		Iterator<Trap> trapIt = traps.snapshotIterator();

		//initialized variables
		InitAnalysis analysis = new InitAnalysis(new BriefUnitGraph(body));

		Local countdown = null;
		
		boolean under_analysis = this.methods_instrument.isEmpty() || this.methods_instrument.contains(PCounter.transform(body.getMethod().getSignature()));
//		under_analysis = under_analysis && !body.getMethod().getDeclaringClass().toString().equals("org.apache.tools.bzip2.CBZip2OutputStream");
//		under_analysis = under_analysis && !body.getMethod().getDeclaringClass().toString().equals("org.apache.derbyTesting.functionTests.tests.lang.concateTests");
//		under_analysis = under_analysis && !body.getMethod().getDeclaringClass().toString().equals("org.apache.derby.impl.sql.compile.SQLParserTokenManager");
		
		
		/*---------------------------------------------- sampling -------------------------------------------------*/
		
		if(this.sample_flag && under_analysis){
			//region weights: weight_function and weight_loops
			Map<Unit, Integer> weight_loops = new HashMap<Unit, Integer>();
			int weight_function = computeWeights(body, units, weight_loops, analysis);
			
			//if no instrumentation site, skip it
			if(weight_function <= 0){
				addReportingBeforeExit(body, units);
				return;
			}
			
			//import current global countdown into a local countdown
			countdown = importGlobalCountdownIntoLocal(body, units);
			
			//at function entry
			insertSampleCheckingCodeAtFunctionentry(body, units, weight_function, countdown);	
			
			//fast path
			Map<Unit, Unit> unitsMap = addClonedCodeAsFastpath(units, stmtIt, traps, trapIt);		
			
			//at loop back edge
			insertSampleCheckingCodeAtLoopback(body, units, weight_loops, countdown, unitsMap);
			
			
			//export local countdown back to global before function exit and remove nop statement
			exportLocalCountdownToGlobal(units, countdown);
		}
		

		/*---------------------------------------------- predicates instrumentation -------------------------------------------------*/
		
		//instrument the specified methods
		if(under_analysis){
			// source file name
			String file_name = body.getMethod().getDeclaringClass().getName();
			int file_name_translated = Translator.getInstance().getInteger(file_name);
			
			boolean flag = true;
			int num = counts_scalarPair_inst;
			
			// body's method
			String method_name = body.getMethod().getSignature();
			int method_name_translated = Translator.getInstance().getInteger(method_name);

			int cfg_number = 0;
			boolean instrumentEntry_flag = true;
			Value def = null;
			
			while (original_stmtIt.hasNext()) {
				// cast back to a statement
				Stmt stmt = (Stmt) original_stmtIt.next();
				
				// get source line number
				int line_number = getSourceLineNumber(stmt);
				
				/* add checking code */
				// for method-entries
				if (this.methodentries_flag && instrumentEntry_flag && !isNonCaughtExceptionIdentityStmt(stmt)) {
					instrumentMethodEntries(file_name_translated, method_name_translated, line_number, cfg_number, body, units, stmt, countdown);
					instrumentEntry_flag = false;
				}
				
				// for branches
				if (this.branches_flag && stmt instanceof IfStmt) {
					instrumentBranches(file_name_translated, method_name_translated, line_number, cfg_number, body, units, stmt, countdown);
				}
				// for returns and scalar-pairs
				if (stmt instanceof AssignStmt && (def = ((AssignStmt) stmt).getLeftOp()).getType() instanceof PrimType) {
					// for returns
					if (this.returns_flag && ((Stmt) stmt).containsInvokeExpr()) {
						instrumentReturns(file_name_translated, method_name_translated, line_number, cfg_number, body, units, stmt, def, countdown);
					}
					// for scalar-pairs
					else if(this.scalarpairs_flag && !((Stmt) stmt).containsInvokeExpr() && flag){
						if(counts_scalarPair_inst - num > Limit_ScalarPairs){
							flag = false;
						}
						instrumentScalarPairs(file_name_translated, method_name_translated, line_number, cfg_number, body, units, stmt, def, analysis, countdown);
					}
					
				}
				// trace cfg_number
				cfg_number++;
			}
		}
		
		
		// add reporting code before program exit 
		addReportingBeforeExit(body, units);
		
		
	}

	
	/** insert reporting code before program exit 
	 * @param body
	 * @param units
	 */
	private void addReportingBeforeExit(Body body, Chain<Unit> units) {
		Iterator<Unit> sIt = units.snapshotIterator();
		while(sIt.hasNext()){
			Stmt stmt = (Stmt) sIt.next();
			
			// insert reporting code before system exit
			if (stmt instanceof InvokeStmt) {
				InvokeExpr iexpr = stmt.getInvokeExpr();
				if (iexpr instanceof StaticInvokeExpr && iexpr.getMethod().getSignature().equals("<java.lang.System: void exit(int)>")) {
					insertReportingCode(units, stmt);
				}
			}
			// insert reporting code before return or throwing an exception within main
			if (body.getMethod().getSubSignature().equals("void main(java.lang.String[])") && (stmt instanceof ReturnStmt || stmt instanceof ReturnVoidStmt 
					|| stmt instanceof ThrowStmt)) {
				insertReportingCode(units, stmt);
			}
		}
	}

	/**
	 * import current global countdown into a local countdown at function entry
	 * 
	 * @param body
	 * @param units
	 * @return
	 */
	private Local importGlobalCountdownIntoLocal(Body body, Chain<Unit> units) {
		Local countdown = Jimple.v().newLocal("_localCountdown", IntType.v());
		body.getLocals().add(countdown);
		
		InvokeExpr getCountdownExpr = Jimple.v().newStaticInvokeExpr(getCountdown.makeRef());
		AssignStmt getCountdownStmt = Jimple.v().newAssignStmt(countdown, getCountdownExpr);
//		Stmt point = getLastIdentityStmt(units);
//		if(point == null){
//			assert(units.getFirst() == getFirstNonIdentityStmt(units));
//			units.addFirst(getCountdownStmt);
//		}
//		else{
//			units.insertAfter(getCountdownStmt, point);
//		}
		insertEquivalentBefore(units, getCountdownStmt, getFirstNonIdentityStmt(units));
		
		return countdown;
	}

	/**
	 * export local countdown back to global before function exit, and by the way remove nop statement
	 * 
	 * @param units
	 * @param countdown
	 */
	private void exportLocalCountdownToGlobal(Chain<Unit> units, Local countdown) {
		Iterator<Unit> stmtIterator = units.snapshotIterator();
		while(stmtIterator.hasNext()){
			Stmt stmt = (Stmt) stmtIterator.next();
			
			//export local countdown 
			if (stmt instanceof ReturnStmt || stmt instanceof ReturnVoidStmt || stmt instanceof ThrowStmt) {
				InvokeExpr invokeExpr = Jimple.v().newStaticInvokeExpr(setCountdown.makeRef(), countdown);
				InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(invokeExpr);
				units.insertBefore(invokeStmt, stmt);
			}
			
//			//remove nopstmt
//			if(stmt instanceof NopStmt){
//				units.remove(stmt);
//			}
		}
	}

	
	/**
	 * insert sample checking code at each loop back point
	 * 
	 * @param body
	 * @param units
	 * @param weight_loops
	 * @param countdown
	 * @param unitsMap
	 */
	private void insertSampleCheckingCodeAtLoopback(Body body, Chain<Unit> units, Map<Unit, Integer> weight_loops,
			Local countdown, Map<Unit, Unit> unitsMap) {
		//nested loops
		LoopNestTree loopNestTree = new LoopNestTree(body);
		for(Loop loop: loopNestTree){
			//only consider the original code
			Stmt slowLoopHead = loop.getHead();
			int weight;
			if(unitsMap.keySet().contains(slowLoopHead) && (weight = weight_loops.get(slowLoopHead)) != 0){
				Stmt slowBackJumpStmt = loop.getBackJumpStmt();
				Stmt fastBackJumpStmt = (Stmt) unitsMap.get(slowBackJumpStmt);
				
				//deal with the special case with gotoStmt as backJumpStmt
				if(slowBackJumpStmt instanceof GotoStmt){
					//sample checking code at loop back
					ConditionExpr condition_loop = Jimple.v().newLeExpr(countdown, IntConstant.v(weight));
					IfStmt ifStmt_loop = Jimple.v().newIfStmt(condition_loop, ((GotoStmt) slowBackJumpStmt).getTarget());
					units.insertAfter(ifStmt_loop, fastBackJumpStmt);	
					
					//decrease countdown at fast path
					BinopExpr binoExpr_loop = Jimple.v().newSubExpr(countdown, IntConstant.v(weight));
					AssignStmt decreaseStmt_loop = Jimple.v().newAssignStmt(countdown, binoExpr_loop);
					units.insertAfter(decreaseStmt_loop, ifStmt_loop);
					
					GotoStmt gotoFast = Jimple.v().newGotoStmt(((GotoStmt) fastBackJumpStmt).getTarget());
					units.insertAfter(gotoFast, decreaseStmt_loop);
					
					((GotoStmt) slowBackJumpStmt).setTarget(ifStmt_loop);
					((GotoStmt) fastBackJumpStmt).setTarget(ifStmt_loop);
				}
				else if(slowBackJumpStmt instanceof IfStmt){
					//sample checking code at loop back
					ConditionExpr condition_loop = Jimple.v().newLeExpr(countdown, IntConstant.v(weight));
					IfStmt ifStmt_loop = Jimple.v().newIfStmt(condition_loop, ((IfStmt) slowBackJumpStmt).getTarget());
					units.insertAfter(ifStmt_loop, fastBackJumpStmt);	
					
					//decrease countdown at fast path
					BinopExpr binoExpr_loop = Jimple.v().newSubExpr(countdown, IntConstant.v(weight));
					AssignStmt decreaseStmt_loop = Jimple.v().newAssignStmt(countdown, binoExpr_loop);
					units.insertAfter(decreaseStmt_loop, ifStmt_loop);
					
					GotoStmt gotoFast = Jimple.v().newGotoStmt(((IfStmt) fastBackJumpStmt).getTarget());
					units.insertAfter(gotoFast, decreaseStmt_loop);
					
					
					((IfStmt) slowBackJumpStmt).setTarget(ifStmt_loop);
					((IfStmt) fastBackJumpStmt).setTarget(ifStmt_loop);
					
					NopStmt nopStmt = Jimple.v().newNopStmt();
					units.insertAfter(nopStmt, gotoFast);
					
					GotoStmt goAhead = Jimple.v().newGotoStmt(nopStmt);
					units.insertAfter(goAhead, fastBackJumpStmt);
				}
				else if(slowBackJumpStmt instanceof LookupSwitchStmt || slowBackJumpStmt instanceof TableSwitchStmt){
					System.err.println("BackJumpStmt is SwitchStmt: " + slowBackJumpStmt);
				}
				else{
					//nop
					NopStmt slowNop = Jimple.v().newNopStmt();
					units.insertAfter(slowNop, slowBackJumpStmt);
					
					NopStmt fastNop = Jimple.v().newNopStmt();
					units.insertAfter(fastNop, fastBackJumpStmt);
					
					//sample checking code at loop back
					ConditionExpr condition_loop = Jimple.v().newLeExpr(countdown, IntConstant.v(weight));
					IfStmt ifStmt_loop = Jimple.v().newIfStmt(condition_loop, slowNop);
					units.insertAfter(ifStmt_loop, fastBackJumpStmt);	
					
					//decrease countdown at fast path
					BinopExpr binoExpr_loop = Jimple.v().newSubExpr(countdown, IntConstant.v(weight));
					AssignStmt decreaseStmt_loop = Jimple.v().newAssignStmt(countdown, binoExpr_loop);
					units.insertAfter(decreaseStmt_loop, ifStmt_loop);
					
					GotoStmt gotoFast = Jimple.v().newGotoStmt(fastNop);
					units.insertAfter(gotoFast, decreaseStmt_loop);
					
					
					//go back to sample checking code from slow path 
					GotoStmt gotoChecking = Jimple.v().newGotoStmt(ifStmt_loop);
					units.insertAfter(gotoChecking, slowBackJumpStmt);
				}
			}
		}
	}

	/**
	 * insert before certain point by inserting after the stmt immediately preceding point
	 * 
	 * @param units
	 * @param toInsert
	 * @param point
	 */
	private void insertEquivalentBefore(Chain<Unit> units, Stmt toInsert, Stmt point) {
		if(units.getFirst() == point){
			units.addFirst(toInsert);
		}
		else{
			units.insertAfter(toInsert, units.getPredOf(point));
		}
	}

	
	/**
	 * duplicate code to build fast path, i.e., add cloned code at the end of the original code
	 * 
	 * @param units
	 * @param stmtIt
	 * @param trapIt 
	 * @param traps 
	 * @return
	 */
	private Map<Unit, Unit> addClonedCodeAsFastpath(Chain<Unit> units, Iterator<Unit> stmtIt, Chain<Trap> traps, Iterator<Trap> trapIt) {
		//duplicate units
		Map<Unit, Unit> unitsMap = new HashMap<Unit, Unit>();
		while(stmtIt.hasNext()){
			Stmt stmt = (Stmt) stmtIt.next();
			Stmt newStmt = (Stmt) stmt.clone();
			if(!isNonCaughtExceptionIdentityStmt(stmt)){
				unitsMap.put(stmt, newStmt);
				units.insertAfter(newStmt, units.getLast());
			}
		}
		
		//change targets accordingly
		for(Unit oldStmt: unitsMap.keySet()){
			if(oldStmt instanceof IfStmt){
				IfStmt newIfStmt = (IfStmt) unitsMap.get(oldStmt);
				newIfStmt.setTarget(unitsMap.get(((IfStmt) oldStmt).getTarget()));
			}
			else if(oldStmt instanceof GotoStmt){
				GotoStmt newGotoStmt = (GotoStmt) unitsMap.get(oldStmt);
				newGotoStmt.setTarget(unitsMap.get(((GotoStmt) oldStmt).getTarget()));
			}
			else if(oldStmt instanceof LookupSwitchStmt){
				LookupSwitchStmt newSwitchStmt = (LookupSwitchStmt) unitsMap.get(oldStmt);
				
				newSwitchStmt.setDefaultTarget(unitsMap.get(((LookupSwitchStmt) oldStmt).getDefaultTarget()));
				List<Unit> targets = newSwitchStmt.getTargets();
				for(int index = 0; index < targets.size(); index++){
					newSwitchStmt.setTarget(index, unitsMap.get(((LookupSwitchStmt) oldStmt).getTarget(index)));
				}
			}
			else if(oldStmt instanceof TableSwitchStmt){
				TableSwitchStmt newSwitchStmt = (TableSwitchStmt) unitsMap.get(oldStmt);
				
				newSwitchStmt.setDefaultTarget(unitsMap.get(((TableSwitchStmt) oldStmt).getDefaultTarget()));
				List<Unit> targets = newSwitchStmt.getTargets();
				for(int index = 0; index < targets.size(); index++){
					newSwitchStmt.setTarget(index, unitsMap.get(((TableSwitchStmt) oldStmt).getTarget(index)));
				}
			}
		}
		
		
		//duplicate traps
		while(trapIt.hasNext()){
			Trap trap = trapIt.next();
			Trap newTrap = (Trap) trap.clone();
			//change trap accordingly
			changeTrap(newTrap, unitsMap);
			traps.insertAfter(newTrap, traps.getLast());
		}
		
		return unitsMap;
	}

	
	/** update trap accordingly: including beginUnit, endUnit, handlerUnit
	 * @param newTrap
	 * @param unitsMap
	 */
	private void changeTrap(Trap newTrap, Map<Unit, Unit> unitsMap) {
		Unit begin = newTrap.getBeginUnit();
		Unit end = newTrap.getEndUnit();
		Unit handler = newTrap.getHandlerUnit();
		
		newTrap.setBeginUnit(unitsMap.get(begin));
		newTrap.setEndUnit(unitsMap.get(end));
		newTrap.setHandlerUnit(unitsMap.get(handler));
	}

	
	/** determine if stmt is nonCaughtException IdentityStmt: ParameterIdentity or ThisIdentity
	 * @param stmt
	 * @return
	 */
	private boolean isNonCaughtExceptionIdentityStmt(Stmt stmt){
		if(stmt instanceof IdentityStmt && !(((IdentityStmt) stmt).getRightOp() instanceof CaughtExceptionRef)){
			return true;
		}
		return false;
	}
	
	/**
	 * insert sample checking code at the function entry point
	 * 
	 * @param body
	 * @param units
	 * @param weight_function
	 * @param countdown
	 */
	private void insertSampleCheckingCodeAtFunctionentry(Body body, Chain<Unit> units, int weight_function, Local countdown) {
			NopStmt nopFast = Jimple.v().newNopStmt();
			units.insertAfter(nopFast, units.getLast());
			
			NopStmt nopSlow = Jimple.v().newNopStmt();
			
			//add sample checking code at the very beginning
			ConditionExpr condition = Jimple.v().newLeExpr(countdown, IntConstant.v(weight_function));
			IfStmt ifStmt_functionentry = Jimple.v().newIfStmt(condition, nopSlow);
			units.insertAfter(ifStmt_functionentry, getFirstNonIdentityStmt(units));
			
			//decrease countdown at fast path
			BinopExpr binoExpr = Jimple.v().newSubExpr(countdown, IntConstant.v(weight_function));
			AssignStmt decreaseStmt = Jimple.v().newAssignStmt(countdown, binoExpr);
			units.insertAfter(decreaseStmt, ifStmt_functionentry);
			
			GotoStmt gotoStmt = Jimple.v().newGotoStmt(nopFast);
			units.insertAfter(gotoStmt, decreaseStmt);
			
			units.insertAfter(nopSlow, gotoStmt);
	}
	

	/**
	 * compute region weights: for whole function region and each loop region 
	 * 
	 * @param body
	 * @param units
	 * @param weight_loops: weight for each loop region
	 * @param analysis
	 * @return weight for the entire function region
	 */
	private int computeWeights(Body body, Chain<Unit> units, Map<Unit, Integer> weight_loops, InitAnalysis analysis) {
		int weight_function = 0;
		boolean instrumentEntry_flag = true;
		Value def = null;
		
		boolean flag_function = true;
		int num_function = 0;
		
		//compute function region weight
		Iterator<Unit> stmtIt = units.snapshotIterator();
		while (stmtIt.hasNext()) {
			// cast back to a statement
			Stmt stmt = (Stmt) stmtIt.next();
			
			// for method-entries
			if (this.methodentries_flag && instrumentEntry_flag && !(stmt instanceof IdentityStmt)) {
				weight_function++;
				instrumentEntry_flag = false;
			}
			
			// for branches
			if (this.branches_flag && stmt instanceof IfStmt) {
				weight_function++;
			}
			// for returns and scalar-pairs
			if (stmt instanceof AssignStmt && (def = ((AssignStmt) stmt).getLeftOp()).getType() instanceof PrimType) {
				// for returns
				if (this.returns_flag && ((Stmt) stmt).containsInvokeExpr()) {
					weight_function++;
				}
				// for scalar-pairs
				else if(this.scalarpairs_flag && !((Stmt) stmt).containsInvokeExpr() && flag_function){
					if(num_function > Limit_ScalarPairs){
						flag_function = false;
					}
					
					if (!(def instanceof soot.Local)) {
						def = ((AssignStmt) stmt).getRightOp();
						continue;
					}
					
					Iterator<Local> it = ((FlowSet) analysis.getFlowAfter(stmt)).iterator();
					List<Local> locals = new ArrayList<Local>();
					while(it.hasNext()){
						Local local = (Local) it.next();
						if (local.getType() == def.getType() && local != def) {
							locals.add(local);
						}
					}
					for(int i = 0; i < locals.size(); i++){
						if(locals.size() > Threshold_Locals && i % (locals.size() / Limit_Site + 1) != 0){
							continue;
						}
						weight_function++;
						num_function++;
					}	
				}
			}
		}
		
		//compute loop region weights
		LoopNestTree loopNestTree = new LoopNestTree(body);
		for(Loop loop: loopNestTree){
			int counts = 0;
			
			boolean flag_loop = true;
			int num_loop = 0;
			
			for(Stmt stmt: loop.getLoopStatements()){
				// for branches
				if (this.branches_flag && stmt instanceof IfStmt) {
					counts++;
				}
				// for returns and scalar-pairs
				if (stmt instanceof AssignStmt && (def = ((AssignStmt) stmt).getLeftOp()).getType() instanceof PrimType) {
					// for returns
					if (this.returns_flag && ((Stmt) stmt).containsInvokeExpr()) {
						counts++;
					}
					// for scalar-pairs
					else if(this.scalarpairs_flag && !((Stmt) stmt).containsInvokeExpr() && flag_loop){
						if(num_loop > PInstrumentor.Limit_ScalarPairs){
							flag_loop = false;
						}
						
						if (!(def instanceof soot.Local)) {
							def = ((AssignStmt) stmt).getRightOp();
							continue;
						}
						
						Iterator<Local> it = ((FlowSet) analysis.getFlowAfter(stmt)).iterator();
						List<Local> locals = new ArrayList<Local>();
						while(it.hasNext()){
							Local local = (Local) it.next();
							if (local.getType() == def.getType() && local != def) {
								locals.add(local);
							}
						}
						for(int i = 0; i < locals.size(); i++){
							if(locals.size() > Threshold_Locals && i % (locals.size() / Limit_Site + 1) != 0){
								continue;
							}
							counts++;
							num_loop++;
						}
					}
				}
			}
			
			weight_loops.put(loop.getHead(), counts);
		}
		
		return weight_function;
	}


	/** return the first non-identity statement
	 * @param units
	 * @return
	 */
	private Stmt getFirstNonIdentityStmt(Chain<Unit> units) {
		// TODO Auto-generated method stub
		Stmt firstNonIdentityStmt = null;
		Iterator<Unit> stmtIt = units.snapshotIterator();
		while(stmtIt.hasNext()){
			Stmt stmt = (Stmt) stmtIt.next();
			if(!isNonCaughtExceptionIdentityStmt(stmt)){
				firstNonIdentityStmt = stmt;
				break;
			}
		}
		return firstNonIdentityStmt;
	}

//	/** return the last non-identity statement
//	 * @param units
//	 * @return
//	 */
//	private Stmt getLastIdentityStmt(Chain<Unit> units){
//		Stmt lastIdentityStmt = null;
//		
//		Iterator<Unit> stmtIt = units.snapshotIterator();
//		while(stmtIt.hasNext()){
//			Stmt stmt = (Stmt) stmtIt.next();
//			if(isNonCaughtExceptionIdentityStmt(stmt)){
//				lastIdentityStmt = stmt;
//			}
//			else{
//				break;
//			}
//		}
//		return lastIdentityStmt;
//	}

	/**
	 * insert reporting code before program exit
	 * 
	 * @param units
	 * @param stmt
	 * @param output_file 
	 */
	private void insertReportingCode(Chain<Unit> units, Stmt stmt) {
		InvokeExpr reportExpr = Jimple.v().newStaticInvokeExpr(report.makeRef(), StringConstant.v(unit_signature), 
				IntConstant.v(this.counts_branch), IntConstant.v(this.counts_return), IntConstant.v(this.counts_scalarPair), IntConstant.v(this.counts_methodEntry));
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
	 * @param countdown 
	 */
	private void instrumentScalarPairs(int file_name, int method_name, int line_number, int cfg_number, Body body,
			Chain<Unit> units, Stmt stmt, Value def, InitAnalysis analysis, Local countdown) {
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
			def = ((AssignStmt) stmt).getRightOp();
			return;
		}

		Iterator it = ((FlowSet) analysis.getFlowAfter(stmt)).iterator();
		List<Local> locals = new ArrayList<Local>();
		while(it.hasNext()){
			Local local = (Local) it.next();
			if (local.getType() == def.getType() && local != def) {
				locals.add(local);
			}
		}
		for(int i = 0; i < locals.size(); i++){
			if(locals.size() > Threshold_Locals && i % (locals.size() / Limit_Site + 1) != 0){
				continue;
			}
			
			Local local = locals.get(i);
			
			counts_scalarPair_inst++;
			
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
			
			//insert checking code for sampling
			insertSampleCheckingCode(units, checkScalarPairStmt, countdown);
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
	 * @param countdown 
	 */
	private void instrumentBranches(int file_name, int method_name, int line_number, int cfg_number, Body body,
			Chain<Unit> units, Stmt stmt, Local countdown) {
		Value conditional = ((IfStmt) stmt).getCondition();
		assert(conditional instanceof ConditionExpr);

		// create static instrumentation site for branch
		BranchSite site = new BranchSite(file_name, line_number, method_name, cfg_number, conditional.toString());
		branch_staticInfo.add(site);

		// insert checking code
		Value leftop = ((ConditionExpr) conditional).getOp1();
		Value rightop = ((ConditionExpr) conditional).getOp2();
		String symbol = ((ConditionExpr) conditional).getSymbol();
		
		InvokeExpr checkBranch = null;
		if(leftop.getType() instanceof PrimType){
			checkBranch = Jimple.v().newStaticInvokeExpr(checkBranches_int.makeRef(), leftop, rightop, StringConstant.v(symbol.trim()), IntConstant.v(branch_staticInfo.size() - 1));
		}
		else{
			checkBranch = Jimple.v().newStaticInvokeExpr(checkBranches_object.makeRef(), leftop, rightop, StringConstant.v(symbol.trim()), IntConstant.v(branch_staticInfo.size() - 1));
		}
		Stmt checkBranchStmt = Jimple.v().newInvokeStmt(checkBranch);
		units.insertBefore(checkBranchStmt, stmt);
		
		//insert checking code for sampling
		insertSampleCheckingCode(units, checkBranchStmt, countdown);
	}

	/**
	 * get the source line number
	 * 
	 * @param stmt
	 * @return
	 */
	private int getSourceLineNumber(Stmt stmt) {
		// TODO Auto-generated method stub
		if(stmt.getTag("LineNumberTag") == null) return -1;
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
	 * @param countdown 
	 */
	private void instrumentMethodEntries(int file_name, int method_name, int line_number, int cfg_number, Body body,
			Chain<Unit> units, Unit stmt, Local countdown) {
		// static instrumentation site for method entry
		MethodEntrySite site = new MethodEntrySite(file_name, line_number, method_name, cfg_number);
		methodEntry_staticInfo.add(site);

		// insert checking code
		InvokeExpr checkMethodEntry = Jimple.v().newStaticInvokeExpr(checkMethodEntries.makeRef(), IntConstant.v(methodEntry_staticInfo.size() - 1));
		Stmt checkMethodEntryStmt = Jimple.v().newInvokeStmt(checkMethodEntry);
		insertEquivalentBefore(units, checkMethodEntryStmt, (Stmt) stmt);
		
		//insert checking code for sampling
		insertSampleCheckingCode(units, checkMethodEntryStmt, countdown);
	}

	
	/**
	 * insert sample checking code at each instrumentation site
	 * 
	 * @param units
	 * @param checkStmt
	 * @param countdown
	 */
	private void insertSampleCheckingCode(Chain<Unit> units, Stmt checkStmt, Local countdown) {
		if(this.sample_flag){
			BinopExpr binopExpr = Jimple.v().newSubExpr(countdown, IntConstant.v(1));
			AssignStmt decreaseStmt = Jimple.v().newAssignStmt(countdown, binopExpr);
			units.insertBefore(decreaseStmt, checkStmt);
			
			InvokeExpr getNextCountdownExpr = Jimple.v().newStaticInvokeExpr(getNextCountdown.makeRef());
			AssignStmt assignNextCountdownStmt = Jimple.v().newAssignStmt(countdown, getNextCountdownExpr);
			units.insertAfter(assignNextCountdownStmt, checkStmt);
			
			NopStmt nopStmt = Jimple.v().newNopStmt();
			units.insertAfter(nopStmt, assignNextCountdownStmt);
			
			ConditionExpr conditionExpr = Jimple.v().newNeExpr(countdown, IntConstant.v(0));
			IfStmt ifStmt = Jimple.v().newIfStmt(conditionExpr, nopStmt);
			units.insertBefore(ifStmt, checkStmt);
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
	 * @param def
	 * @param countdown 
	 */
	private void instrumentReturns(int file_name, int method_name, int line_number, int cfg_number, Body body,
			Chain<Unit> units, Unit stmt, Value def, Local countdown) {
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
		
		
		//insert checking code for sampling
		insertSampleCheckingCode(units, checkReturnStmt, countdown);
	}

	
	
}
