import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.Stack;
import java.util.function.Supplier;

public class Menu {
	// so that we can navigate between different screens
	private Stack<MenuEntry[]> _menuHistory;
	private int _selectionIndex = 0; // index into _currentEntries of currently highlighted menu item
	
	private boolean _enteringText = false; // if set to true, keyPressed simply appends text to _textField
	private String _textField = "";
	private Runnable _toRunWhenTextEntered = null;
	
	private MenuEntry[] _entries = {
			new MenuEntry("start game!", () -> {
				System.out.println("BOOP GAME START");
				
				// change "start game!" to connecting text; this just makes it more obvious when the server is down
				// TODO handle server connection issues and client lobby intelligently
				_menuHistory.peek()[0] = new MenuEntry("[connecting....]", () -> {});
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {}

				// causes the main loop in Menu() to exit, and we return to the CrashClient constructor, which starts the game
				_shouldClose = true;
			}),
			
			new MenuEntry("settings", () -> {
				_menuHistory.push(new MenuEntry[]{
					new MenuEntry(() -> "player name: "+Settings._playerName, () -> {
						_textField = Settings._playerName;
						_toRunWhenTextEntered = () -> Settings._playerName = _textField;
						_enteringText = true;
					}),
					new MenuEntry(() -> "server IP: "+Settings._serverIP, () -> {
						_textField = Settings._serverIP;
						_toRunWhenTextEntered = () -> Settings._serverIP = _textField;
						_enteringText = true;
					}),
                    new MenuEntry(() -> "server port: "+Settings._serverPort, () -> {
                         _textField = Settings._serverPort + "";
                         _toRunWhenTextEntered = () -> Settings._serverPort = Integer.parseInt(_textField); // psh, error checking is for fools
                         _enteringText = true;
                    }),
					new MenuEntry("setting 4", () -> System.out.println("setting quatro!")),
					new MenuEntry("back", () -> {
						_menuHistory.pop();
						_selectionIndex = 0;
						Settings.save();
					}),
				});
				_selectionIndex = 0;
			}),
			
			new MenuEntry("quit", () -> System.exit(0)),
	};
	
	private boolean _shouldClose = false;
	
	@SuppressWarnings("serial")
	public Menu(Canvas canvas) {
		_menuHistory = new Stack<MenuEntry[]>(){{ push(_entries); }};
		
		KeyListener listener;
		canvas.addKeyListener(listener = new KeyListener(){
			public void keyPressed(KeyEvent e) {
				if (!_enteringText) {
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
						_menuHistory.peek()[_selectionIndex].tail.run();
						break;
					}
				} else {
					if(e.getKeyCode() == KeyEvent.VK_ENTER){
						_enteringText = false;

	                    if (_toRunWhenTextEntered != null)
	                        _toRunWhenTextEntered.run();
					}
				}
			}
			
			public void keyTyped(KeyEvent e) {
				if (_enteringText) {
					switch (e.getKeyChar()) {
					case '\n':
						// do nothing; leaving the entry field is handled in keyPressed
						break;
					case '\b':
						if (_textField.length() > 0)
							_textField = _textField.substring(0,_textField.length()-1);
						break;
					default:
						// because I know you folks and you are going to mess with this
						if (_textField.length() < 64)
							_textField += e.getKeyChar();
					}
				}
			}
			
			public void keyReleased(KeyEvent e) {}
		});
		
		BufferStrategy buff = canvas.getBufferStrategy();
		while (!_shouldClose) {
			Graphics2D g = (Graphics2D) buff.getDrawGraphics();
			
			g.addRenderingHints(new RenderingHints(
		             RenderingHints.KEY_TEXT_ANTIALIASING,
		             RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
		    g.addRenderingHints(new RenderingHints(
		             RenderingHints.KEY_ANTIALIASING,
		             RenderingHints.VALUE_ANTIALIAS_ON));
		    g.addRenderingHints(new RenderingHints(
		             RenderingHints.KEY_RENDERING,
		             RenderingHints.VALUE_RENDER_QUALITY));
		    g.addRenderingHints(new RenderingHints(
		             RenderingHints.KEY_INTERPOLATION,
		             RenderingHints.VALUE_INTERPOLATION_BICUBIC));
		    
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, Resources.W, Resources.H);
			g.setColor(Color.BLACK);
			
			// TODO make this actually pretty
			g.setFont(Resources.getFont(100));
			g.drawString("CRASH", 100, 100);
			g.setFont(Resources.getFont(50));
			
			MenuEntry[] entries = _menuHistory.peek();
			for (int i = 0; i < entries.length; i++) {
				g.drawString((_selectionIndex == i ? "-> " : "") + entries[i].head.get()+
						(_enteringText && _selectionIndex == i?"_":""), 100, 200+50*i);
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
	private class MenuEntry extends Pair<Supplier<String>, Runnable>{
		public MenuEntry(String name, Runnable action){
			this(() -> name, action);
		}
		
		public MenuEntry(Supplier<String> name, Runnable action){
			super(name, action);
		}
	}
}
