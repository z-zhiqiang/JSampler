package test;

import java.util.regex.Pattern;

public class test {

	public static void main(String[] args) {
		String pattern = "^a\\d+";
		Pattern p = Pattern.compile(pattern);
		System.out.println(p.matcher("bba123").find());
		
	}
	
}
