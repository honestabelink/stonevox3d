package stonevox.util;

import java.util.HashMap;

import stonevox.data.Keyhook;

public class KeyboardUtil
{
	static HashMap<Integer, Keyhook> keyboardhook = new HashMap<Integer, Keyhook>();

	public static boolean handleKeyboardInput(int key, boolean keystate)
	{
		if (keystate)
		{
			if (keyboardhook.containsKey(key))
			{
				keyboardhook.get(key).down();
				return true;
			}
		}
		else
		{
			if (keyboardhook.containsKey(key))
			{
				keyboardhook.get(key).up();
				return true;
			}
		}
		return false;
	}

	public static void Add(int key, Keyhook hook)
	{
		if (!keyboardhook.containsKey(key))
		{
			keyboardhook.put(key, hook);
		}
		else
		{
			keyboardhook.replace(key, hook);
		}
	}
}
