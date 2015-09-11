package edu.uci.jsampler.site;


public class MethodEntrySite extends AbstractSite {
	public MethodEntrySite(String fileName, int lineNumber, String methodName, int cfgNumber) {
		super(fileName, lineNumber, methodName, cfgNumber);
	}

	@Override
	protected void toString(StringBuilder builder) {
		// TODO Auto-generated method stub
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
