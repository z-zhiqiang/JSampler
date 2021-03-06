package edu.uci.jsampler.transformer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PrimType;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.InitAnalysis;
import soot.util.Chain;

public class PCounter extends BodyTransformer {
	public static final String Delimiter = "-";
	
	// instrumentation flag
	private final boolean branches_flag;

	private final boolean returns_flag;

	private final boolean scalarpairs_flag;

	private final boolean methodentries_flag;

	private final Set<String> methods_instrument;// methods instrumented
	
	// counts
	public static int counts_branch;
	
	public static int counts_return;
	
	public static int counts_scalarPair;
	
	public static int counts_methodEntry;
	

	public PCounter(boolean branches_flag, boolean returns_flag, boolean scalarpairs_flag, boolean methodentries_flag, Set<String> methods_instrument) {
		// TODO Auto-generated constructor stub
		this.branches_flag = branches_flag;
		this.returns_flag = returns_flag;
		this.scalarpairs_flag = scalarpairs_flag;
		this.methodentries_flag = methodentries_flag;
		this.methods_instrument = methods_instrument;
	}

	@Override
	protected void internalTransform(Body body, String phaseName, Map options) {
		// get body's units
		Chain<Unit> units = body.getUnits();
		
		Iterator<Unit> original_stmtIt = units.snapshotIterator();
		
		//initialized variables
		InitAnalysis analysis = new InitAnalysis(new BriefUnitGraph(body));
		
		boolean under_analysis = this.methods_instrument.isEmpty() || this.methods_instrument.contains(transform(body.getMethod().getSignature()));		
		
		//instrument the specified methods
		if(under_analysis){
			boolean instrumentEntry_flag = true;
			Value def = null;
			
			boolean flag = true;
			int num = counts_scalarPair;
			
			while (original_stmtIt.hasNext()) {
				// cast back to a statement
				Stmt stmt = (Stmt) original_stmtIt.next();
				
				// for method-entries
				if (this.methodentries_flag && instrumentEntry_flag && !(stmt instanceof IdentityStmt)) {
					counts_methodEntry++;
					instrumentEntry_flag = false;
				}
				
				// for branches
				if (this.branches_flag && stmt instanceof IfStmt) {
					counts_branch++;
				}
				// for returns and scalar-pairs
				if (stmt instanceof AssignStmt && (def = ((AssignStmt) stmt).getLeftOp()).getType() instanceof PrimType) {
					// for returns
					if (this.returns_flag && ((Stmt) stmt).containsInvokeExpr()) {
						counts_return++;
					}
					// for scalar-pairs
					else if(this.scalarpairs_flag && !((Stmt) stmt).containsInvokeExpr() && flag){
						if(counts_scalarPair - num > PInstrumentor.Limit_ScalarPairs){
							flag = false;
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
							if(locals.size() > PInstrumentor.Threshold_Locals && i % (locals.size() / PInstrumentor.Limit_Site + 1) != 0){
								continue;
							}
							counts_scalarPair++;
						}	
						
					}
				}
			}
			
		}
	}
	
	
	public static String transform(String method_signature){
		return method_signature.replaceAll(" ", Delimiter)
				.replaceAll("\\(", Delimiter).replaceAll("\\)", Delimiter)
				.replaceAll(":", Delimiter)
				.replaceAll("<", Delimiter).replaceAll(">", Delimiter)
				.replaceAll("\\$", Delimiter);
	}
	

}
