import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

import com.google.protobuf.MessageLite;

import protos.HandshakeProto.PlayerData;
import protos.KeyProto.Keys;

public class Player {
    private static final int PACKET_START = 0x1234;
    private static final int PACKET_END = 0x4321;
    
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
	
	void writeData(MessageLite data) throws IOException {
        cliOut.writeInt(PACKET_START);
        for (byte b : data.toByteArray()) {
            cliOut.writeInt(b);
        }
        cliOut.writeInt(PACKET_END);
	}
	
	byte[] readData() throws IOException {
        if (cliIn.readInt() != PACKET_START) {
            // ignore random bytes on stream
            return null;
        }
        recorder.reset();
        int read = -1;
        while ((read = cliIn.readInt()) != PACKET_END) {
            recorder.write(read);
        }
        return recorder.toByteArray();
	}
	
	void act(World world){
		if(keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP]){
			b.applyForce(new Vec2(0,30), b.getWorldCenter());
		}
		if(keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT]){
			b.applyForce(new Vec2(-20,0), b.getWorldCenter());
		}
		if(keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT]){
			b.applyForce(new Vec2(20,0), b.getWorldCenter());
		}
	}

	void sendKeys() throws IOException{
	    clientSideKeyProcessing();
		//First send all the keys currently down
	    Keys.Builder keyData = Keys.newBuilder();
		for(int i=0; i<(1<<16); i++){
			if(keys[i]){
			    keyData.addKey(i);
			}
		}
		// TODO better packets lol
        writeData(keyData.build());
		
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
        PlayerData data = PlayerData.newBuilder().setName(name).build();
        writeData(data);
	}
	
	void recvInit() throws IOException{
	    this.name = PlayerData.parseFrom(readData()).getName();
		this.nameset = true;
		System.out.println("Name Received: "+this.name);
	}
	
	void recvKeys() throws IOException{
		while(cliIn.available() >= 4){
		    Keys keyData = Keys.parseFrom(readData());
		    int loc = 0;
		    for (int nextDown : keyData.getKeyList()) {
                for( ; loc<nextDown && loc < keys.length; loc++)
                    keys[loc] = false;
                keys[nextDown] = true;
                loc++;
		    }
			while(loc < keys.length)
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
