package stonevox.tools;

import java.util.ArrayList;

import org.lwjgl.input.Mouse;

import stonevox.Program;
import stonevox.data.Cube;
import stonevox.data.RayHitPoint;
import stonevox.data.Tool;
import stonevox.util.CursorUtil;
import stonevox.util.UndoUtil;

public class ToolRemove implements Tool
{
	private boolean active;
	private RayHitPoint lasthitpoint = new RayHitPoint();
	public int state = 0;
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
		Program.rayCaster.raycast_dirt = true;
		Program.model.GetActiveMatrix().clean();
		CursorUtil.SetCursor(CursorUtil.REMOVE, true);
	}

	public void deactivate()
	{
		this.active = false;
		Program.model.GetActiveMatrix().clean();

		if (undodata.size() > 0)
		{
			UndoUtil.putData(UndoUtil.REMOVE, undodata);
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
		if (Mouse.isButtonDown(0))
		{
			wasmousedown = true;
			if (Program.rayCaster.getAllowRaycasting())
			{
				use(Program.rayCaster.rayhitpoint);
			}
		}
		else if (wasmousedown)
		{
			wasmousedown = false;
			Program.model.GetActiveMatrix().clean();

			if (undodata.size() > 0)
			{
				UndoUtil.putData(UndoUtil.REMOVE, undodata);
				undodata = new ArrayList<Float>(100);
			}
		}
	}

	public boolean handelInput(int key, boolean state)
	{
		return false;
	}

	public void use(RayHitPoint hit)
	{
		if ((!lasthitpoint.cubelocation.isEqual(hit.cubelocation))
				|| (lasthitpoint.cubelocation.isEqual(hit.cubelocation) && !lasthitpoint.cubenormal
						.isEqual(hit.cubenormal)))
		{
			Cube cube = Program.model.GetActiveMatrix().getCubeSaftly(hit.cubelocation);
			if (cube != null && !cube.isDirty)
			{
				Program.model.GetActiveMatrix().getCube(hit.cubelocation).setColor(-1, -1, -1, 0);
				Program.model.GetActiveMatrix().updateLightMap(cube.pos);
				Program.model.GetActiveMatrix().updateMesh();

				undodata.add(hit.cubelocation.x);
				undodata.add(hit.cubelocation.y);
				undodata.add(hit.cubelocation.z);
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
		this.state = id;
	}

	@Override
	public void resetUndoRedo()
	{
		lasthitpoint.cubelocation.y = 1000000;
	}
}
