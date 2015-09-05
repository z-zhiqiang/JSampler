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

	public ScalarPairSite(String fileName, int lineNumber, String methodName,
					int cfgNumber, String left, String leftType, String containerType,
					String right, String rightType) {
				super(fileName, lineNumber, methodName, cfgNumber);
				this.left = left.intern();
				this.leftType = leftType.intern();
				this.containerType = containerType.intern();
				this.right = right.intern();
				this.rightType = rightType.intern();
			}

	@Override
	protected void toString(StringBuilder builder) {
		builder.append("{").append(this.left).append("[").append(this.leftType).append(",").append(this.containerType)
				.append("]").append(", ").append(this.right).append("[").append(rightType).append("]").append("}");
	}

	@Override
	public SiteCategory getCategory() {
		// TODO Auto-generated method stub
		return SiteCategory.SCALAR_PAIR;
	}
}
