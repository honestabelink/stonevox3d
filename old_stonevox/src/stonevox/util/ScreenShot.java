package stonevox.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import stonevox.gui.Textbox;

public class ScreenShot
{

	private static BufferedImage getScaledImage(BufferedImage src, int w, int h)
	{
		int finalw = w;
		int finalh = h;
		double factor = 1.0d;
		if (src.getWidth() > src.getHeight())
		{
			factor = ((double) src.getHeight() / (double) src.getWidth());
			finalh = (int) (finalw * factor);
		}
		else
		{
			factor = ((double) src.getWidth() / (double) src.getHeight());
			finalw = (int) (finalh * factor);
		}

		BufferedImage resizedImg = new BufferedImage(finalw, finalh, BufferedImage.TRANSLUCENT);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(src, 0, 0, finalw, finalh, null);
		g2.dispose();
		return resizedImg;
	}

	public static void screenShot(int x, int y, int width, int height)
	{
		BufferedImage image = null;

		ByteBuffer fb = BufferUtils.createByteBuffer(width * height * 4);
		int[] pixels = new int[width * height];
		int bindex;

		GL11.glReadBuffer(GL11.GL_FRONT);
		GL11.glReadPixels(x, y, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, fb);

		for (int i = 0; i < pixels.length; i++)
		{
			bindex = i * 4;
			int a = fb.get(bindex + 3) & 0xff;
			int r = fb.get(bindex + 0) & 0xff;
			int g = fb.get(bindex + 1) & 0xff;
			int b = fb.get(bindex + 2) & 0xff;
			pixels[i] = (a << 24) | (r << 16) | (g << 8) | (b);
		}

		AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
		tx.translate(0, -height);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, width, height, pixels, 0, width);

		image = op.filter(image, null);

		try
		{
			Textbox projectnametextbox = (Textbox) GUI.get(GUI.PROJECTSETTINGS_NAME);
			String path = GetPath.getPath("");
			File folder = new File(path + "\\export");
			folder.mkdirs();
			File file = new File(path + "\\export\\" + projectnametextbox.text + ".png");
			String format = "PNG";
			ImageIO.write(image, format, file);
		}
		catch (Exception e)
		{
			System.out.println("ScreenShot() exception: " + e);
		}
	}
}
