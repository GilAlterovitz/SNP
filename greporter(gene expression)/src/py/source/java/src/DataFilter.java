/*
 * Author: Peijin Zhang
 * 
 * File filter for txt and csv
 * 
 */

import java.io.File;
import javax.swing.filechooser.*;

public class DataFilter extends FileFilter 
{
    public boolean accept(File f) 
    {
    	if(f.isDirectory())
    		return true;
    	String extension = null;
    	String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 &&  i < s.length() - 1)
            extension = s.substring(i+1).toLowerCase();
        if (extension != null) 
        {
            if(extension.equals("txt")) 
                return true;
            else if(extension.equals("csv"))
            	return true;
            else
                return false;
        }
        return false;
    }

    public String getDescription() 
    {
        return "Edge Lists (.txt/.csv)";
    }
}
