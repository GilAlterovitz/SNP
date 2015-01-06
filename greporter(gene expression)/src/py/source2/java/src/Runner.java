import java.io.File;
public class Runner {
	
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
			//String workpath = "I:\\files\\Gil project\\chinachen\\mouse2\\data\\STRING\\badge_0.005";
			String workpath = args[0];
			File inputdir=new File(workpath+"\\yedinput");
			//File inputdir_afterFilter=new File(workpath+"\\data\\STRING\\COGs\\yedinput");
			File outdir=new File(workpath+"\\network");
			File[] lists=inputdir.listFiles();
			//File[] lists_afterFilter=inputdir_afterFilter.listFiles();
			new YedRunner_STRnetwork(lists, outdir);
			new YedRunner_STRnetwork_Single(lists, outdir);
			new YedRunner_STRnetwork_black(lists, outdir);
			System.out.println("Done!!");
	}

}