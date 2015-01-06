/*
 * Author: Peijin Zhang
 * 
 * Runner for Yed
 * 
 */

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;

public class YedRunner_STRnetwork
{
	
	public YedRunner_STRnetwork(File[] inputfiles, File outputdirectory) throws Exception
	{
		for(int x = 0; x < inputfiles.length; x++)
		{
			//generate the graphs
			System.out.println(inputfiles[x]);
			YedGraph_STRnetwork temp = new YedGraph_STRnetwork(inputfiles[x]);
			
			// format jpg: 2
			String outputpath = outputdirectory+FormatFolder(2);
			File savepath = new File(outputpath);
			if (!savepath.exists()) {
				savepath.mkdir();
			}
			save(temp, new File(outputpath, filename(inputfiles[x].getName(), 2)), 2);
		}
	}
	
	
	public void save(YedGraph_STRnetwork graph, File file, int index)
	{
		switch(index)
		{
			case 0:		graph.saveGraphML(file); break;
			case 1:		graph.saveYGF(file); break;
			case 2:		graph.saveJPG(file); break;
			case 3:		graph.saveSVG(file); break;
			case 4:		graph.savePDF(file); break;
			case 5:		graph.saveSWF(file); break;
			case 6:		graph.saveEMF(file); break;
		}
	}
	
	public static String filename(String name, int index)
	{
		String prefix = name.substring(0, name.lastIndexOf("-"));
		String newfnm = prefix+"_complete";
		switch(index)
		{
			case 0:		newfnm += ".graphml"; break;
			case 1:		newfnm += ".ygf"; break;
			case 2:		newfnm += ".jpg";break;
			case 3:		newfnm += ".svg"; break;
			case 4:		newfnm += ".pdf"; break;
			case 5:		newfnm += ".swf"; break;
			case 6:		newfnm += ".emf";
		}
		return newfnm;
	}
	
	private String FormatFolder(int index){
		String subfolder = "\\graphml";;
		switch(index)
		{
			case 0:		subfolder= "\\graphml"; break;
			case 1:		subfolder= "\\ygf"; break;
			case 2:		subfolder= "\\jpg";break;
			case 3:		subfolder= "\\svg"; break;
			case 4:		subfolder= "\\pdf"; break;
			case 5:		subfolder= "\\swf"; break;
			case 6:		subfolder= "\\emf";
		}
		return subfolder+"\\complete";	
	}
}
