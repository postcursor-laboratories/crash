import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

import protos.KeyProto.Key;
import protos.KeyProto.Keys;

public class Player {
    private static final int KEY_START = 0x1234;
    private static final int KEY_END = 0x4321;
    
	Body b;
	DataOutputStream cliOut;
	DataInputStream cliIn;
	boolean[] keys = new boolean[1 << 16];
	private final ByteArrayOutputStream recorder = new ByteArrayOutputStream(128);
	
	ArrayList<Integer> strokes = new ArrayList<>();
	boolean initialized = false;
	
	String name;
	boolean nameset = false;
	
	public Player(DataOutputStream out, DataInputStream in, String newName){
		cliOut = out;
		cliIn = in;
		name = newName;
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
	    clientSideKeyProcessing();
		//First send all the keys currently down
	    Keys.Builder keyData = Keys.newBuilder();
		for(int i=0; i<(1<<16); i++){
			if(keys[i]){
			    keyData.addKey(Key.newBuilder().setKeyCode(i));
			}
		}
		// TODO better packets lol
        cliOut.writeInt(KEY_START);
		for (byte b : keyData.build().toByteArray()) {
		    cliOut.writeInt(b);
		}
        cliOut.writeInt(KEY_END);
		
//		synchronized(strokes){
//			//Next send all of the keystrokes received
//			cliOut.writeInt(strokes.size());
//			for(Integer i : strokes)
//				cliOut.writeInt(i);
//	
//			strokes.clear();
//		}
	}
	
	/**
	 * Process client-side bindings.
	 */
	private void clientSideKeyProcessing() {
        if(keys[KeyEvent.VK_K]){
            System.exit(0);
        }
    }

    void sendInit() throws IOException{
		//Currently just send the player's name.
		cliOut.writeChars(this.name);
		cliOut.writeChar('\00');
	}
	
	void recvInit() throws IOException{
		char in;
		while((in = cliIn.readChar()) != '\00')
		{
			this.name += in;
		}
		this.nameset = true;
		System.out.println("Name Recieved: "+this.name);
	}
	
	void recvKeys() throws IOException{
		while(cliIn.available() >= 4){
		    if (cliIn.readInt() != KEY_START) {
		        // ignore random bytes on stream
		        continue;
		    }
		    recorder.reset();
		    int read = -1;
		    while ((read = cliIn.readInt()) != KEY_END) {
		        recorder.write(read);
		    }
		    Keys keyData = Keys.parseFrom(recorder.toByteArray());
		    int loc = 0;
		    for (Key key : keyData.getKeyList()) {
		        int nextDown = key.getKeyCode();
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
}
