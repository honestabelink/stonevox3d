package stonevox.gui;

import org.newdawn.slick.Color;

import stonevox.data.GUIelement;
import stonevox.decorator.PlainText;

public class Label extends GUIelement
{

	public Label(int ID, String text, Color color)
	{
		super(ID);
		this.appearence.Add("text", new PlainText("default", text, color));
		setText(text);
	}

	public void setTextColor(Color color)
	{
		this.getPlainText("text").setColor(color);
	}

	public void setText(String text)
	{
		this.getPlainText("text").setText(text);
		float[] size = getPlainText("text").getTextSize();
		this.setSize(size[0], size[1]);
	}
}
