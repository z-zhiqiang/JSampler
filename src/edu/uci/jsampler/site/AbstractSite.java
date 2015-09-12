package edu.uci.jsampler.site;

import edu.uci.jsampler.util.Translator;

public abstract class AbstractSite {
	final private int fileName;
	final private int lineNumber;
	final private int methodName;
	final private int cfgNumber;

	public AbstractSite(int fileName, int lineNumber, int methodName, int cfgNumber) {
		super();
		this.fileName = fileName;
		this.lineNumber = lineNumber;
		this.methodName = methodName;
		this.cfgNumber = cfgNumber;
	}

	public int getFileName() {
		return fileName;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getMethodName() {
		return methodName;
	}

	public int getCfgNumber() {
		return cfgNumber;
	}

	public String getFileString() {
		return "file = " + this.fileName;
	}

	
	public String printToString(){
		Translator translator = Translator.getInstance();
		StringBuilder builder = new StringBuilder();
		builder.append(translator.getString(this.fileName)).append("\t").append(this.lineNumber).append("\t").append(translator.getString(this.methodName)).append("\t").append(this.cfgNumber);
		return builder.toString();
	}

	public abstract String getSchemeName();
}