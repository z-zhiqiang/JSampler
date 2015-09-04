package edu.uci.jsampler.instrument;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.util.Chain;

public class PInstrumentor extends BodyTransformer {

	private boolean branches_flag;

	private boolean returns_flag;

	private boolean scalarpairs_flag;

	private boolean functionentries_flag;

	private boolean sample_flag;

	private int opportunities;
	
	private Set<String> methods_instrument;

	
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
			case "-sampler-scheme=function-entries":
				this.functionentries_flag = true;
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

	
	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		// TODO Auto-generated method stub
		// body's method
		SootMethod method = body.getMethod();
		System.out.println(method.getSignature());

		// get body's units
		Chain units = body.getUnits();
		Iterator stmtIt = units.snapshotIterator();

		while (stmtIt.hasNext()) {
			// cast back to a statement
			Stmt stmt = (Stmt) stmtIt.next();

			// for function-entries

			
			// for returns
			if (stmt instanceof ReturnStmt || stmt instanceof ReturnVoidStmt) {
				//

			}

			// for branches
			if (stmt instanceof IfStmt) {
				//

			}

			// for scalar-pairs
			if (stmt instanceof AssignStmt) {
				//

			}

			
		}

	}

}
