package stonevox.gui;

import java.util.ArrayList;

import stonevox.data.GUIelement;

public class Tab extends GUIelement
{
	private ArrayList<GUIelement> elements = new ArrayList<GUIelement>();
	private boolean elementsEnabled;

	public Tab(int ID, boolean enabled, GUIelement... controlElements)
	{
		super(ID);

		elementsEnabled = enabled;

		for (int i = 0; i < controlElements.length; i++)
		{
			elements.add(i, controlElements[i]);
			controlElements[i].setEnable(enabled);
		}
	}

	public Tab(int ID, boolean enabled, ArrayList<GUIelement> controlElements)
	{
		super(ID);

		elementsEnabled = enabled;

		for (int i = 0; i < controlElements.size(); i++)
		{
			elements.add(i, controlElements.get(i));
			controlElements.get(i).setEnable(enabled);
		}
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
		return elementsEnabled;
	}

	public void addControl(GUIelement el)
	{
		elements.add(el);
	}

	private void setControlsEnabled(boolean enabled)
	{
		elementsEnabled = enabled;
		for (int i = 0; i < elements.size(); i++)
		{
			elements.get(i).setEnable(enabled);
		}
	}
}
