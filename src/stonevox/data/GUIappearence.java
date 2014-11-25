package stonevox.data;

import java.util.HashMap;

public class GUIappearence
{
	HashMap<String, GUIdecorator> decs = new HashMap<String, GUIdecorator>();
	float z = .9f;

	public void render(float x, float y, float width, float height)
	{
		z = .9f;
		for (GUIdecorator value : decs.values())
		{
			if (value.isEnabled())
				value.paint(x, y, width, height);
			z -= 1f;
		}
	}

	public GUIdecorator Get(String name)
	{
		return decs.get(name);
	}

	public int getSize()
	{
		return decs.size();
	}

	public GUIdecorator Add(String name, GUIdecorator dec)
	{
		decs.put(name, dec);
		return dec;
	}

	public void SetEnabled(boolean enabled)
	{
		for (GUIdecorator value : decs.values())
		{
			value.setEnabled(enabled);
		}
	}

	public void SetEnabled(String name, boolean value)
	{
		Get(name).setEnabled(value);
	}

	public void dispose()
	{
		for (GUIdecorator value : decs.values())
		{
			value.dispose();
		}
	}

}
