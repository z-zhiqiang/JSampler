package edu.uci.jsampler.site;


public class ReturnSite extends AbstractSite{

	private final String callee;

	public ReturnSite(int fileName, int lineNumber, int methodName,
			int cfgNumber, String callee) {
		super(fileName, lineNumber, methodName, cfgNumber);
		this.callee = callee.intern();
	}

	public String getCallee() {
		return callee;
	}


	public String printToString(){
		StringBuilder builder = new StringBuilder();
		builder.append(super.printToString()).append("\t").append(this.callee);
		return builder.toString();
	}

	@Override
	public String getSchemeName() {
		// TODO Auto-generated method stub
		return "returns";
	}
}
