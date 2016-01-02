import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;

public class Leaderboard {
	private final ArrayList<Pair<String, Integer>> _scores = new ArrayList<Pair<String, Integer>>();
	private final int NUM_ENTRIES;
	private File _saveFile;
	
	public Leaderboard(String saveFilePath, int numEntries) throws IOException {
		NUM_ENTRIES = numEntries;
		_saveFile = new File(saveFilePath);
		if (_saveFile.exists()) {
			try(BufferedReader in = new BufferedReader(new FileReader(_saveFile))) {
				String line;
				while ((line = in.readLine()) != null) {
					if (line.isEmpty())
						continue;
					if (line.charAt(0) == '#')
						continue;
					
					try {
						String name = line.substring(0,line.indexOf(':'));
						int score = Integer.parseInt(line.substring(line.indexOf(':')+2)); // +2 for ": "
						
						_scores.add(new Pair<String,Integer>(name, score));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			_saveFile.createNewFile();
		}
		
		sort();
	}
	
	private void sort() {
		Collections.sort(_scores, new Comparator<Pair<String,Integer>>(){
			public int compare(Pair<String,Integer> o1, Pair<String,Integer> o2) {
				return o1.tail-o2.tail;
			}
		});
	}
	
	public void save() throws IOException {
		try(BufferedWriter out = new BufferedWriter(new FileWriter(_saveFile))) {
			for (Pair<String,Integer> p : _scores) {
				out.write(p.head+": "+p.tail);
				out.newLine();
			}
		}
	}
	
	public void addScore(String name, int score) {
		if (_scores.size() < NUM_ENTRIES) {
			_scores.add(new Pair<String,Integer>(name,score));
			sort();
		} else if (_scores.get(0).tail < score){
			_scores.get(0).head = name;
			_scores.get(0).tail = score;
		}
	}
	
	public ArrayList<Pair<String,Integer>> getScoreList() {
		return new ArrayList<Pair<String,Integer>>(_scores);
	}
}
