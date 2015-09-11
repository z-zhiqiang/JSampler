package edu.uci.jsampler.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import edu.uci.jsampler.site.AbstractSite;

public class InfoPrinter {
	
	public static void printStaticInstrumentationInfo(List<AbstractSite> sitesInfo, File file){
		PrintWriter out = null;
		try{
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			//write the passing inputs
			out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			for(AbstractSite site: sitesInfo){
				out.println(site);
			}
			out.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			out.close();
		}
	}

}
