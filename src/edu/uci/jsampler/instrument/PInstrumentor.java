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
import soot.jimple.Jimple;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.tagkit.*;
import soot.util.Chain;

public class PInstrumentor extends BodyTransformer {
	
	//internal fields
	static SootClass checkerClass;
	static SootMethod checkReturns, checkBranches, checkScalarPairs, checkMethodEntries;
	
	static{
		checkerClass = Scene.v().loadClassAndSupport("edu.uci.jsampler.instrument.StaticChecker");
		checkReturns = checkerClass.getMethod("void checkReturns(java.lang.Object,int)");
		checkBranches = checkerClass.getMethod("void checkBranches(boolean,int)");
		checkScalarPairs = checkerClass.getMethod("void checkScalarPairs(java.lang.Object,java.lang.Object,int)");
		checkMethodEntries = checkerClass.getMethod("void checkMethodEntries(int)");
	}

	//instrumentation flag
	private boolean branches_flag;

	private boolean returns_flag;

	private boolean scalarpairs_flag;

	private boolean methodentries_flag;

	//sampling flag
	private boolean sample_flag;

	private int opportunities;
	
	//methods instrumented
	private Set<String> methods_instrument;
	
	//static instrumentation site information
	public static final List<ReturnSite> return_staticInfo = new ArrayList<ReturnSite>();
	
	public static final List<BranchSite> branch_staticInfo = new ArrayList<BranchSite>();

	public static final List<ScalarPairSite> scalarPair_staticInfo = new ArrayList<ScalarPairSite>();

	public static final List<MethodEntrySite> methodEntry_staticInfo = new ArrayList<MethodEntrySite>();
	
	
	public static String unit_signature;//compilation unit signature: a 128-bit as 32 hexadecimal digits
	
	
	/**constructor mainly for sampler options parsing
	 * @param sampler_options
	 */
	public PInstrumentor(List<String> sampler_options) {
		System.out.println("constructor.........................");
		this.methods_instrument = new HashSet<String>();
		// parse the parameters to initialize flag fields
		for (String option : sampler_options) {
			switch (option) {
			case "-sampler-scheme=branches":
				this.branches_flag = true;
				break;
			case "-sampler-scheme=returns":
				this.returns_flag = true;
				break;
			case "-sampler-scheme=scalar-pairs":
				this.scalarpairs_flag = true;
				break;
			case "-sampler-scheme=method-entries":
				this.methodentries_flag = true;
				break;
			case "-sampler":
				this.sample_flag = true;
				break;
			case "-sampler-no":
				this.sample_flag = false;
				break;
			case "-sampler-opportunities=\\d+":
				this.opportunities = Integer.parseInt(option.split("=")[1].trim());
				break;
			case "-sampler-include-method=.*":
				this.methods_instrument.add(option.split("=")[1].trim());
			default:
				System.err.println("wrong options!");
			}
		}

		//initialize 128-bit compilation unit id
		this.unit_signature = generateUnitSignature();
		
	}


	private String generateUnitSignature() {
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
		// need to get file name
		// to-do
		String file_name = "";
		file_name = body.getMethod().getDeclaringClass().getName();
		System.out.println(file_name);
		
		// body's method
		SootMethod method = body.getMethod();
		String method_name = method.getSignature();
		System.out.println(method_name);
		
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
				instrumentMethodEntries(file_name, method_name, line_number, cfg_number, body, units, stmt);
				instrumentEntry_flag = false;
			}	
			
			// for branches
			if (stmt instanceof IfStmt) {
				instrumentBranches(file_name, method_name, line_number, cfg_number, body, units, stmt);
			}
			// for returns and scalar-pairs
			if (stmt instanceof AssignStmt && (def = ((AssignStmt) stmt).getLeftOp()).getType() instanceof PrimType && !(def.getType() instanceof BooleanType)) {
				//for returns
				if(((Stmt) stmt).containsInvokeExpr()){
					instrumentReturns(file_name, method_name, line_number, cfg_number, body, units, stmt, def);
				}
				//for scalar-pairs
				else{
					instrumentScalarPairs(file_name, method_name, line_number, cfg_number, body, units, stmt, def, original_locals);
				}

			}

			//trace cfg_number
			cfg_number++;
		}
	}


	private void instrumentScalarPairs(String file_name, String method_name, int line_number, int cfg_number, 
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


	private void instrumentBranches(String file_name, String method_name, int line_number, int cfg_number,
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


	private void instrumentMethodEntries(String file_name, String method_name, int line_number, int cfg_number,
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
	private void instrumentReturns(String file_name, String method_name, int line_number, int cfg_number, 
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
