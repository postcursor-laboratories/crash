import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.jbox2d.collision.WorldManifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.ContactEdge;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

public class CrashClient {
	GameWorld world;
	JFrame jf;
	Canvas can;

	Socket serv;
	Player player;

	static final int W = Resources.W, H = Resources.H;

	boolean[] keys = new boolean[1 << 16];
	
	long lostTime;

	public CrashClient() {
		jf = new JFrame();
		jf.add(can = new Canvas());
		can.setSize(W, H);
		jf.pack();
		jf.setTitle("Client Viewer");
		jf.setVisible(true);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setLocation(800, 300);

		try {
			serv=new Socket("localhost",42973);
			player = new Player(
					new DataOutputStream(serv.getOutputStream()),
					new DataInputStream(serv.getInputStream())
							);
		} catch (IOException e2) {
			e2.printStackTrace();
			return;
		}

		can.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				keys[e.getKeyCode()] = true;
			}

			public void keyReleased(KeyEvent e) {
				keys[e.getKeyCode()] = false;
			}
		});
		can.setFocusable(true);
		can.requestFocus();

		world = new GameWorld();
		try {
			world.readWorld(player.cliIn);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		
		new Thread("ClientUpdate"){
			public void run(){
				try {
					while(true){
						world.readWorld(player.cliIn);
						player.sendKeys();
					} 
				} catch (IOException e1) {
					throw new RuntimeException(e1);
				}
			}
		}.start();
		
		bigLoop:
		while(true){
			can.createBufferStrategy(2);
			BufferStrategy buff = can.getBufferStrategy();
			while (true) {
				world.tick();
				
				Graphics2D g = (Graphics2D) buff.getDrawGraphics();
				world.draw(g, W, H);
				buff.show();
				
				try {
					Thread.sleep(30); //will hang on sock read
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public static void main(String[] args) {
		if(args.length > 0){
			new Thread("Server"){public void run(){
				new CrashServer();
			}}.start();
		}
		new CrashClient();
	}
}
