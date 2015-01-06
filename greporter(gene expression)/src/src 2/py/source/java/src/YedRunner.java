/*
 * Author: Peijin Zhang
 * 
 * Runner for Yed
 * 
 */

import java.io.File;

public class YedRunner
{
	
	// Multiple Graph Export
	
	public YedRunner(File[] inputfiles, File outputdirectory) throws Exception
	{
		for(int x = 0; x < inputfiles.length; x++)
		{
			YedGraph temp = new YedGraph(inputfiles[x]);
			for (int y=0; y<7; y++)
			{
				String outputpath = outputdirectory+FormatFolder(y);
				//save the files
				File savepath = new File(outputpath);
				if (!savepath.exists()) {
					savepath.mkdir();
				}
				save(temp, new File(outputpath, filename(inputfiles[x].getName(), y)), y);
			}
		}
	}
	
	public void save(YedGraph graph, File file, int index)
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
		String temp = name.substring(0, name.lastIndexOf("."));
		switch(index)
		{
			case 0:		temp += ".graphml"; break;
			case 1:		temp += ".ygf"; break;
			case 2:		temp += ".jpg";break;
			case 3:		temp += ".svg"; break;
			case 4:		temp += ".pdf"; break;
			case 5:		temp += ".swf"; break;
			case 6:		temp += ".emf";
		}
		return temp;
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
		return subfolder;	
	}
	//Single graph edit
	
	public YedRunner(File inputfile) throws Exception
	{
		
		YedGraph temp = new YedGraph(inputfile);
		temp.start("Yed Graph");
	}
}
