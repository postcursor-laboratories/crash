import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

public class Player {
	Body b;
	DataOutputStream cliOut;
	DataInputStream cliIn;
	boolean[] keys = new boolean[1 << 16];
	
	ArrayList<Integer> strokes = new ArrayList<>();
	boolean initialized = false;
	
	public Player(DataOutputStream out, DataInputStream in){
		cliOut = out;
		cliIn = in;
	}
	
	void act(World world){
		if(keys[KeyEvent.VK_W]){
			b.applyForce(new Vec2(0,30), b.getWorldCenter());
		}
		if(keys[KeyEvent.VK_A]){
			b.applyForce(new Vec2(-20,0), b.getWorldCenter());
		}
		if(keys[KeyEvent.VK_D]){
			b.applyForce(new Vec2(20,0), b.getWorldCenter());
		}
	}

	void sendKeys() throws IOException{
		//First send all the keys currently down, followed by -1 (to indicate end of list)
		for(int i=0; i<(1<<16); i++){
			if(keys[i]){
				cliOut.writeInt(i);
			}
		}
		cliOut.writeInt(-1);
		
//		synchronized(strokes){
//			//Next send all of the keystrokes received
//			cliOut.writeInt(strokes.size());
//			for(Integer i : strokes)
//				cliOut.writeInt(i);
//	
//			strokes.clear();
//		}
	}
	
	void recvKeys() throws IOException{
		while(cliIn.available() >= 4){
			int loc=0;
			int nextDown;
			while( (nextDown = cliIn.readInt()) != -1){
				for( ; loc<nextDown; loc++)
					keys[loc] = false;
				keys[nextDown] = true;
				loc++;
			}
			while(loc<(1<<16))
				keys[loc++]=false;
			
//			strokes.clear();
//			
//			int numStrokes = cliIn.readInt();
//			for(int i=0; i<numStrokes; i++){
//				strokes.add(cliIn.readInt());
//			}
		}
	}

	void markDead(GameWorld world) {
		world.deadBodies.add(b);
		world.newDeadBodies.add(b);
	}
	
	public Body getBody()
	{
		return this.b;
	}
}
