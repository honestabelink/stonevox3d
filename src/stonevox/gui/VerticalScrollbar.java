package stonevox.gui;

import org.newdawn.slick.Color;

import stonevox.data.GUIelement;
import stonevox.data.GUIlayout;
import stonevox.decorator.PlainBackground;
import stonevox.decorator.PlainBorder;
import stonevox.util.GUI;

public class VerticalScrollbar extends GUIelement
{

	private GUIelement	scrollbar;

	private int			maxvalue	= 10;

	public VerticalScrollbar(int ID, float width, float height)
	{
		super(ID);

		setSize(width, height);

		this.appearence.Add("bg", new PlainBackground(Color.gray.darker(.3f)));
		this.appearence.Add("border", new PlainBorder(1.5f, Color.yellow));

		scrollbar = new GUIelement(GUI.getNextID())
		{

		};
		scrollbar.setParent(this);
		scrollbar.width = width * 1.15f;
		scrollbar.height = height * 1.15f;

		scrollbar.appearence.Add("bg", new PlainBackground(Color.gray.brighter(.4f)));
		scrollbar.appearence.Add("border", new PlainBorder(3f, Color.gray));

		GUI.AddElement(scrollbar);
		GUI.layout.add(new GUIlayout(scrollbar.ID, false));
		GUI.layout.add(new GUIlayout(this.ID, false));
	}
}
