package stonevox.tools;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;

import stonevox.Program;
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
				if (Program.model.GetActiveMatrix().hasCube(hit.cubelocation))
				{
					Color color = Program.model.GetActiveMatrix().getColor(hit.cubelocation).toNEWDAWN();

					undodata.add(hit.cubelocation.x);
					undodata.add(hit.cubelocation.y);
					undodata.add(hit.cubelocation.z);
					undodata.add(color.r);
					undodata.add(color.g);
					undodata.add(color.b);

					Program.model.GetActiveMatrix().setVoxelColor(hit.cubelocation,
							stonevox.data.Color.FromNEWDAWN(paintColor));

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

	public void resetUndoRedo()
	{
		lasthitpoint.cubelocation.y = 1000000;
	}
}
