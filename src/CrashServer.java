import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;

public class CrashServer {
	GameWorld world;
	private Leaderboard _leaderboard;
	JFrame jf;
	Canvas can;
	
	ServerSocket ss;
	Socket cli;
	
	Player player;

	static final int W = Resources.W, H = Resources.H;
	
	boolean[] keys = new boolean[1 << 16];

	public CrashServer() {
		jf = new JFrame();
		jf.add(can = new Canvas());
		can.setSize(W, H);
		jf.pack();
		jf.setTitle("Server Viewer");
		jf.setVisible(true);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		try {
			ss=new ServerSocket(42973);
			cli=ss.accept();
			
			DataOutputStream cliOut=new DataOutputStream(cli.getOutputStream());
			DataInputStream cliIn=new DataInputStream(cli.getInputStream());
			player = new Player(cliOut, cliIn);
			
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
		world.init();
		
		world.createPlayer(player);
		try {
			world.sendInit(player.cliOut, player);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		
		world.tick();
		
		try {
			_leaderboard = new Leaderboard("./leaderboard.txt", 10);
		} catch (IOException e2) {
			throw new RuntimeException("Couldn't open leaderboard!");
		}
		
		while(true){
			can.createBufferStrategy(2);
			BufferStrategy buff = can.getBufferStrategy();
			while (true) {
				Graphics2D g = (Graphics2D) buff.getDrawGraphics();
				world.draw(g, W, H);
				buff.show();
				
				try {
					world.sendWorld(player.cliOut);
				} catch (IOException e1) {
					throw new RuntimeException(e1);
				}
				
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
				}
				world.tick();
				player.act(world._world);
			}
		}
	}

//	public static void main(String[] args) {
//		new CrashServer();
//	}
}
