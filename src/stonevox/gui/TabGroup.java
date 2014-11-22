package stonevox.gui;

import java.util.ArrayList;

import stonevox.data.GUIelement;

public class TabGroup extends GUIelement
{

	public ArrayList<Tab>	tabs	= new ArrayList<Tab>();

	public TabGroup(int ID, Tab... tabs)
	{
		super(ID);

		for (int i = 0; i < tabs.length; i++)
			this.tabs.add(i, tabs[i]);
	}

	public void select(int id)
	{
		for (int i = 0; i < tabs.size(); i++)
		{
			if (tabs.get(i).ID == id)
			{
				tabs.get(i).select();
			}
			else
				tabs.get(i).deselect();
		}
	}
}
