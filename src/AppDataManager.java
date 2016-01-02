import java.io.File;

public class AppDataManager{
	private static String OS = System.getProperty("os.name").toLowerCase();
	static String appName="Crash";
	
	static{
		File f=new File(dataFolderPrefix(appName));
		System.out.println("AppData[Folder="+f+",Exists="+f.exists()+",Created="+f.mkdir()+"]");
	}
	
	static String dataFolderPrefix(final String applicationName) {
		if(OS.contains("win")){
			if(System.getenv("APPDATA")!=null)
				return System.getenv("APPDATA")+"/"+applicationName+"/";
			if(OS.contains("xp"))
				return System.getProperty("user.home") + "/Local Settings/Application Data/"+applicationName+"/";
			else
				return System.getProperty("user.home") + "/AppData/Roaming/"+applicationName+"/";
		} else if(OS.contains("nix") || OS.contains("nux")){
			return System.getProperty("user.home")+"/."+applicationName+"/";
		} else if(OS.contains("mac")) {
			return System.getProperty("user.home")+"/Library/Application Support/"+applicationName+"/";
		} else {
			throw new RuntimeException("OS NOT RECOGNIZED, WTF IS THIS?");
		}
	}
	
	static File getAppDataFile(String name){
		return new File(dataFolderPrefix(appName)+name);
	}
}
