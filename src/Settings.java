import java.io.Serializable;

public class Settings implements Serializable {
	private static final long serialVersionUID = 1L;
	
	String _playerName = "default";
	String _serverIP = "184.187.175.50";
	
	// TODO write serializable stuff
	// it should be read from/written to AppDataManager.getAppDataFile("settings")
}
