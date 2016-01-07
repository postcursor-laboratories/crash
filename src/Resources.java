import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;


public class Resources {
	static int
		W = (int)java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
		H = (int)java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight()  - 50;
	static final Color MAROON = new Color(120, 0, 160);

	static Font font;
	static Image bg;
	
	static {
		try {
			InputStream is = CrashServer.class.getClassLoader().getResourceAsStream(
					"AnonymousPro-1.002.001/Anonymous Pro.ttf");
					//"ScoreFont.ttf");
			font = Font.createFont(Font.TRUETYPE_FONT, is);
			System.out.println(font);
			is.close();
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
}
