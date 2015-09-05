package test;

import java.util.regex.Pattern;

public class test {

	public static void main(String[] args) {
		String pattern = "^a\\d+";
		Pattern p = Pattern.compile(pattern);
		System.out.println(p.matcher("bba123").find());
		
		
		boolean a = true;
		byte b = 0;
//		int i = 8;
		char c = (char) -3;
		float f = 0.4f;
		double d = 0.4d;
		System.out.println((double) f);
		
		
		byte by = 0;
		for(int i = 0; i < 128; i++){
			by++;
		}
		System.out.println(by);
	}
	
}
