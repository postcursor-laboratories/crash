import java.awt.Canvas;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JFrame;

public class CrashServer {
	GameWorld world;
	private Leaderboard _leaderboard;
	JFrame jf;
	Canvas can;
	
	ArrayList<Player> players = new ArrayList<>();

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
		jf.setExtendedState(Frame.MAXIMIZED_BOTH);

		world = new GameWorld();
		world.init();
		world.tick();

		new Thread("ServerAcceptThread") { public void run() {
			ServerSocket ss;
			try {
				ss = new ServerSocket();
				ss.setReuseAddress(true);
				ss.bind(new InetSocketAddress(Settings._serverPort));
			} catch (IOException e2) {
				e2.printStackTrace();
				return;
			}
			while (true) {
				try {
					Socket cli = ss.accept();

					DataOutputStream cliOut = new DataOutputStream(cli.getOutputStream());
					DataInputStream cliIn = new DataInputStream(cli.getInputStream());
					synchronized(players){
						Player player = new Player(cliOut, cliIn, "");
						world.createPlayer(player);
//						world.sendInit(player.cliOut, player);
	
						players.add(player);
					}

				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
		}}.start();

		can.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) { }
			public void keyPressed(KeyEvent e) {
				keys[e.getKeyCode()] = true;
			}
			public void keyReleased(KeyEvent e) {
				keys[e.getKeyCode()] = false;
			}
		});
		
		can.setFocusable(true);
		can.requestFocus();
		
		try {
			_leaderboard = new Leaderboard("./leaderboard.txt", 10);
		} catch (IOException e2) {
			throw new RuntimeException("Couldn't open leaderboard!");
		}

		can.createBufferStrategy(2);
//		BufferStrategy buff = can.getBufferStrategy();
		while(true){
//			Graphics2D g = (Graphics2D) buff.getDrawGraphics();
//			world.draw(g, W, H);
//			buff.show();
			
			long sendTime = 0;
			long actTime = 0;
			long totalTime = 0;
			
			synchronized(players){
				
				totalTime -= System.currentTimeMillis();
				ArrayList<Player> playersToRemove = new ArrayList<>();
				
				for (Player player : players) {
					try {
						
						sendTime -= System.currentTimeMillis();
						if(!player.initialized){
							world.sendInit(player.cliOut, player);
						}else if(!player.nameset){
							player.recvInit();
						}
						else {
							world.sendWorld(player.cliOut);
						}
						sendTime += System.currentTimeMillis();
						
						actTime -= System.currentTimeMillis();
						player.recvKeys();
						player.act(world._world);
						actTime += System.currentTimeMillis();
						
					} catch (Exception e1) {
						playersToRemove.add(player);
//						throw new RuntimeException(e1);
						sendTime += System.currentTimeMillis();
					}
				}
				players.removeAll(playersToRemove);
				
				world.tick();

				for (Player player : playersToRemove) {
					player.markDead(world);
				}
				totalTime += System.currentTimeMillis();
				
				//System.out.println(sendTime+", "+actTime+", "+(totalTime-sendTime-actTime));
			}
			
			try {
				if(totalTime <= 29)
					Thread.sleep(30 - totalTime);
			} catch (InterruptedException e1) {}
		}
	}

	public static void main(String[] args) {
		new CrashServer();
	}
}
