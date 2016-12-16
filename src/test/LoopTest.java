package test;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class LoopTest {
	public static void main(String[] args) throws Exception{
		for(int i = 0; i < 10; i++){
			for(int j = 0; j < 10; j++){
				System.out.println(i + j);
			}
		}
//		try{
//			readCollection(new File("a.file"));
//		}
//		catch (IOException e){
//			e.printStackTrace();
//		}
	}
	
	
	public static Collection<String> readCollection(File file) throws IOException{ 
		Collection<String> lines = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			String line;
			reader = new BufferedReader(new FileReader(file));
			while((line = reader.readLine()) != null){
				lines.add(line);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//			throw new IOException("IO");
		} 
		finally{
//			if(reader != null){
//				try {
//					reader.close();
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
			System.out.println("finally");
		}
		System.out.println("return lines");
		return lines;
	}
	
	
	private void syncFile() throws IOException {
		for (int i = 0;;) {
			// 3311: JVM sync call sometimes fails under high load against NFS
			// mounted disk. We re-try to do this 20 times.
			try {
				// raf.sync( false);
				System.out.println();

				// the sync succeed, so return
				break;
			} catch (Exception ioe) {
				i++;
				try {
					// wait for .2 of a second, hopefully I/O is done by now
					// we wait a max of 4 seconds before we give up
					Thread.sleep(200);
				} catch (InterruptedException ie) {
					// does not matter weather I get interrupted or not
				}

				if (i > 20) {
					throw new IOException();
				}
			}
		}
	}
//	
//	
//	private void syncFile2() throws IOException {
//		for (int i = 0;;) {
//			// 3311: JVM sync call sometimes fails under high load against NFS
//			// mounted disk. We re-try to do this 20 times.
//			try {
//				// raf.sync( false);
//				System.out.println();
//
//				// the sync succeed, so return
//				break;
//			} catch (Exception ioe) {
//				i++;
//				try {
//					// wait for .2 of a second, hopefully I/O is done by now
//					// we wait a max of 4 seconds before we give up
//					Thread.sleep(200);
//				} catch (InterruptedException ie) {
//					// does not matter weather I get interrupted or not
//				}
//
////				if (i > 20) {
////					throw new IOException();
////				}
//			}
//		}
//	}
//	
//	public void foo(int i, int j) {
//	    while (true) {
//	        try {
//	            while (i < j)
//	                i = j++ / i;
//	        } catch (RuntimeException re) {
//	            i = 10;
//	            continue;
//	        }
//	        break;
//	    }
//	}

}
