import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Settings {

	static String _playerName = "default";
	static String _serverIP = "184.187.175.50";
	static int _serverPort = 42973;
	
	// window dimensions
	static int _winX = 0, _winY = 0, _winW = 800, _winH = 600;

	static {
		load();
	}

	public static void save() {
		try (ObjectOutputStream out =
						new ObjectOutputStream(new FileOutputStream(
								AppDataManager.getAppDataFile("settings")))) {
			out.writeUTF(_playerName);
			out.writeUTF(_serverIP);
			out.writeInt(_serverPort);
			
			out.writeInt(_winX);
			out.writeInt(_winY);
			out.writeInt(_winW);
			out.writeInt(_winH);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error: could not save settings file!");
		}
	}

	public static void load() {
		try (ObjectInputStream in =
						new ObjectInputStream(new FileInputStream(
								AppDataManager.getAppDataFile("settings")))) {
			_playerName = in.readUTF();
			_serverIP = in.readUTF();
			_serverPort = in.readInt();
			
			_winX = in.readInt();
			_winY = in.readInt();
			_winW = in.readInt();
			_winH = in.readInt();
		} catch (IOException e) {
			System.out.println(
					"Warning: could not load settings file; assuming default values");
		}
	}
}
