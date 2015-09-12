package edu.uci.jsampler.site;


public class BranchSite extends AbstractSite {
	final private String predicate;

	public BranchSite(int fileName, int lineNumber, int methodName, int cfgNumber, String predicate) {
		super(fileName, lineNumber, methodName, cfgNumber);
		this.predicate = predicate.intern();
	}

	public String getPredicate() {
		return predicate;
	}

	
	public String printToString(){
		StringBuilder builder = new StringBuilder();
		builder.append(super.printToString()).append("\t").append(this.predicate);
		return builder.toString();
	}

	@Override
	public String getSchemeName() {
		// TODO Auto-generated method stub
		return "branches";
	}

}
