import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.Insets;
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
	Canvas _canvas;

	Socket serv;
	Player player;

	static int W = Resources.W, H = Resources.H;
	
	long lostTime;

	public CrashClient() {
		jf = new JFrame();
		jf.add(_canvas = new Canvas());
		_canvas.setSize(W, H);
		jf.pack();
		jf.setTitle("Client Viewer");
		jf.setVisible(true);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		jf.setLocation(800, 300);
		jf.setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        Insets i = jf.getInsets();
        W = Resources.W = jf.getWidth() - i.left - i.right;
        H = Resources.H = jf.getHeight() - i.top - i.bottom;
		
		_canvas.setFocusable(true);
		_canvas.requestFocus();
		_canvas.createBufferStrategy(2);
		
		while (true) {
			// this will block until the user leaves the menu
			new Menu(_canvas);

			try {
				serv = new Socket(Settings._serverIP, Settings._serverPort);
				player = new Player(
						new DataOutputStream(serv.getOutputStream()),
						new DataInputStream(serv.getInputStream()),
						Settings._playerName);
				player.nameset = true;
				break;
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
		
		_canvas.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				player.keys[e.getKeyCode()] = true;
				synchronized(player.strokes){
					player.strokes.add(e.getKeyCode());
				}
			}

			public void keyReleased(KeyEvent e) {
				player.keys[e.getKeyCode()] = false;
			}
		});

		world = new GameWorld();
		try {
			world.readInit(player.cliIn, player);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		try {
			player.sendInit();
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
			BufferStrategy buff = _canvas.getBufferStrategy();
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
//				Thread.sleep(100);
//			}catch(Exception e){}
//		}
		new Thread("Client"){public void run(){
			new CrashClient();
		}}.start();
	}
}
