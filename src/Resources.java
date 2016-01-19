import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;


public class Resources {
	private static int
		W = (int)java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
		H = (int)java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight()  - 50;
	private static double SCALE = 1;
	private static final int GAME_W = 2440;
	static final Pair<Integer, Integer> GAME_RATIO = new Pair<Integer, Integer>(16, 9) {
		public String toString() {
			return head + "/" + tail;
		};
	};

	public static void setH(int h) {
		H = h;
		remakeTransform();
	}

	public static void setW(int w) {
		W = w;
		remakeTransform();
	}

	public static int getH() {
		return H;
	}

	public static int getW() {
		return W;
	}

	private static void remakeTransform() {
		SCALE = chooseVisibleScale();
//		int gameW = getGameWidth();
//		int gameH = getGameHeight();
//		System.err.printf(
//				"Scaling %sx%s (r=%s) to %sx%s (r=%s) using target %sx%s (r=%s) (scale = %s)%n",
//				gameW, gameH, GAME_RATIO.head / GAME_RATIO.tail.doubleValue(), SCALE * gameW,
//				SCALE * gameH, (SCALE * gameW) / (SCALE * gameH), W, H,
//				W / (double) H, SCALE);
		synchronized (TRANSFORM) {
			TRANSFORM.setToIdentity();
			TRANSFORM.scale(SCALE, SCALE);
		}
	}

	private static double chooseVisibleScale() {
		double scaleW = W / (double) getGameWidth();
		if (scaleW * getGameHeight() > H) {
			// not visible by height
			double scaleH = H / (double) getGameHeight();
			if (scaleH * getGameWidth() > W) {
				// eh.
				System.err.println("Woah there, no good ratio? Is your screen non-existent?");
				return 1;
			}
			return scaleH;
		}
		return scaleW;
	}

	private static final AffineTransform TRANSFORM = new AffineTransform();

	static {
		remakeTransform();
	}
	
	static final Color MAROON = new Color(120, 0, 160); // display this when we can't load an image

	static Font font;
	static Image bg;
	
	static {
		try (InputStream is = CrashServer.class.getClassLoader().getResourceAsStream(
				"AnonymousPro-1.002.001/Anonymous Pro.ttf")) {
					//"ScoreFont.ttf");
			font = Font.createFont(Font.TRUETYPE_FONT, is);
			System.out.println(font);
		} catch (IOException | FontFormatException ex) {
			ex.printStackTrace();
			System.err.println("Font absent from res!");
			font = Font.decode("Arial 12");
		}
	
		try {
			bg = ImageIO.read(CrashServer.class.getClassLoader().getResourceAsStream(
					"sunset-bg.png"));
		} catch (IOException | IllegalArgumentException e1) {
			bg = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
			Graphics g=bg.getGraphics();
			g.setColor(MAROON);
			g.fillRect(0, 0, W, H);
		}
	}
	
	public static Font getFont(float size) {
		return font.deriveFont(size);
	}
	
	public static int getGameWidth() {
		return GAME_W;
	}
	
	public static int getGameHeight() {
		return GAME_W / GAME_RATIO.head * GAME_RATIO.tail;
	}

	public static void transform(Graphics2D g) {
		synchronized (TRANSFORM) {
			g.setTransform(TRANSFORM);
		}
	}
	
}
