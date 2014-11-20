package stonevox.gui;

import java.util.ArrayList;

import org.newdawn.slick.Color;

import stonevox.data.GUIelement;
import stonevox.decorator.PlainBackground;
import stonevox.decorator.PlainBorder;
import stonevox.util.GUI;

public class Tab extends GUIelement
{
	private ArrayList<Integer>	controls	= new ArrayList<Integer>();
	private boolean				controlsEnabled;

	public Tab(int ID, boolean enabled, int... controlIDs)
	{
		super(ID);

		controlsEnabled = enabled;

		for (int i = 0; i < controlIDs.length; i++)
		{
			controls.add(i);
			GUI.get(controlIDs[i]).enabled = enabled;
		}

		// need artwork
		// this.appearence.Add("bg", new Sprite());
		this.appearence.Add("bg", new PlainBackground(Color.black.brighter(.7f)));
		this.appearence.Add("border", new PlainBorder(3f, Color.yellow));
	}

	public void select()
	{
		setControlsEnabled(true);
	}

	public void deselect()
	{
		setControlsEnabled(false);
	}

	public boolean controlsEnabled()
	{
		return controlsEnabled;
	}

	private void setControlsEnabled(boolean enabled)
	{
		controlsEnabled = enabled;
		for (int i = 0; i < controls.size(); i++)
		{
			controls.add(i);
			GUI.get(controls.get(i)).enabled = enabled;
		}
	}
}
