import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.jbox2d.dynamics.Body;

public class Player {
	Body b;
	DataOutputStream cliOut;
	DataInputStream cliIn;
	boolean[] keys = new boolean[1 << 16];
	
	ArrayList<Integer> strokes = new ArrayList<>();
	
	public Player(DataOutputStream out, DataInputStream in){
		cliOut = out;
		cliIn = in;
	}

	void sendKeys() throws IOException{
		//First send all the keys currently down, followed by -1 (to indicate end of list)
		for(int i=0; i<(1<<16); i++){
			if(keys[i]){
				cliOut.writeInt(i);
			}
		}
		cliOut.writeInt(-1);
		
		//Next send all of the keystrokes received
		cliOut.writeInt(strokes.size());
		for(Integer i : strokes)
			cliOut.writeInt(i);
	
		strokes.clear();
	}
	
	void recvKeys() throws IOException{
		int loc=0;
		int nextDown;
		while( (nextDown = cliIn.readInt()) != -1){
			for(int i=loc; i<nextDown; i++)
				keys[i] = false;
			keys[nextDown] = true;
		}
		
		strokes.clear();
		
		int numStrokes = cliIn.readInt();
		for(int i=0; i<numStrokes; i++){
			strokes.add(cliIn.readInt());
		}
	}
}
