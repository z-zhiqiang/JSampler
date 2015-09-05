package edu.uci.jsampler.site;


public class ReturnSite extends AbstractSite{

	private final String callee;

	public ReturnSite(String fileName, int lineNumber, String methodName,
			int cfgNumber, String callee) {
		super(fileName, lineNumber, methodName, cfgNumber);
		this.callee = callee.intern();
	}

	public String getCallee() {
		return callee;
	}

	@Override
	protected void toString(StringBuilder builder) {
		builder.append("{callee=").append(callee).append('}');
	}

	@Override
	public SiteCategory getCategory() {
		// TODO Auto-generated method stub
		return SiteCategory.RETURN;
	}

}
