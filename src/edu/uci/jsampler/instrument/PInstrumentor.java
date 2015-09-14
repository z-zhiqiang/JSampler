package edu.uci.jsampler.instrument;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
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
import soot.BooleanType;
import soot.Local;
import soot.PrimType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.tagkit.*;
import soot.util.Chain;

public class PInstrumentor extends BodyTransformer {
	
	//internal fields
	static SootClass checkerReporterClass;
	static SootMethod checkReturns, checkBranches, checkScalarPairs, checkMethodEntries;
	static SootMethod report;
	
	static{
		checkerReporterClass = Scene.v().loadClassAndSupport("edu.uci.jsampler.instrument.StaticCheckerReporter");
		
		checkReturns = checkerReporterClass.getMethod("void checkReturns(java.lang.Object,int)");
		checkBranches = checkerReporterClass.getMethod("void checkBranches(boolean,int)");
		checkScalarPairs = checkerReporterClass.getMethod("void checkScalarPairs(java.lang.Object,java.lang.Object,int)");
		checkMethodEntries = checkerReporterClass.getMethod("void checkMethodEntries(int)");
		
		report = checkerReporterClass.getMethod("void exportReports(java.lang.String)");
	}

	//instrumentation flag
	private final boolean branches_flag;

	private final boolean returns_flag;

	private final boolean scalarpairs_flag;

	private final boolean methodentries_flag;
	

	private final boolean sample_flag;//sampling flag

	private final int opportunities;//sampling opportunities
	
	private final Set<String> methods_instrument;//methods instrumented
	
	
	private final String output_file_sites;//output file name storing static sites info
	
	private final String output_file_reports;//output file name for dynamic reports
	
	
	//static instrumentation site information
	public static final List<ReturnSite> return_staticInfo = new ArrayList<ReturnSite>();
	
	public static final List<BranchSite> branch_staticInfo = new ArrayList<BranchSite>();

	public static final List<ScalarPairSite> scalarPair_staticInfo = new ArrayList<ScalarPairSite>();

	public static final List<MethodEntrySite> methodEntry_staticInfo = new ArrayList<MethodEntrySite>();
	
	
	public static final String unit_signature = generateUnitSignature();//compilation unit signature: a 128-bit as 32 hexadecimal digits
	

	/**constructor mainly for sampler options parsing
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


	private static String generateUnitSignature() {
		byte[] unitID = new byte[16];
		SecureRandom random = new SecureRandom();
		random.nextBytes(unitID);
		
		StringBuilder builder = new StringBuilder();
		for(byte b: unitID){
			builder.append(String.format("%02X", b));
		}
		System.out.println(builder.toString());
		return builder.toString();
	}

	
	/* (non-Javadoc)
	 * @see soot.BodyTransformer#internalTransform(soot.Body, java.lang.String, java.util.Map)
	 */
	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		boolean isMain = body.getMethod().getSubSignature().equals("void main(java.lang.String[])");
		
		// need to get file name
		// to-do
		String file_name = body.getMethod().getDeclaringClass().getName();
		System.out.println(file_name);
		int file_name_translated = Translator.getInstance().getInteger(file_name);
		
		// body's method
		String method_name = body.getMethod().getSignature();
		System.out.println(method_name);
		int method_name_tranlated = Translator.getInstance().getInteger(method_name);
		
		// locals
		Chain<Local> locals = body.getLocals();
		List<Local> original_locals = new ArrayList<Local>();
		for(Local local: locals){
			original_locals.add(local);
		}

		// get body's units
		Chain<Unit> units = body.getUnits();
		Iterator<Unit> stmtIt = units.snapshotIterator();
		int cfg_number = 0;

		int line_number = 0;
		boolean instrumentEntry_flag = true;
		Value def = null;
		
		while (stmtIt.hasNext()) {
			// cast back to a statement
			Stmt stmt = (Stmt) stmtIt.next();
			System.out.println(stmt.toString());
			
			cfg_number++;
			line_number = getSourceLineNumber(stmt);
			
			// for method-entries
			if(instrumentEntry_flag && !(stmt instanceof IdentityStmt)){
				instrumentMethodEntries(file_name_translated, method_name_tranlated, line_number, cfg_number, body, units, stmt);
				instrumentEntry_flag = false;
			}	
			
			// for branches
			if (stmt instanceof IfStmt) {
				instrumentBranches(file_name_translated, method_name_tranlated, line_number, cfg_number, body, units, stmt);
			}
			// for returns and scalar-pairs
			if (stmt instanceof AssignStmt && (def = ((AssignStmt) stmt).getLeftOp()).getType() instanceof PrimType && !(def.getType() instanceof BooleanType)) {
				//for returns
				if(((Stmt) stmt).containsInvokeExpr()){
					instrumentReturns(file_name_translated, method_name_tranlated, line_number, cfg_number, body, units, stmt, def);
				}
				//for scalar-pairs
				else{
					instrumentScalarPairs(file_name_translated, method_name_tranlated, line_number, cfg_number, body, units, stmt, def, original_locals);
				}

			}

			// trace cfg_number
			cfg_number++;
			
			
			/* add reporting code */
			// insert reporting code before exit
			if(stmt instanceof InvokeStmt){
				InvokeExpr iexpr = stmt.getInvokeExpr();
				if(iexpr instanceof StaticInvokeExpr && iexpr.getMethod().getSignature().equals("<java.lang.System: void exit(int)>")){
					instrumentReport(units, stmt);
				}
			}
			// insert reporting code before return of main
			if(isMain && (stmt instanceof ReturnStmt || stmt instanceof ReturnVoidStmt)){
				instrumentReport(units, stmt);
			}
		}
	}


	private void instrumentReport(Chain<Unit> units, Stmt stmt) {
		InvokeExpr reportExpr = Jimple.v().newStaticInvokeExpr(report.makeRef(), StringConstant.v(output_file_reports));
		Stmt reportStmt = Jimple.v().newInvokeStmt(reportExpr);
		units.insertBefore(reportStmt, stmt);
	}


	private void instrumentScalarPairs(int file_name, int method_name, int line_number, int cfg_number, 
			Body body, Chain<Unit> units, Stmt stmt, Value def, List<Local> original_locals) {
		/* static site information for scalar-pair */
		//left info
		String left = def.toString();
		String scope_type_assign = null, container_type = null;
		if(def instanceof soot.Local){
			scope_type_assign = "local";
			container_type = "direct";
		}
		else if(def instanceof soot.jimple.InstanceFieldRef){
			scope_type_assign = "local";
			container_type = "field";
		}
		else if (def instanceof soot.jimple.StaticFieldRef){
			scope_type_assign = "global";
			container_type = "field";
		}
		else if(def instanceof soot.jimple.ArrayRef){
			scope_type_assign = "mem";
			container_type = "index";
		}
		//right and compared variable info
		String right = ((AssignStmt) stmt).getRightOp().toString();
		
		if(!(def instanceof soot.Local)){ 
			//insert checking code
			Local tmp = Jimple.v().newLocal("tmp" + cfg_number, def.getType());
			body.getLocals().add(tmp);
			Stmt inserted_assign = Jimple.v().newAssignStmt(tmp, def);
			units.insertAfter(inserted_assign, stmt);
			
			def = tmp;
			stmt = inserted_assign;
		}
		
		for(Local local: original_locals){
			if(local.getType() == def.getType()){
				//create static instrumentation site for scalar-pairs
				ScalarPairSite site = new ScalarPairSite(file_name, line_number, method_name, cfg_number, left, scope_type_assign, container_type, right, local.toString());
				this.scalarPair_staticInfo.add(site);
				
				//insert checking code 
				InvokeExpr checkScalarPair = Jimple.v().newStaticInvokeExpr(checkScalarPairs.makeRef(), def, local, IntConstant.v(this.scalarPair_staticInfo.size() - 1));
				Stmt checkScalarPairStmt = Jimple.v().newInvokeStmt(checkScalarPair);
				units.insertAfter(checkScalarPairStmt, stmt);
			}
		}
	}


	private void instrumentBranches(int file_name, int method_name, int line_number, int cfg_number,
			Body body, Chain<Unit> units, Stmt stmt) {
		Value conditional = ((IfStmt) stmt).getCondition();
		System.out.println(conditional.getType().toString());
		
		Local tmp = Jimple.v().newLocal("tmp" + cfg_number, conditional.getType());
		body.getLocals().add(tmp);
		Stmt inserted_assign = Jimple.v().newAssignStmt(tmp, conditional);
		units.insertBefore(inserted_assign, stmt);
		
		//create static instrumentation site for branch
		BranchSite site = new BranchSite(file_name, line_number, method_name, cfg_number, conditional.toString());
		this.branch_staticInfo.add(site);
		
		//insert checking code 
		InvokeExpr checkBranch = Jimple.v().newStaticInvokeExpr(checkBranches.makeRef(), tmp, IntConstant.v(this.branch_staticInfo.size() - 1));
		Stmt checkBranchStmt = Jimple.v().newInvokeStmt(checkBranch);
		units.insertBefore(checkBranchStmt, stmt);
	}


	private int getSourceLineNumber(Stmt stmt) {
		// TODO Auto-generated method stub
		return ((LineNumberTag) stmt.getTag("LineNumberTag")).getLineNumber();
	}


	private void instrumentMethodEntries(int file_name, int method_name, int line_number, int cfg_number,
			Body body, Chain<Unit> units, Unit stmt) {
		//static instrumentation site for method entry
		MethodEntrySite site = new MethodEntrySite(file_name, line_number, method_name, cfg_number); 
		this.methodEntry_staticInfo.add(site);
		
		//insert checking code
		InvokeExpr checkMethodEntry = Jimple.v().newStaticInvokeExpr(checkMethodEntries.makeRef(), IntConstant.v(this.methodEntry_staticInfo.size() - 1));
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
	private void instrumentReturns(int file_name, int method_name, int line_number, int cfg_number, 
			Body body, Chain<Unit> units, Unit stmt, Value def) {
		//callee
		String callee = "static";
		if(!(((Stmt) stmt).getInvokeExpr() instanceof StaticInvokeExpr)){
			callee = ((ValueBox) ((Stmt) stmt).getInvokeExpr().getUseBoxes().get(0)).getValue().toString();
		}
		
		//create static instrumentation site information for return
		ReturnSite site = new ReturnSite(file_name, line_number, method_name, cfg_number, callee);
		this.return_staticInfo.add(site);
		
		//insert checking code
		InvokeExpr checkReturn = Jimple.v().newStaticInvokeExpr(checkReturns.makeRef(), def, IntConstant.v(this.return_staticInfo.size() - 1));
		Stmt checkReturnStmt = Jimple.v().newInvokeStmt(checkReturn);
		units.insertAfter(checkReturnStmt, stmt);
	}

}
