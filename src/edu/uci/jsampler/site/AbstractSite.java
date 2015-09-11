package edu.uci.jsampler.site;

public abstract class AbstractSite {
	final private String fileName;
	final private int lineNumber;
	final private String methodName;
	final private int cfgNumber;

	public AbstractSite(String fileName, int lineNumber, String methodName, int cfgNumber) {
		super();
		this.fileName = fileName.intern();
		this.lineNumber = lineNumber;
		this.methodName = methodName.intern();
		this.cfgNumber = cfgNumber;
	}

	public String getFileName() {
		return fileName;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public String getMethodName() {
		return methodName;
	}

	public int getCfgNumber() {
		return cfgNumber;
	}

	public String getFileString() {
		return "file = " + this.fileName;
	}

	public String toStringWithoutFile() {
		StringBuilder builder = new StringBuilder();

		this.toString(builder);
		builder.append(", line=").append(lineNumber).append(", method=").append(methodName).append(", cfg=")
				.append(this.cfgNumber);

		return builder.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		this.toString(builder);
		builder.append(", line=").append(lineNumber).append(", method=").append(methodName).append(", file=")
				.append(fileName).append(", cfg=").append(cfgNumber);

		return builder.toString();
	}

	protected abstract void toString(StringBuilder builder);
	
	public String printToString(){
		StringBuilder builder = new StringBuilder();
		builder.append(this.fileName).append("\t").append(this.lineNumber).append("\t").append(this.methodName).append("\t").append(this.cfgNumber);
		return builder.toString();
	}

	public abstract String getSchemeName();
}