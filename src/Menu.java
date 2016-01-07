import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Menu {

	/**
	 * so that we can navigate between different screens
	 */
	private Stack<MenuEntry[]> _menuHistory;
	/**
	 * index into _currentEntries of currently highlighted menu item
	 */
	private int _selectionIndex = 0;
	/**
	 * if set to true, keyPressed simply appends text to current editable
	 */
	private boolean _enteringText = false;
	private Runnable _toRunWhenTextEntered = null;

	/**
	 * Shared instance to go back to the previous menu.
	 */
	private final ActionMenuEntry goBack = new ActionMenuEntry("back", () -> {
		_menuHistory.pop();
		_selectionIndex = 0;
		Settings.save();
	});

	private MenuEntry[] _entries =
			{ new ActionMenuEntry("start game!", this::startGame),
					new TransitionMenuEntry("settings", new EditableMenuEntry(
							"player name", s -> Settings._playerName = s,
							Settings._playerName),
					new EditableMenuEntry("server IP",
							s -> Settings._serverIP = s, Settings._serverIP),
					new EditableMenuEntry("server port",
							s -> Settings._serverPort = Integer.parseInt(s),
							Settings._serverPort + "")),
			new ActionMenuEntry("quit", () -> System.exit(0)) };
	private boolean _shouldClose = false;

	@SuppressWarnings("serial")
	public Menu(Canvas canvas) {
		_menuHistory = new Stack<MenuEntry[]>() {

			{
				push(_entries);
			}
		};

		KeyListener listener;
		canvas.addKeyListener(listener = new KeyListener() {

			public void keyPressed(KeyEvent e) {
				if (!_enteringText) {
					switch (e.getKeyCode()) {
						case KeyEvent.VK_S:
						case KeyEvent.VK_DOWN:
							_selectionIndex = (_selectionIndex + 1)
									% _menuHistory.peek().length;
							break;

						case KeyEvent.VK_W:
						case KeyEvent.VK_UP:
							_selectionIndex = (_selectionIndex - 1
									+ _menuHistory.peek().length)
									% _menuHistory.peek().length;
							break;

						case KeyEvent.VK_ENTER:
							MenuEntry menu =
									_menuHistory.peek()[_selectionIndex];
							if (menu instanceof ActionMenuEntry) {
								((ActionMenuEntry) menu).getAction().run();
							} else if (menu instanceof TransitionMenuEntry) {
								_menuHistory.push(((TransitionMenuEntry) menu)
										.getSubEntries());
								_selectionIndex = 0;
							} else if (menu instanceof EditableMenuEntry) {
								_toRunWhenTextEntered =
										((EditableMenuEntry) menu)::onEditingComplete;
								_enteringText = true;
							}
							break;
					}
				} else {
					boolean reprocess = false;
					switch (e.getKeyCode()) {
						case KeyEvent.VK_W:
						case KeyEvent.VK_S:
						case KeyEvent.VK_UP:
						case KeyEvent.VK_DOWN:
							reprocess = true;
							// fall through for initial processing
						case KeyEvent.VK_ENTER:
							_enteringText = false;

							if (_toRunWhenTextEntered != null)
								_toRunWhenTextEntered.run();
					}
					if (reprocess) {
						keyPressed(e);
					}
				}
			}

			public void keyTyped(KeyEvent e) {
				if (_enteringText) {
					EditableMenuEntry editable =
							(EditableMenuEntry) _menuHistory
									.peek()[_selectionIndex];
					String _textField = editable.getContents();
					switch (e.getKeyChar()) {
						case '\n':
							// do nothing; leaving the entry field is handled in
							// keyPressed
							break;
						case '\b':
							if (_textField.length() > 0)
								_textField = _textField.substring(0,
										_textField.length() - 1);
							break;
						default:
							// because I know you folks and you are going to
							// mess with this
							if (_textField.length() < 64)
								_textField += e.getKeyChar();
					}
					editable.setContents(_textField);
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		});

		BufferStrategy buff = canvas.getBufferStrategy();
		while (!_shouldClose) {
			Graphics2D g = (Graphics2D) buff.getDrawGraphics();

			g.addRenderingHints(
					new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
							RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
			g.addRenderingHints(
					new RenderingHints(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON));
			g.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY));
			g.addRenderingHints(
					new RenderingHints(RenderingHints.KEY_INTERPOLATION,
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
				g.drawString(
						(_selectionIndex == i ? "-> " : "") + entries[i]
								.getName()
						+ (_enteringText && _selectionIndex == i ? "_" : ""),
						100, 200 + 50 * i);
			}

			buff.show();
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
			}
		}

		// We should shut down now; remove all references so we can be garbage
		// collected
		canvas.removeKeyListener(listener);
	}

	private void startGame() {
		System.out.println("BOOP GAME START");

		// change "start game!" to connecting text; this just makes it more
		// obvious when the server is down
		// TODO handle server connection issues and client lobby intelligently
		_menuHistory.peek()[0] = () -> "[connecting....]";
		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {
		}

		// causes the main loop in Menu() to exit, and we return to the
		// CrashClient constructor, which starts the game
		_shouldClose = true;
	}

	// trolololololollll
	private static interface MenuEntry {

		String getName();

	}

	private static class ActionMenuEntry implements MenuEntry {

		private final String name;
		private final Runnable action;

		private ActionMenuEntry(String name, Runnable action) {
			this.name = name;
			this.action = action;
		}

		@Override
		public String getName() {
			return name;
		}

		public Runnable getAction() {
			return action;
		}

	}

	private class TransitionMenuEntry implements MenuEntry {

		private final String title;
		private final MenuEntry[] subEntries;

		private TransitionMenuEntry(String title, MenuEntry... subEntries) {
			this.title = title;
			// Implicit back entry
			this.subEntries =
					Stream.concat(Stream.of(subEntries), Stream.of(goBack))
							.toArray(MenuEntry[]::new);
		}

		@Override
		public String getName() {
			return title;
		}

		public MenuEntry[] getSubEntries() {
			return subEntries;
		}

	}

	private static class EditableMenuEntry implements MenuEntry {

		private final String title;
		private final Consumer<String> updateField;
		private String contents;

		private EditableMenuEntry(String title, Consumer<String> updateField,
				String contents) {
			this.title = title;
			this.updateField = updateField;
			this.contents = contents;
		}

		@Override
		public String getName() {
			return String.format("%s: %s", title, contents);
		}

		public String getContents() {
			return contents;
		}

		public void setContents(String contents) {
			this.contents = contents;
		}

		public void onEditingComplete() {
			updateField.accept(contents);
		}

	}
}
