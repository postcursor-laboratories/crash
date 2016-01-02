import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.Stack;

@SuppressWarnings("serial")
public class Menu {
	// so that we can navigate between different screens
	private Stack<MenuEntry[]> _menuHistory;
	private int _selectionIndex = 0; // index into _currentEntries of currently highlighted menu item
	
	private MenuEntry[] _entries = {
			new MenuEntry("start game!", () -> {
				System.out.println("BOOP GAME START");
				// causes the main loop in Menu() to exit, and we return to the CrashClient constructor, which starts the game
				_shouldClose = true;
			}),
			new MenuEntry("settings", () -> { _menuHistory.push(new MenuEntry[]{
					new MenuEntry("setting 1", () -> System.out.println("setting uno!")),
					new MenuEntry("setting 2", () -> System.out.println("setting dos!")),
					new MenuEntry("setting 3", () -> System.out.println("setting tres!")),
					new MenuEntry("setting 4", () -> System.out.println("setting quatro!")),
					new MenuEntry("back", () -> { _menuHistory.pop(); _selectionIndex = 0; }),
			}); _selectionIndex = 0; }),
			new MenuEntry("quit", () -> System.exit(0)),
	};
	
	private boolean _shouldClose = false;
	
	public Menu(Canvas canvas) {
		_menuHistory = new Stack<MenuEntry[]>(){{ push(_entries); }};
		
		KeyListener listener;
		canvas.addKeyListener(listener = new KeyListener(){
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_S:
				case KeyEvent.VK_DOWN:
					_selectionIndex = (_selectionIndex+1) % _menuHistory.peek().length;
					break;
					
				case KeyEvent.VK_W:
				case KeyEvent.VK_UP:
					_selectionIndex = (_selectionIndex-1+_menuHistory.peek().length) % _menuHistory.peek().length;
					break;
				
				case KeyEvent.VK_ENTER:
				case KeyEvent.VK_SPACE:
					_menuHistory.peek()[_selectionIndex].tail.run();
					break;
				}
			}
			
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
		});
		
		BufferStrategy buff = canvas.getBufferStrategy();
		while (!_shouldClose) {
			Graphics2D g = (Graphics2D) buff.getDrawGraphics();
			
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, Resources.W, Resources.H);
			g.setColor(Color.BLACK);
			
			// TODO make this actually pretty
			g.setFont(Resources.getFont(100));
			g.drawString("CRASH", 100, 100);
			g.setFont(Resources.getFont(50));
			
			MenuEntry[] entries = _menuHistory.peek();
			if (entries != null) {
				for (int i = 0; i < entries.length; i++) {
					g.drawString((_selectionIndex == i ? "-> " : "") + entries[i].head, 100, 200+50*i);
				}
			}
			
			buff.show();
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
			}
		}
		
		// We should shut down now; remove all references so we can be garbage collected
		canvas.removeKeyListener(listener);
	}
	
	// trolololololollll
	private class MenuEntry extends Pair<String, Runnable>{
		public MenuEntry(String name, Runnable action){
			super(name, action);
		}
	}
}
