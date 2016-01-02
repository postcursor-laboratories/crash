import java.awt.Canvas;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;

public class CrashClient {
	GameWorld world;
	JFrame jf;
	Canvas can;

	Socket serv;
	Player player;

	static final int W = Resources.W, H = Resources.H;
	
	long lostTime;

	public CrashClient() {
		jf = new JFrame();
		jf.add(can = new Canvas());
		can.setSize(W, H);
		jf.pack();
		jf.setTitle("Client Viewer");
		jf.setVisible(true);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		jf.setLocation(800, 300);
		jf.setExtendedState(Frame.MAXIMIZED_BOTH);

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
				System.out.println(e.getKeyChar());
				player.keys[e.getKeyCode()] = true;
				synchronized(player.strokes){
					player.strokes.add(e.getKeyCode());
				}
			}

			public void keyReleased(KeyEvent e) {
				player.keys[e.getKeyCode()] = false;
			}
		});
		
		can.setFocusable(true);
		can.requestFocus();

		world = new GameWorld();
		try {
			world.readInit(player.cliIn, player);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		
		new Thread("ClientUpdate"){
			public void run(){
				try {
					while(true){
						player.sendKeys();
						world.readWorld(player.cliIn);
					} 
				} catch (IOException e1) {
					throw new RuntimeException(e1);
				}
			}
		}.start();
		
		while(true){
			can.createBufferStrategy(2);
			BufferStrategy buff = can.getBufferStrategy();
			while (true) {
				world.tick();
				player.act(world._world);
				
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
//		if(args.length > 0){
//			new Thread("Server"){public void run(){
//				new CrashServer();
//			}}.start();
//			try{
//				Thread.sleep(1000);
//			}catch(Exception e){}
//		}
		new Thread("Client1"){public void run(){
			new CrashClient();
		}}.start();
//		try{
//			Thread.sleep(2000);
//		}catch(Exception e){}
//		new Thread("Client2"){public void run(){
//			new CrashClient();
//		}}.start();
	}
}
