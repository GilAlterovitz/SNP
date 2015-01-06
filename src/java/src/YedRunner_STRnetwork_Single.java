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

public class YedRunner_STRnetwork_Single
{

	public YedRunner_STRnetwork_Single(File[] inputfiles, File outputdirectory) throws Exception
	{
		int allopts[]={0, 1, 2, 4, 5, 6, 7};
		for(int x = 0; x < inputfiles.length; x++)
		{
			//generate the graphs
			System.out.println(inputfiles[x]);
			for (int y = 0; y < allopts.length; y++)
			{
				YedGraph_STRnetwork_Single temp = new YedGraph_STRnetwork_Single(inputfiles[x], allopts[y]);

				// format jpg: 2
				String outputpath = outputdirectory+FormatFolder(2, allopts[y]);
				File savepath = new File(outputpath);
				if (!savepath.exists()) {
					savepath.mkdir();
				}
				save(temp, new File(outputpath, filename(inputfiles[x].getName(), 2, allopts[y])), 2);

			}
		}
	}


	public void save(YedGraph_STRnetwork_Single graph, File file, int index)
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

	public static String filename(String name, int index, int opt)
	{
		String prefix = name.substring(0, name.lastIndexOf("-"));
		String option = "_neighborhood";
		String format = ".jpg";
		switch(opt)
		{
			case 0:		option = "_neighborhood"; break;
			case 1:		option = "_genefusion"; break;
			case 2:		option = "_cooccurrence";break;
			case 4:		option = "_coexpression"; break;
			case 5:		option = "_experiments"; break;
			case 6:		option = "_databases"; break;
			case 7:		option = "_textmining";
		}
		switch(index)
		{
			case 0:		format = ".graphml"; break;
			case 1:		format = ".ygf"; break;
			case 2:		format = ".jpg";break;
			case 3:		format = ".svg"; break;
			case 4:		format = ".pdf"; break;
			case 5:		format = ".swf"; break;
			case 6:		format = ".emf";
		}
		return prefix+option+format;
	}

	private String FormatFolder(int index, int opt){
		String subfolder = "\\jpg";
		String option = "neighborhood";
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
		switch(opt)
		{
			case 0:		option = "neighborhood"; break;
			case 1:		option = "genefusion"; break;
			case 2:		option = "cooccurrence";break;
			case 4:		option = "coexpression"; break;
			case 5:		option = "experiments"; break;
			case 6:		option = "databases"; break;
			case 7:		option = "textmining";
		}
		return subfolder+"\\"+option;
	}
}
