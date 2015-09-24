package test;

import java.util.Map;
import java.util.regex.Pattern;

import soot.toolkits.scalar.InitAnalysis;

public class test {

	public static void main(String[] args) {
//		String pattern = "-sampler-out-sites=.*";
//		Pattern p = Pattern.compile(pattern);
//		System.out.println(p.matcher("-sampler-out-sites=/home/icuzzq/Workspace/program/JSampler/test").find());
//		
//		
//		boolean a = true;
//		byte b = 0;
////		int i = 8;
//		char c = (char) -3;
//		float f = 0.4f;
//		double d = 0.4d;
//		System.out.println((double) f);
//		
//		
//		byte by = 0;
//		for(int i = 0; i < 128; i++){
//			by++;
//		}
//		System.out.println(by);
//		
//		
//		boolean bb = false;
//		bb = 1<2;
//		System.out.println(bb);
//		
//		
//		long ll = 4;
//		double dd = ll;
//		System.out.println();
//		
//		int ii = 0;
//		System.out.println(ii);
		
		int[] array = {1, 2, 3, 4};
		int[][] aarray = new int[4][];
//		InitAnalysis init = new InitAnalysis()
		for(int i = 0; i < 4; i++){
			int[] aaa = new int[array[i]];
			aarray[i] = aaa;
		}
		
		for(int i = 0; i < aarray.length; i++){
			for(int j = 0; j < aarray[i].length; j++){
				System.out.print(aarray[i][j] + "\t");
			}
			System.out.println();
		}
		
		
		Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            System.out.format("%s=%s%n",
                              envName,
                              env.get(envName));
        }
	}
	
}
