package stonevox.tools;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import stonevox.Program;
import stonevox.data.Color;
import stonevox.data.RayHitPoint;
import stonevox.data.Tool;
import stonevox.data.Vector3;
import stonevox.gui.ColorOption;
import stonevox.util.CursorUtil;
import stonevox.util.UndoUtil;

public class ToolAdd implements Tool
{
	private boolean active;
	private RayHitPoint lasthitpoint = new RayHitPoint();
	private boolean wasmousedown = false;

	private ArrayList<Float> undodata = new ArrayList<Float>(100);

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
		Program.model.GetActiveMatrix().clean();
		CursorUtil.SetCursor(CursorUtil.ADD, true);

		undodata.clear();
	}

	public void deactivate()
	{
		this.active = false;
		Program.model.GetActiveMatrix().clean();

		if (undodata.size() > 0)
		{
			UndoUtil.putData(UndoUtil.ADD, undodata);
			undodata = new ArrayList<Float>(100);
		}
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

			if (undodata.size() > 0)
			{
				UndoUtil.putData(UndoUtil.ADD, undodata);
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
		if ((!lasthitpoint.cubelocation.isEqual(hit.cubelocation))
				|| (lasthitpoint.cubelocation.isEqual(hit.cubelocation) && !lasthitpoint.cubenormal
						.isEqual(hit.cubenormal)))
		{

			if (Program.model.GetActiveMatrix().withinRange(Vector3.add(hit.cubelocation, hit.cubenormal)))
			{
				Vector3 loc = Vector3.add(hit.cubelocation, hit.cubenormal);
				Program.model.GetActiveMatrix().addVoxel(loc, Color.FromNEWDAWN(ColorOption.lastOption.color));

				undodata.add(loc.x);
				undodata.add(loc.y);
				undodata.add(loc.z);
			}
			lasthitpoint = hit;
		}
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
	}

	@Override
	public void resetUndoRedo()
	{
		lasthitpoint.cubelocation.y = 1000000;
	}
}
