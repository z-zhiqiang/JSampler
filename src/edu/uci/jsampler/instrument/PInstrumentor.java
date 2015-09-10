package edu.uci.jsampler.instrument;

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
	
	
	
	/**constructor mainly for sampler options parsing
	 * @param sampler_options
	 */
	public PInstrumentor(List<String> sampler_options) {
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

	}

	
	/* (non-Javadoc)
	 * @see soot.BodyTransformer#internalTransform(soot.Body, java.lang.String, java.util.Map)
	 */
	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		// need to get file name
		// to-do
		String file_name = "";
//		stmt = units.getFirst();
//		String file_name = ((SourceFileTag) stmt.getTag("SourceFileTag")).getAbsolutePath();
//		file_name = ((SourceFileTag) body.getMethod().getDeclaringClass().getTag("SourceFileTag")).getAbsolutePath();
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

		Unit stmt = null;
		int line_number = 0;
		boolean instrumentEntry_flag = true;
		Value def = null;
		
		while (stmtIt.hasNext()) {
			// cast back to a statement
			stmt = stmtIt.next();
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
				Value conditional = ((IfStmt) stmt).getCondition();
//				instrumentBranches(file_name, method_name, line_number, cfg_number, units, stmt, conditional);
			}
			// for returns and scalar-pairs
			if (stmt instanceof AssignStmt && (def = ((AssignStmt) stmt).getLeftOp()).getType() instanceof PrimType && !(def.getType() instanceof BooleanType)) {
				//for returns
				if(((Stmt) stmt).containsInvokeExpr()){
					instrumentReturns(file_name, method_name, line_number, cfg_number, body, units, stmt, def);
				}
				//for scalar-pairs
				else{
					for(Local local: original_locals){
						if(local.getType() == def.getType()){
							instrumentScalarPairs(file_name, method_name, line_number, cfg_number, body, units, stmt, def, local);
						}
					}
				}

			}

			//trace cfg_number
			cfg_number++;
		}
	}


	private int getSourceLineNumber(Unit stmt) {
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
	 * @param local
	 */
	private void instrumentScalarPairs(String file_name, String method_name, int line_number, int cfg_number,
			Body body, Chain<Unit> units, Unit stmt, Value def, Value local) {
		/* static site information for scalar-pair */
		//left info
		Value leftOp = ((AssignStmt) stmt).getLeftOp();
		String left = leftOp.toString();
		assert(left.equals(def.toString()));
		String scope_type_assign = null, container_type = null, scope_type_compare = null;
		if(leftOp instanceof soot.Local){
			scope_type_assign = "local";
			container_type = "direct";
		}
		else if(leftOp instanceof soot.jimple.InstanceFieldRef){
			scope_type_assign = "local";
			container_type = "field";
		}
		else if (leftOp instanceof soot.jimple.StaticFieldRef){
			scope_type_assign = "global";
			container_type = "field";
		}
		else if(leftOp instanceof soot.jimple.ArrayRef){
			scope_type_assign = "mem";
			container_type = "index";
		}
		//right and compared variable info
		String right = ((AssignStmt) stmt).getRightOp().toString();
		scope_type_compare = "local";
		
		ScalarPairSite site = new ScalarPairSite(file_name, line_number, method_name, cfg_number, left, scope_type_assign, container_type, right, scope_type_compare);
		this.scalarPair_staticInfo.add(site);
		
		//insert checking code
		Local tmp = Jimple.v().newLocal("tmp" + cfg_number, def.getType());
		body.getLocals().add(tmp);
		Stmt assign = Jimple.v().newAssignStmt(tmp, def);
		units.insertAfter(assign, stmt);
		InvokeExpr checkScalarPair = Jimple.v().newStaticInvokeExpr(checkScalarPairs.makeRef(), tmp, local, IntConstant.v(this.scalarPair_staticInfo.size() - 1));
		Stmt checkScalarPairStmt = Jimple.v().newInvokeStmt(checkScalarPair);
		units.insertAfter(checkScalarPairStmt, stmt);
	}


	/**
	 * @param file_name
	 * @param method_name
	 * @param line_number
	 * @param cfg_number
	 * @param units
	 * @param stmt
	 * @param condition
	 */
	private void instrumentBranches(String file_name, String method_name, int line_number, int cfg_number, 
			Chain<Unit> units, Unit stmt, Value condition) {
		//condition predicate
		String predicate = condition.toString();
		
		//create static instrumentation site for branch
		BranchSite site = new BranchSite(file_name, line_number, method_name, cfg_number, predicate);
		this.branch_staticInfo.add(site);
		
		//insert checking code 
		InvokeExpr checkBranch = Jimple.v().newStaticInvokeExpr(checkBranches.makeRef(), condition, IntConstant.v(this.branch_staticInfo.size() - 1));
		Stmt checkBranchStmt = Jimple.v().newInvokeStmt(checkBranch);
		units.insertAfter(checkBranchStmt, stmt);
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
