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

	@Override
	public SiteCategory getCategory() {
		// TODO Auto-generated method stub
		return SiteCategory.BRANCH;
	}

}
