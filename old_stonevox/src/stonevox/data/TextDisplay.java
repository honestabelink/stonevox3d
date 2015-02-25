package stonevox.data;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;

import stonevox.util.FontUtil;

public class TextDisplay
{
	public TTFFont font;

	public float x;
	public float y;

	public Color color;
	public String text;

	public TextDisplay(String fontID, String displayText, float x, float y, Color color)
	{
		font = FontUtil.GetFont(fontID);

		this.color = color;
		this.x = x;
		this.y = y;
		this.text = displayText;
	}

	public void render()
	{
		font.render(x, y, text, color);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
}
