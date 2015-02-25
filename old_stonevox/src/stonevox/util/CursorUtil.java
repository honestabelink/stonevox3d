package stonevox.util;

import java.io.IOException;
import java.util.HashMap;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.opengl.CursorLoader;

public class CursorUtil
{
	public static String DEFAULT = "default";
	public static String ADD = "add";
	public static String REMOVE = "remove";
	public static String PAINT = "paint";
	public static String TOOL = "default";
	public static String COLORPICK = "colorpick";

	static HashMap<String, Cursor> cursors = new HashMap<String, Cursor>();
	static String currentCursor = "";
	static String lastCursor = "";

	static
	{
		cursors.put(DEFAULT, null);

		try
		{
			cursors.put(ADD, CursorLoader.get().getCursor(GetPath.getPath("/data/cursor_add.png"), 0, 0));
			cursors.put(REMOVE, CursorLoader.get().getCursor(GetPath.getPath("/data/cursor_remove.png"), 0, 0));
			cursors.put(PAINT, CursorLoader.get().getCursor(GetPath.getPath("/data/cursor_paint.png"), 0, 0));
			cursors.put(COLORPICK, CursorLoader.get().getCursor(GetPath.getPath("/data/cursor_eyedrop.png"), 0, 0));
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (LWJGLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void SetCursor(String id)
	{
		if (currentCursor != id)
		{
			lastCursor = currentCursor;
			currentCursor = id;
			try
			{
				Mouse.setNativeCursor(cursors.get(id));
			}
			catch (LWJGLException e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void SetCursor(String id, boolean tool)
	{
		if (currentCursor != id)
		{
			lastCursor = currentCursor;
			currentCursor = id;
			try
			{
				Mouse.setNativeCursor(cursors.get(id));
			}
			catch (LWJGLException e)
			{
				e.printStackTrace();
			}
		}

		if (tool)
			TOOL = id;
	}

	public static String getDefault(boolean tool)
	{
		if (tool)
			return TOOL;
		else
			return DEFAULT;
	}
}
