import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;

public class CrashClient {

	private GameWorld world;
	private JFrame jf;
	private Canvas _canvas;

	private Socket serv;
	private Player player;

	private final Object gLock = new Object();
	private volatile BufferStrategy currentStrat;
	private volatile Graphics2D currentGraphics;

	static int W = Resources.getW(), H = Resources.getH();

	long lostTime;

	public CrashClient() {
		System.out.println("Launching Crash client");
		jf = new JFrame();
		jf.add(_canvas = new Canvas());
		_canvas.setSize(Resources.getGameWidth(), Resources.getGameHeight());
		Runnable onResize = () -> {
			Insets i = jf.getInsets();
			Resources.setW(W = jf.getWidth() - i.left - i.right);
			Resources.setH(H = jf.getHeight() - i.top - i.bottom);
			synchronized (gLock) {
				_canvas.createBufferStrategy(2);
				currentStrat = _canvas.getBufferStrategy();
				currentGraphics = (Graphics2D) currentStrat.getDrawGraphics();
			}
		};
		jf.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent paramComponentEvent) {
				onResize.run();
			}

		});
		jf.pack();
		jf.setTitle("Client Viewer");
		jf.setVisible(true);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// jf.setLocation(800, 300);
		jf.setExtendedState(JFrame.MAXIMIZED_BOTH);

		_canvas.setFocusable(true);
		_canvas.requestFocus();
		// Ensure that _canvas is updated
		onResize.run();

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
				synchronized (player.strokes) {
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

		new Thread("ClientUpdate") {

			public void run() {
				try {
					while (true) {
						player.sendKeys();
						world.readWorld(player.cliIn);
					}
				} catch (IOException e1) {
					throw new RuntimeException(e1);
				}
			}
		}.start();

		int gameWidth = Resources.getGameWidth();
		int gameHeight = Resources.getGameHeight();
		while (true) {
			world.tick();
			player.act(world._world);
			synchronized (gLock) {
				world.draw(currentGraphics, gameWidth, gameHeight);
				currentStrat.show();
			}

			try {
				Thread.sleep(30); // will hang on sock read
			} catch (InterruptedException e) {
			}
		}
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			new Thread("Server") {

				public void run() {
					new CrashServer();
				}
			}.start();
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}
		new Thread("Client") {

			public void run() {
				new CrashClient();
			}
		}.start();
	}
}
