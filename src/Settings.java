import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Settings {
	String _playerName = "default";
	String _serverIP = "184.187.175.50";
	
	public Settings() {
		load();
	}
	
	public void save() {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(AppDataManager.getAppDataFile("settings")));
			out.writeUTF(_playerName);
			out.writeUTF(_serverIP);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error: could not save settings file!");
		}
	}
	
	public void load() {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(AppDataManager.getAppDataFile("settings")));
			_playerName = in.readUTF();
			_serverIP = in.readUTF();
			in.close();
		} catch (IOException e) {
			System.out.println("Warning: could not load settings file; assuming default values");
		}
	}
}
