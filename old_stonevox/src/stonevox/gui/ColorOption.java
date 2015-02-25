package stonevox.gui;

import org.newdawn.slick.Color;

import stonevox.Program;
import stonevox.data.GUIelement;
import stonevox.data.Vector3;
import stonevox.decorator.PlainBackground;
import stonevox.decorator.PlainBorder;
import stonevox.util.GUI;

public class ColorOption extends GUIelement
{
	public static ColorOption lastOption;
	public Color color = Color.white;
	public Color huecolor = Color.white;
	public Vector3 colorsquarelocation = new Vector3(-10, -10, -10);

	public ColorOption(int id, float y, Color color)
	{
		super(id);
		this.setPositon(5, y);
		this.setSize(75f, 40f);
		this.color = color;
		this.huecolor = color;

		appearence.Add("background", new PlainBackground(color));
		appearence.Add("highlight", new PlainBorder(3f, new Color(254f / 256f, 254 / 256f, 62 / 256f)));
		appearence.Add("border", new PlainBorder(3f, Color.gray));
	}

	public PlainBackground getBackground()
	{
		return (PlainBackground) appearence.Get("background");
	}

	public PlainBorder getHighlight()
	{
		return (PlainBorder) appearence.Get("highlight");
	}

	public PlainBorder getBorder()
	{
		return (PlainBorder) appearence.Get("border");
	}

	public void select()
	{
		if (lastOption != null)
		{
			lastOption.deselect();
		}

		getBorder().setEnabled(false);
		getHighlight().setEnabled(true);

		GUIelement colorsquare = GUI.get(GUI.COLOR_PICKER_COLORSQARE);
		if (colorsquare != null)
		{
			if (colorsquare.getPlainMarker("marker") != null)
				colorsquare.getPlainMarker("marker").setPosition(colorsquarelocation.x, colorsquarelocation.y);
			if (colorsquare.getMultiColorBackground("mcBG") != null)
			{
				colorsquare.getMultiColorBackground("mcBG").setColor(2, huecolor);
				colorsquare.getMultiColorBackground("mcBG").updateGradient();
			}
		}

		lastOption = this;

		Program.toolpainter.SetColor(color);
	}

	public void deselect()
	{
		getBorder().setEnabled(true);
		getHighlight().setEnabled(false);
	}

	public void setColor(Color c)
	{
		this.color = c;
		getBackground().color = c;
	}

	public boolean isActive()
	{
		return lastOption == this;
	}

	public void setColoredSquareLocation(float x, float y)
	{
		colorsquarelocation.x = x;
		colorsquarelocation.y = y;
	}
}
