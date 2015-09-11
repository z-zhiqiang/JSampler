package edu.uci.jsampler.site;


public class BranchSite extends AbstractSite {
	final private String predicate;

	public BranchSite(String fileName, int lineNumber, String methodName, int cfgNumber, String predicate) {
		super(fileName, lineNumber, methodName, cfgNumber);
		this.predicate = predicate.intern();
	}

	public String getPredicate() {
		return predicate;
	}

	@Override
	protected void toString(StringBuilder builder) {
		builder.append("{predicate=").append(predicate).append('}');
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
