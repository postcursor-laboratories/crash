import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class CrashServer {
	private GameWorld world;
	private Leaderboard _leaderboard;

	private ArrayList<Player> players = new ArrayList<>();

	public CrashServer() {
		System.out.println("Launching Crash server");

		world = new GameWorld();
		world.init();
		world.tick();

		new Thread("ServerAcceptThread") {
			public void run() {
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
			}
		}.start();
		
		try {
			_leaderboard = new Leaderboard("./leaderboard.txt", 10);
		} catch (IOException e2) {
			throw new RuntimeException("Couldn't open leaderboard!");
		}

		while(true){
			long totalTime = 0;
			
			synchronized(players){
				totalTime -= System.currentTimeMillis();
				ArrayList<Player> playersToRemove = new ArrayList<>();
				
				for (Player player : players) {
					try {
						if (!player.initialized) {
							world.sendInit(player.cliOut, player);
						} else if (!player.nameset) {
							player.recvInit();
						} else {
							world.sendWorld(player.cliOut);
						}
						
						player.recvKeys();
						player.act(world._world);
					} catch (Exception e1) {
						playersToRemove.add(player);
					}
				}
				players.removeAll(playersToRemove);
				
				world.tick();

				for (Player player : playersToRemove) {
					player.markDead(world);
				}
				totalTime += System.currentTimeMillis();
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
