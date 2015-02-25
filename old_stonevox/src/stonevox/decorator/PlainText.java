package stonevox.decorator;

import org.newdawn.slick.Color;

import stonevox.Program;
import stonevox.data.GUIdecorator;
import stonevox.data.TextDisplay;
import stonevox.util.GUI;
import stonevox.util.Scale;

public class PlainText implements GUIdecorator
{
	public boolean enabled = true;
	private TextDisplay td;

	public float xoffset;
	public float yoffset;

	public PlainText(String fontID, String text, Color color)
	{
		td = new TextDisplay(fontID, text, 0, 0, color);

		GUI.text.add(td);
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;

		if (!enabled)
		{
			if (GUI.text.contains(td))
				GUI.text.remove(td);
		}
		else
		{
			if (!GUI.text.contains(td))
				GUI.text.add(td);
		}
	}

	public void paint(float x, float y, float width, float height)
	{
		td.x = (float) Scale.hUnPosScale(x) + xoffset;
		td.y = (Program.height - ((float) Scale.vUnPosScale(y)) - td.font.height * 1.25f) + yoffset;
	}

	public void dispose()
	{
		// GUI.text.remove(textdisplay);
	}

	public void setColor(Color color)
	{
		td.color = color;
	}

	public void setText(String text)
	{
		td.text = text;
	}

	public String getText()
	{
		return td.text;
	}

	public float[] getTextSize()
	{
		float[] t = new float[2];
		t[0] = td.font.font.getWidth(td.text) + xoffset;
		t[1] = td.font.font.getHeight(td.text) + yoffset;

		return t;
	}

	public float getFontHeight()
	{
		return td.font.height;
	}

	public float[] measureString(String text)
	{
		float[] t = new float[2];
		t[0] = td.font.font.getWidth(text);
		t[1] = td.font.font.getHeight(text);
		return t;
	}

}
