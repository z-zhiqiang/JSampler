package edu.uci.jsampler.site;


public class MethodEntrySite extends AbstractSite {
	public MethodEntrySite(int fileName, int lineNumber, int methodName, int cfgNumber) {
		super(fileName, lineNumber, methodName, cfgNumber);
	}


	public String printToString(){
		return super.printToString();
	}

	@Override
	public String getSchemeName() {
		// TODO Auto-generated method stub
		return "function-entries";
	}
}
