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
	}
	
	
	public static Collection<String> readCollection(File file) throws FileNotFoundException{ 
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
			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
//		finally{
//			if(reader != null){
//				try {
//					reader.close();
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
		
		return lines;
	}

}
