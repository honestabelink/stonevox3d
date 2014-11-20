package stonevox.tools;

import org.lwjgl.input.Mouse;

import stonevox.Program;
import stonevox.data.Cube;
import stonevox.data.RayHitPoint;
import stonevox.data.Vector3;
import stonevox.gui.ColorOption;

public class ToolEdit implements Tool
{
	public static int	STATE_ADD		= 0;
	public static int	STATE_REMOVE	= 1;

	private boolean		active;
	private RayHitPoint	lasthitpoint	= new RayHitPoint();

	public int			state			= 0;

	private boolean		wasmousedown	= false;

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
		}
	}

	public void use(RayHitPoint hit)
	{
		if (lasthitpoint != null)
		{
			if ((!lasthitpoint.cubelocation.isEqual(hit.cubelocation))
					|| (lasthitpoint.cubelocation.isEqual(hit.cubelocation) && !lasthitpoint.cubenormal
							.isEqual(hit.cubenormal)))
			{
				if (state == STATE_ADD)// && okChange(hit, true))
				{
					// System.out.print("hit z : " + hit.cubelocation.z + "\n");
					Cube cube = Program.model.GetActiveMatrix().getCubeSaftly(hit.cubelocation);
					Cube toaddcube =
							Program.model.GetActiveMatrix()
									.getCubeSaftly(Vector3.add(hit.cubelocation, hit.cubenormal));
					if (cube != null && toaddcube != null)
					{
						Program.model.GetActiveMatrix().getCube(Vector3.add(hit.cubelocation, hit.cubenormal))
								.setColor(ColorOption.lastOption.color);
						Program.model.GetActiveMatrix().updateLightMap(cube.pos);
						Program.model.GetActiveMatrix().updateMesh();
						// Program.model.GetActiveMatrix().GetCubeSaftly(hit.cubelocation).isDirty
						// = true;
					}
					else if (cube == null && toaddcube != null)
					{
						Program.model.GetActiveMatrix().getCubeSaftly(Vector3.add(hit.cubelocation, hit.cubenormal))
								.setColor(ColorOption.lastOption.color);
						Program.model.GetActiveMatrix().updateLightMap(toaddcube.pos);
						Program.model.GetActiveMatrix().updateMesh();
					}
				}
				else if (state == STATE_REMOVE)// && okChange(hit, false))
				{
					Cube cube = Program.model.GetActiveMatrix().getCubeSaftly(hit.cubelocation);
					if (cube != null && !cube.isDirty)
					{
						Program.model.GetActiveMatrix().getCube(hit.cubelocation).setColor(-1, -1, -1, 0);
						Program.model.GetActiveMatrix().updateLightMap(cube.pos);
						Program.model.GetActiveMatrix().updateMesh();
					}
				}
				lasthitpoint = hit;
			}
		}
		else
		{
			if (state == STATE_ADD)
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
			}
			else if (state == STATE_REMOVE)
			{
				Cube cube = Program.model.GetActiveMatrix().getCubeSaftly(hit.cubelocation);
				if (cube != null && !cube.isDirty)
				{
					Program.model.GetActiveMatrix().getCube(hit.cubelocation).setColor(-1, -1, -1, 0);
					Program.model.GetActiveMatrix().updateLightMap(cube.pos);
					Program.model.GetActiveMatrix().updateMesh();
				}
			}
			lasthitpoint = hit;
		}
	}

	// boolean okChange(RayHitPoint hit, boolean add)
	// {
	// if (add)
	// {
	// if (changes[(int) (hit.cubelocation.z)][(int) (hit.cubelocation.y)][(int)
	// (hit.cubelocation.x)] == 1)
	// {
	// changes[(int) (hit.cubelocation.z + hit.cubenormal.z)][(int)
	// (hit.cubelocation.y + hit.cubenormal.y)][(int) (hit.cubelocation.x +
	// hit.cubenormal.x)] =
	// 0;
	// return true;
	// }
	// }
	// else
	// {
	// if (changes[(int) (hit.cubelocation.z)][(int) (hit.cubelocation.y)][(int)
	// (hit.cubelocation.x)] == 1)
	// {
	// return true;
	// }
	// }
	// return false;
	// }

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
		this.state = id;

		if (this.state == STATE_ADD)
		{
			Program.rayCaster.raycast_dirt = false;
		}
		else if (this.state == STATE_REMOVE)
		{
			Program.rayCaster.raycast_dirt = true;
		}
	}
}
