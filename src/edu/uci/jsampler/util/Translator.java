package edu.uci.jsampler.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Translator {
	
	private static final Translator translatorInstance = new Translator();
	
	private final Map<String, Integer> stoimap;
	
	private final List<String> itoslist;
	
	private Translator(){
		this.stoimap = new HashMap<String, Integer>();
		this.itoslist = new ArrayList<String>();
	}
	
	public static Translator getInstance(){
		return translatorInstance;
	}
	
	public int getInteger(String s){
		if(!this.stoimap.containsKey(s)){
			this.stoimap.put(s, this.itoslist.size());
			this.itoslist.add(s);
		}
		return this.stoimap.get(s);
	}
	
	public String getString(int i){
		return this.itoslist.get(i);
	}

	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append(this.stoimap.toString()).append("\n\n");
		builder.append(this.itoslist).append("\n\n");
		return builder.toString();
	}
}
