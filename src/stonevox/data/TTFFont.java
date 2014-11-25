package stonevox.data;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.newdawn.slick.Color;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

import stonevox.util.GetPath;

public class TTFFont
{
	public UnicodeFont font;
	public float height;

	public TTFFont(String path)
	{
		DataInputStream in = null;
		try
		{
			in = new DataInputStream(new BufferedInputStream(new FileInputStream(GetPath.getPath(path))));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		Font awtFont2 = null;
		try
		{
			awtFont2 = Font.createFont(Font.TRUETYPE_FONT, in);
		}
		catch (FontFormatException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		awtFont2 = awtFont2.deriveFont(24f);

		font = new UnicodeFont(awtFont2);
		font.getEffects().add(new ColorEffect(java.awt.Color.white));
		font.addAsciiGlyphs();
		try
		{
			font.loadGlyphs();
		}
		catch (SlickException ex)
		{
			ex.printStackTrace();
		}

		try
		{
			in.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		height = font.getHeight("T");
	}

	public void render(float x, float y, String text, Color color)
	{
		font.drawString(x, y, text, color);
	}
}
