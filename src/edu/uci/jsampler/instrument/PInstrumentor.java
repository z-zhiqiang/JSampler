package edu.uci.jsampler.instrument;

import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.util.Chain;

public class PInstrumentor extends BodyTransformer {

	
	
	
	
	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		// TODO Auto-generated method stub
		//body's method
		SootMethod method = body.getMethod();
		System.out.println(method.getSignature());
		
		//get body's units
		Chain units = body.getUnits();
		Iterator stmtIt = units.snapshotIterator();
		
		while(stmtIt.hasNext()){
			//cast back to a statement
			Stmt stmt = (Stmt) stmtIt.next();
			
			
			
			
			
		}
		
		
	}
	
	

}
