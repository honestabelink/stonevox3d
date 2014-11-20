package stonevox.gui;

import stonevox.data.GUIelement;
import stonevox.decorator.Sprite;

public class SpriteButton extends GUIelement
{
	public SpriteButton(int id, String lowlight, String highlight)
	{
		super(id);
		this.appearence.Add("background", new Sprite(lowlight, this));
		this.appearence.Add("background_highlight", new Sprite(highlight, this));

		getBackground().setEnabled(true);
		getBackgroundH().setEnabled(false);
	}

	public void mouseEnter()
	{
		getBackground().setEnabled(false);
		getBackgroundH().setEnabled(true);
		super.mouseEnter();
	}

	public void mouseLeave()
	{
		getBackground().setEnabled(true);
		getBackgroundH().setEnabled(false);
		super.mouseLeave();
	}

	public Sprite getBackground()
	{
		return (Sprite) appearence.Get("background");
	}

	public Sprite getBackgroundH()
	{
		return (Sprite) appearence.Get("background_highlight");
	}
}
