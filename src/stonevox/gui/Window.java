package stonevox.gui;

import java.util.ArrayList;

import stonevox.data.GUIelement;

public class Window extends GUIelement
{
	public ArrayList<GUIelement> elements = new ArrayList<GUIelement>();

	public Window(int ID, GUIelement... els)
	{
		super(ID);

		for (GUIelement e : els)
		{
			elements.add(e);
		}
	}

	@Override
	public void setEnable(boolean enabled)
	{
		super.setEnable(enabled);

		for (GUIelement e : elements)
		{
			e.setEnable(enabled);
		}
	}

}
