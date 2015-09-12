package edu.uci.jsampler.site;

public class ScalarPairSite extends AbstractSite {
	// 5
	private final String left;
	// 6
	private final String leftType;
	// 7
	private final String containerType;
	// 8
	private final String right;
	// 9
	private final String rightType;

	public ScalarPairSite(int fileName, int lineNumber, int methodName, int cfgNumber, String left,
			String leftType, String containerType, String right, String rightType) {
		super(fileName, lineNumber, methodName, cfgNumber);
		this.left = left.intern();
		this.leftType = leftType.intern();
		this.containerType = containerType.intern();
		this.right = right.intern();
		this.rightType = rightType.intern();
	}

	
	public String printToString() {
		StringBuilder builder = new StringBuilder();
		builder.append(super.printToString()).append("\t").append(this.left).append("\t").append(this.leftType)
				.append("\t").append(this.containerType).append("\t").append(this.right).append("\t")
				.append(this.rightType);
		return builder.toString();
	}

	@Override
	public String getSchemeName() {
		// TODO Auto-generated method stub
		return "scalar-pairs";
	}
}
