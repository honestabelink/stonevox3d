package stonevox.tools;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;

import stonevox.Program;
import stonevox.data.GUIelement;
import stonevox.data.RayHitPoint;
import stonevox.gui.ColorOption;
import stonevox.util.GUI;

public class ToolColorPicker implements Tool
{
	public boolean	active	= false;

	public boolean isActive()
	{
		return active;
	}

	public void init()
	{
	}

	public void activate()
	{
		active = true;
		// Program.DisableRayCasting();
	}

	public void deactivate()
	{
		active = false;
		// Program.EnableRayCasting();
	}

	public boolean repeatTest(RayHitPoint hit)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void logic()
	{
		if (isActive() && Program.fps % 2 == 0)
		{
			if (Mouse.isButtonDown(0))
			{
				use(Program.rayCaster.rayhitpoint);
			}
		}
	}

	public void use(RayHitPoint hit)
	{
		if (hit == null)
		{
			GL11.glReadBuffer(GL11.GL_FRONT);
			ByteBuffer buffer = BufferUtils.createByteBuffer(4);
			GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

			int r = buffer.get(0) & 0xFF;
			int g = buffer.get(1) & 0xFF;
			int b = buffer.get(2) & 0xFF;

			if (ColorOption.lastOption != null)
			{
				ColorOption.lastOption.setColor(new Color(r / 256f, g / 256f, b / 256f));
				ColorOption.lastOption.huecolor = new Color(r, g, b);

				GUIelement el = GUI.get(GUI.COLOR_PICKER_COLORSQARE);
				if (el != null)
				{
					el.getPlainMarker("marker").setPosition(-10, -10);
					el.getMultiColorBackground("mcBG").setColor(2, new Color(r, g, b));
					el.getMultiColorBackground("mcBG").updateGradient();
				}
			}
		}
		else
		{
			if (ColorOption.lastOption != null)
			{
				if (Program.model.GetActiveMatrix().getCubeSaftly(hit.cubelocation) != null)
				{
					Color c = Program.model.GetActiveMatrix().getCube(hit.cubelocation).fcolor;
					ColorOption.lastOption.setColor(new Color(c.r, c.g, c.b));
					ColorOption.lastOption.huecolor = new Color(c.r, c.g, c.b);

					GUIelement el = GUI.get(GUI.COLOR_PICKER_COLORSQARE);
					if (el != null)
					{
						el.getPlainMarker("marker").setPosition(-10, -10);
						el.getMultiColorBackground("mcBG").setColor(2, c);
						el.getMultiColorBackground("mcBG").updateGradient();
					}
				}
			}
		}
	}

	public void undo()
	{
		// TODO Auto-generated method stub

	}

	public void redo()
	{
		// TODO Auto-generated method stub

	}

	public int hotKey()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public void render()
	{
		// TODO Auto-generated method stub

	}

	public void setState(int id)
	{
		// TODO Auto-generated method stub

	}
}
