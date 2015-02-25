package stonevox.util;

import java.util.HashMap;

import stonevox.data.TTFFont;

public class FontUtil
{
	static HashMap<String, TTFFont> fonts = new HashMap<String, TTFFont>();

	public static void loadFont(String fontID, String fontPath)
	{
		fonts.put(fontID, new TTFFont(fontPath));
	}

	public static TTFFont GetFont(String fontID)
	{
		return fonts.get(fontID);
	}
}
