package stonevox.tools;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import stonevox.Program;
import stonevox.data.Cube;
import stonevox.data.RayHitPoint;
import stonevox.data.Vector3;
import stonevox.gui.ColorOption;
import stonevox.util.CursorUtil;

public class ToolAdd implements Tool
{
	private boolean active;
	private RayHitPoint lasthitpoint = new RayHitPoint();

	public int state = 0;

	private boolean wasmousedown = false;

	public boolean isActive()
	{
		return active;
	}

	public void init()
	{
	}

	public void activate()
	{
		this.active = true;
		Program.rayCaster.raycast_dirt = false;
		CursorUtil.SetCursor(CursorUtil.ADD, true);
	}

	public void deactivate()
	{
		this.active = false;
		Program.model.GetActiveMatrix().clean();
	}

	public boolean repeatTest(RayHitPoint hit)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void logic()
	{
		if (Mouse.isButtonDown(0) && !Keyboard.isKeyDown(Keyboard.KEY_LMENU))
		{
			wasmousedown = true;
			if (Program.rayCaster.getAllowRaycasting())
			{
				use(Program.rayCaster.rayhitpoint);
			}
		}
		else if (Mouse.isButtonDown(0) && Keyboard.isKeyDown(Keyboard.KEY_LMENU))
		{
			if (Program.rayCaster.rayhitpoint != null)
			{
				Program.toolcolorpicker.use(Program.rayCaster.rayhitpoint);
			}
		}
		else if (wasmousedown)
		{
			wasmousedown = false;
			Program.model.GetActiveMatrix().clean();
		}
	}

	public boolean handelInput(int key, boolean state)
	{
		if (key == Keyboard.KEY_LMENU && state)
		{
			CursorUtil.SetCursor(CursorUtil.COLORPICK, false);
			return true;
		}
		else if (key == Keyboard.KEY_LMENU && !state)
		{
			CursorUtil.SetCursor(CursorUtil.getDefault(true));
			return true;
		}
		return false;
	}

	public void use(RayHitPoint hit)
	{
		if (lasthitpoint != null)
		{
			if ((!lasthitpoint.cubelocation.isEqual(hit.cubelocation))
					|| (lasthitpoint.cubelocation.isEqual(hit.cubelocation) && !lasthitpoint.cubenormal
							.isEqual(hit.cubenormal)))
			{
				Cube cube = Program.model.GetActiveMatrix().getCubeSaftly(hit.cubelocation);
				Cube toaddcube =
						Program.model.GetActiveMatrix().getCubeSaftly(Vector3.add(hit.cubelocation, hit.cubenormal));
				if (cube != null && toaddcube != null)
				{
					Program.model.GetActiveMatrix().getCube(Vector3.add(hit.cubelocation, hit.cubenormal))
							.setColor(ColorOption.lastOption.color);
					Program.model.GetActiveMatrix().updateLightMap(cube.pos);
					Program.model.GetActiveMatrix().updateMesh();
				}
				else if (cube == null && toaddcube != null)
				{
					Program.model.GetActiveMatrix().getCubeSaftly(Vector3.add(hit.cubelocation, hit.cubenormal))
							.setColor(ColorOption.lastOption.color);
					Program.model.GetActiveMatrix().updateLightMap(toaddcube.pos);
					Program.model.GetActiveMatrix().updateMesh();
				}
				lasthitpoint = hit;
			}
		}
		else
		{
			Cube cube = Program.model.GetActiveMatrix().getCubeSaftly(hit.cubelocation);
			Cube toaddcube =
					Program.model.GetActiveMatrix().getCubeSaftly(Vector3.add(hit.cubelocation, hit.cubenormal));
			if (cube != null && toaddcube != null)
			{
				Program.model.GetActiveMatrix().getCube(Vector3.add(hit.cubelocation, hit.cubenormal))
						.setColor(ColorOption.lastOption.color);
				Program.model.GetActiveMatrix().updateLightMap(cube.pos);
				Program.model.GetActiveMatrix().updateMesh();
			}
			else if (cube == null && toaddcube != null)
			{
				Program.model.GetActiveMatrix().getCubeSaftly(Vector3.add(hit.cubelocation, hit.cubenormal))
						.setColor(ColorOption.lastOption.color);
				Program.model.GetActiveMatrix().updateLightMap(toaddcube.pos);
				Program.model.GetActiveMatrix().updateMesh();
			}
			lasthitpoint = hit;
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
		// TODO Auto-generated method stub
		return 0;
	}

	public void render()
	{
		// TODO Auto-generated method stub

	}

	public void setState(int id)
	{
		this.state = id;

	}
}
