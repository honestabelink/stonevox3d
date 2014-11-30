package stonevox.tools;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;

import stonevox.Program;
import stonevox.data.Cube;
import stonevox.data.RayHitPoint;
import stonevox.data.Tool;
import stonevox.gui.ColorOption;
import stonevox.util.CursorUtil;
import stonevox.util.UndoUtil;

public class ToolPainter implements Tool
{
	public Color paintColor;
	private boolean active;
	private boolean wasmousedown = true;
	private RayHitPoint lasthitpoint = new RayHitPoint();

	private ArrayList<Float> undodata = new ArrayList<Float>(100);

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
		Program.model.GetActiveMatrix().clean();
		Program.rayCaster.raycast_dirt = true;
		CursorUtil.SetCursor(CursorUtil.PAINT, true);
	}

	public void deactivate()
	{
		this.active = false;

		if (undodata.size() > 0)
		{
			UndoUtil.putData(UndoUtil.PAINT, undodata);
			undodata = new ArrayList<Float>(100);
		}
	}

	public boolean repeatTest(RayHitPoint hit)
	{
		return false;
	}

	public void logic()
	{
		if (Mouse.isButtonDown(0) && !Keyboard.isKeyDown(Keyboard.KEY_LMENU))
		{
			wasmousedown = true;
			if (Program.rayCaster.rayhitpoint != null)
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
			if (undodata.size() > 0)
			{
				UndoUtil.putData(UndoUtil.PAINT, undodata);
				undodata = new ArrayList<Float>(100);
			}
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
		if (hit.cubelocation.y != -10000)
		{
			if (!lasthitpoint.cubelocation.isEqual(hit.cubelocation))
			{
				Cube cube = Program.model.GetActiveMatrix().getCubeSaftly(hit.cubelocation);
				if (cube != null)
				{
					if (!cube.isDirty)
					{
						undodata.add(hit.cubelocation.x);
						undodata.add(hit.cubelocation.y);
						undodata.add(hit.cubelocation.z);
						undodata.add(cube.fcolor.r);
						undodata.add(cube.fcolor.g);
						undodata.add(cube.fcolor.b);
					}

					Program.model.GetActiveMatrix().getCube(hit.cubelocation).setColor(paintColor);
					Program.model.GetActiveMatrix().updateMesh();

					lasthitpoint = hit;
				}
			}
		}
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

	@Override
	public void resetUndoRedo()
	{
		lasthitpoint.cubelocation.y = 1000000;
	}
}
