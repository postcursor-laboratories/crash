import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.Arrays;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
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

	private MenuEntry[] _entries = {
			new ActionMenuEntry("start game!", this::startGame),
			new TransitionMenuEntry("settings",
					new EditableMenuEntry("player name", Settings._playerName,
							s -> Settings._playerName = s),
					new EditableMenuEntry("server IP", Settings._serverIP,
							s -> Settings._serverIP = s),
					new EditableMenuEntry("server port",
							Settings._serverPort + "",
							s -> Settings._serverPort = Integer.parseInt(s),
							codePoint -> '0' <= codePoint && codePoint <= '9')),
			new TextDisplayMenuEntry("about",
					"Crash v0.smol, by Postcursor Laboratories, January 2016",
					"Postcursor Laboratories is a derivative of 1064CBread LLC",
					"(and is also undeniably hella)"), // you don't need git
														// blame for this
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
					int lastIndex = _selectionIndex;
					switch (e.getKeyCode()) {
						case KeyEvent.VK_S:
						case KeyEvent.VK_DOWN:
							if (lastIndex == -1) {
								break;
							}
							do {
								_selectionIndex = (_selectionIndex + 1)
										% _menuHistory.peek().length;
							} while (_menuHistory
									.peek()[_selectionIndex] instanceof UnselectableMenuEntry && _selectionIndex != lastIndex);
							if (_selectionIndex == lastIndex) {
								_selectionIndex = -1;
							}
							break;

						case KeyEvent.VK_W:
						case KeyEvent.VK_UP:
							if (lastIndex == -1) {
								break;
							}
							do {
								_selectionIndex = (_selectionIndex - 1
										+ _menuHistory.peek().length)
										% _menuHistory.peek().length;
							} while (_menuHistory
									.peek()[_selectionIndex] instanceof UnselectableMenuEntry && _selectionIndex != lastIndex);
							if (_selectionIndex == lastIndex) {
								_selectionIndex = -1;
							}
							break;

						case KeyEvent.VK_ENTER:
							if (lastIndex == -1) {
								break;
							}
							MenuEntry menu =
									_menuHistory.peek()[_selectionIndex];
							if (menu instanceof ActionMenuEntry) {
								((ActionMenuEntry) menu).getAction().run();
							} else if (menu instanceof TransitionMenuEntry) {
								_menuHistory.push(((TransitionMenuEntry) menu)
										.getSubEntries());
								int length = _menuHistory.peek().length;
								_selectionIndex = 0;
								while (_menuHistory
										.peek()[_selectionIndex] instanceof UnselectableMenuEntry && _selectionIndex < length) {
									_selectionIndex++;
								}
								if (_selectionIndex == length) {
									_selectionIndex = -1;
								}
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
					if (_selectionIndex == -1) {
						// sanity check
						_enteringText = false;
						return;
					}
					EditableMenuEntry editable =
							(EditableMenuEntry) _menuHistory
									.peek()[_selectionIndex];
					String _textField = editable.getContents();
					char keyChar = e.getKeyChar();
					switch (keyChar) {
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
							if (_textField.length() < 64
									&& editable.isCodePointValid(keyChar))
								_textField += keyChar;
					}
					try {
						editable.setContents(_textField);
					} catch (IllegalArgumentException badText) {
						// ignore this
					}
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
		private final IntPredicate charValidation;
		private String contents;

		private EditableMenuEntry(String title, String contents,
				Consumer<String> updateField) {
			this(title, contents, updateField, x -> true);
		}

		private EditableMenuEntry(String title, String contents,
				Consumer<String> updateField, IntPredicate charValidation) {
			this.title = title;
			this.updateField = updateField;
			this.charValidation = charValidation;
			this.contents = contents;
		}

		@Override
		public String getName() {
			return String.format("%s: %s", title, contents);
		}

		public boolean isCodePointValid(int codePoint) {
			return charValidation.test(codePoint);
		}

		public String getContents() {
			return contents;
		}

		public void setContents(String contents) {
			int[] invalid = contents.codePoints()
					.filter(charValidation.negate()).toArray();
			if (invalid.length > 0) {
				throw new IllegalArgumentException(
						"invalid characters in contents: "
								+ Arrays.toString(invalid));
			}
			this.contents = contents;
		}

		public void onEditingComplete() {
			updateField.accept(contents);
		}
	}

	private static class UnselectableMenuEntry implements MenuEntry {

		private final String title;

		public UnselectableMenuEntry(String title) {
			this.title = title;
		}

		@Override
		public String getName() {
			return this.title;
		}
	}

	private class TextDisplayMenuEntry extends TransitionMenuEntry {

		private TextDisplayMenuEntry(String title, String... lines) {
			super(title, Stream.of(lines).map(UnselectableMenuEntry::new)
					.toArray(UnselectableMenuEntry[]::new));
		}
	}
}
