package stonevox.tools;

import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;

import stonevox.Program;
import stonevox.data.Cube;
import stonevox.data.RayHitPoint;
import stonevox.gui.ColorOption;

public class ToolPainter implements Tool
{
	public Color		paintColor;
	private boolean		active;

	private RayHitPoint	lasthitpoint	= new RayHitPoint();

	public boolean isActive()
	{
		return active;
	}

	public void SetColor(Color c)
	{
		this.paintColor = c;
	}

	public void init()
	{
	}

	public void activate()
	{
		this.active = true;
		this.paintColor = ColorOption.lastOption.getBackground().color;
		Program.rayCaster.raycast_dirt = true;
	}

	public void deactivate()
	{
		this.active = false;
	}

	public boolean repeatTest(RayHitPoint hit)
	{
		return false;
	}

	public void logic()
	{
		if (Mouse.isButtonDown(0))
		{
			if (Program.rayCaster.rayhitpoint != null)
				use(Program.rayCaster.rayhitpoint);
		}
	}

	public void use(RayHitPoint hit)
	{
		if (hit.cubelocation.y != -10000)
		{
			if ((!lasthitpoint.cubelocation.isEqual(hit.cubelocation))
					|| (lasthitpoint.cubelocation.isEqual(hit.cubelocation) && !lasthitpoint.cubenormal
							.isEqual(hit.cubenormal)))
			{
				Cube cube = Program.model.GetActiveMatrix().getCubeSaftly(hit.cubelocation);
				if (cube != null)
				{
					Program.model.GetActiveMatrix().getCube(hit.cubelocation).setColor(paintColor);
					Program.model.GetActiveMatrix().updateMesh();
					lasthitpoint = hit;
				}
			}
		}
	}

	public void undo()
	{
	}

	public void redo()
	{
	}

	public int hotKey()
	{
		return 0;
	}

	public void render()
	{
	}

	public void setState(int id)
	{
	}

}
