package stonevox.data;

import java.nio.IntBuffer;
import java.util.Arrays;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import stonevox.Program;
import stonevox.util.GUI;
import stonevox.util.RaycastingUtil;
import stonevox.util.SideUtil;

public class QbMatrix
{
	private float cubesize = 0.5f;

	private String name;
	public Vector3 pos;
	public Vector3 size;

	public Vector3 posSize;
	public boolean visible;

	public Matrix transform;
	public Matrix final_transform;

	public Color[][][] cubecolor;
	public boolean[][][] cubedirty;
	private QbMatrixSide front;
	private QbMatrixSide back;
	private QbMatrixSide top;
	private QbMatrixSide bottom;
	private QbMatrixSide left;
	private QbMatrixSide right;

	private IntBuffer indexbuffer;
	private int indexbufferID;

	public QbMatrix()
	{
		visible = true;

		front = new QbMatrixSide(Side.FRONT);
		back = new QbMatrixSide(Side.BACK);
		top = new QbMatrixSide(Side.TOP);
		bottom = new QbMatrixSide(Side.BOTTOM);
		left = new QbMatrixSide(Side.LEFT);
		right = new QbMatrixSide(Side.RIGHT);
	}

	public void setSize(int x, int y, int z)
	{
		indexbuffer = BufferUtils.createIntBuffer(x * z * y * 36);

		for (int i = 0; i < x * y * z * 6; i++)
		{
			indexbuffer.put(i * 4);
			indexbuffer.put(i * 4 + 1);
			indexbuffer.put(i * 4 + 2);

			indexbuffer.put(i * 4);
			indexbuffer.put(i * 4 + 2);
			indexbuffer.put(i * 4 + 3);
		}

		indexbuffer.flip();

		cubecolor = new Color[z][y][x];
		cubedirty = new boolean[z][y][x];
		front.setSize(x, y, z);
		back.setSize(x, y, z);
		top.setSize(x, y, z);
		bottom.setSize(x, y, z);
		left.setSize(x, y, z);
		right.setSize(x, y, z);

		size = new Vector3(x, y, z);
	}

	public void setPosition(int x, int y, int z)
	{
		pos = new Vector3(x * .5f, y * .5f, z * .5f);
		posSize = new Vector3((float) (size.x) * .5f - .5f, (float) (size.y) * .5f - .5f, (float) (size.z) * .5f - .5f);

		transform = Matrix.CreateTranslation(0, 0, 0);
	}

	public void generateVoxelData()
	{
		indexbufferID = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbufferID);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbuffer, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		front.indexbufferid = indexbufferID;
		back.indexbufferid = indexbufferID;
		top.indexbufferid = indexbufferID;
		bottom.indexbufferid = indexbufferID;
		left.indexbufferid = indexbufferID;
		right.indexbufferid = indexbufferID;

		Color color = null;

		for (int z = 0; z < size.z; z++)
		{
			for (int y = 0; y < size.y; y++)
			{
				for (int x = 0; x < size.x; x++)
				{
					color = cubecolor[z][y][x];

					// no cube
					if (color.a <= 1)
					{
						continue;
					}
					else
					{
						// front
						if ((color.a & 32) == 32)
						{
							front.putVertexData(x, y, z, color);
						}
						else
							front.putBlankData(x, y, z);

						// back
						if ((color.a & 64) == 64)
						{
							back.putVertexData(x, y, z, color);
						}
						else
							back.putBlankData(x, y, z);

						// top
						if ((color.a & 8) == 8)
						{
							top.putVertexData(x, y, z, color);
						}
						else
							top.putBlankData(x, y, z);

						// bottom
						if ((color.a & 16) == 16)
						{
							bottom.putVertexData(x, y, z, color);
						}
						else
							bottom.putBlankData(x, y, z);

						// left
						if ((color.a & 2) == 2)
						{
							left.putVertexData(x, y, z, color);
						}
						else
							left.putBlankData(x, y, z);

						// right
						if ((color.a & 4) == 4)
						{
							right.putVertexData(x, y, z, color);
						}
						else
							right.putBlankData(x, y, z);
					}
				}
			}
		}

		front.genVoxelBuffers();
		back.genVoxelBuffers();
		top.genVoxelBuffers();
		bottom.genVoxelBuffers();
		left.genVoxelBuffers();
		right.genVoxelBuffers();
	}

	public void encodeVisibilityMask()
	{
		for (int z = 0; z < size.z; z++)
		{
			for (int y = 0; y < size.y; y++)
			{
				for (int x = 0; x < size.x; x++)
				{
					if (cubecolor[z][y][x].a >= 1)
						updateVisibilityMaskRelative(x, y, z);
				}
			}
		}
	}

	private Color tempcolor = null;
	private int tempmask = 1;

	public void updateVisibilityMaskRelative(int x, int y, int z)
	{
		tempcolor = cubecolor[z][y][x];
		tempmask = 1;

		if (z - 1 >= 0)
		{
			if (cubecolor[z - 1][y][x].a == 0)
			{
				tempmask += SideUtil.getVisibilityMask(Side.FRONT);
			}
		}
		else
		{
			tempmask += SideUtil.getVisibilityMask(Side.FRONT);
		}

		if (z + 1 < size.z)
		{
			if (cubecolor[z + 1][y][x].a == 0)
			{
				tempmask += SideUtil.getVisibilityMask(Side.BACK);
			}
		}
		else
		{
			tempmask += SideUtil.getVisibilityMask(Side.BACK);
		}

		if (y + 1 < size.y)
		{
			if (cubecolor[z][y + 1][x].a == 0)
			{
				tempmask += SideUtil.getVisibilityMask(Side.TOP);
			}
		}
		else
		{
			tempmask += SideUtil.getVisibilityMask(Side.TOP);
		}

		if (y - 1 >= 0)
		{
			if (cubecolor[z][y - 1][x].a == 0)
			{
				tempmask += SideUtil.getVisibilityMask(Side.BOTTOM);
			}
		}
		else
		{
			tempmask += SideUtil.getVisibilityMask(Side.BOTTOM);
		}

		if (x + 1 < size.x)
		{
			if (cubecolor[z][y][x + 1].a == 0)
			{
				tempmask += SideUtil.getVisibilityMask(Side.LEFT);
			}
		}
		else
		{
			tempmask += SideUtil.getVisibilityMask(Side.LEFT);
		}

		if (x - 1 >= 0)
		{
			if (cubecolor[z][y][x - 1].a == 0)
			{
				tempmask += SideUtil.getVisibilityMask(Side.RIGHT);
			}
		}
		else
		{
			tempmask += SideUtil.getVisibilityMask(Side.RIGHT);
		}

		cubecolor[z][y][x].a = tempmask;
	}

	public void updateVoxelData(int mask, int x, int y, int z, Color color)
	{
		// front
		if ((mask & 32) == 32)
		{
			front.addVoxelData(x, y, z, color);
		}
		else
			front.removeVoxelData(x, y, z);

		// back
		if ((mask & 64) == 64)
		{
			back.addVoxelData(x, y, z, color);
		}
		else
			back.removeVoxelData(x, y, z);

		// top
		if ((mask & 8) == 8)
		{
			top.addVoxelData(x, y, z, color);
		}
		else
			top.removeVoxelData(x, y, z);

		// bottom
		if ((mask & 16) == 16)
		{
			bottom.addVoxelData(x, y, z, color);
		}
		else
			bottom.removeVoxelData(x, y, z);

		// left
		if ((mask & 2) == 2)
		{
			left.addVoxelData(x, y, z, color);
		}
		else
			left.removeVoxelData(x, y, z);

		// right
		if ((mask & 4) == 4)
		{
			right.addVoxelData(x, y, z, color);
		}
		else
			right.removeVoxelData(x, y, z);
	}

	public RayHitPoint rayTest()
	{
		RayHitPoint rayhit = new RayHitPoint();
		rayhit.distance = 100000;

		Color color = null;
		int ci = 0;
		float dis = 0;
		boolean allowdirt = Program.rayCaster.raycast_dirt;

		if (allowdirt)
		{
			for (int z = 0; z < size.z; z++)
			{
				for (int y = 0; y < size.y; y++)
				{
					for (int x = 0; x < size.x; x++)
					{
						color = cubecolor[z][y][x];

						if (cubedirty[z][y][x])
						{
							if (front.raytestUndefinedCube(x, y, z))
							{
								dis = RaycastingUtil.Distance(x * .5f, y * .5f, z * .5f - .5f);

								if (dis < rayhit.distance)
								{
									rayhit.cubelocation.x = x;
									rayhit.cubelocation.y = y;
									rayhit.cubelocation.z = z;
									rayhit.cubenormal = front.normal;
									rayhit.distance = dis;
								}
							}
							if (back.raytestUndefinedCube(x, y, z))
							{
								dis = RaycastingUtil.Distance(x * .5f, y * .5f, z * .5f + .5f);

								if (dis < rayhit.distance)
								{
									rayhit.cubelocation.x = x;
									rayhit.cubelocation.y = y;
									rayhit.cubelocation.z = z;
									rayhit.cubenormal = back.normal;
									rayhit.distance = dis;
								}
							}
							if (top.raytestUndefinedCube(x, y, z))
							{
								dis = RaycastingUtil.Distance(x * .5f, y * .5f + .5f, z * .5f);

								if (dis < rayhit.distance)
								{
									rayhit.cubelocation.x = x;
									rayhit.cubelocation.y = y;
									rayhit.cubelocation.z = z;
									rayhit.cubenormal = top.normal;
									rayhit.distance = dis;
								}
							}
							if (bottom.raytestUndefinedCube(x, y, z))
							{
								dis = RaycastingUtil.Distance(x * .5f, y * .5f - .5f, z * .5f);

								if (dis < rayhit.distance)
								{
									rayhit.cubelocation.x = x;
									rayhit.cubelocation.y = y;
									rayhit.cubelocation.z = z;
									rayhit.cubenormal = bottom.normal;
									rayhit.distance = dis;
								}
							}
							if (left.raytestUndefinedCube(x, y, z))
							{
								dis = RaycastingUtil.Distance(x * .5f + .5f, y * .5f, z * .5f);

								if (dis < rayhit.distance)
								{
									rayhit.cubelocation.x = x;
									rayhit.cubelocation.y = y;
									rayhit.cubelocation.z = z;
									rayhit.cubenormal = left.normal;
									rayhit.distance = dis;
								}
							}
							if (right.raytestUndefinedCube(x, y, z))
							{
								dis = RaycastingUtil.Distance(x * .5f - .5f, y * .5f, z * .5f);

								if (dis < rayhit.distance)
								{
									rayhit.cubelocation.x = x;
									rayhit.cubelocation.y = y;
									rayhit.cubelocation.z = z;
									rayhit.cubenormal = right.normal;
									rayhit.distance = dis;
								}
							}
						}
						else if (color.a > 1)
						{
							// front
							if ((color.a & 32) == 32)
							{
								ci = front.cubeindexs[z][y][x];

								if (ci > -1
										&& RaycastingUtil.rayTest(front.normal, front.vertexdata[ci],
												front.vertexdata[ci + 1], front.vertexdata[ci + 2],
												front.vertexdata[ci + 7], front.vertexdata[ci + 8],
												front.vertexdata[ci + 9], front.vertexdata[ci + 14],
												front.vertexdata[ci + 15], front.vertexdata[ci + 16]) != null
										|| RaycastingUtil.rayTest(front.normal, front.vertexdata[ci],
												front.vertexdata[ci + 1], front.vertexdata[ci + 2],
												front.vertexdata[ci + 14], front.vertexdata[ci + 15],
												front.vertexdata[ci + 16], front.vertexdata[ci + 21],
												front.vertexdata[ci + 22], front.vertexdata[ci + 23]) != null)
								{
									dis = RaycastingUtil.Distance(x * .5f, y * .5f, z * .5f - .5f);

									if (dis < rayhit.distance)
									{
										rayhit.cubelocation.x = x;
										rayhit.cubelocation.y = y;
										rayhit.cubelocation.z = z;
										rayhit.cubenormal = front.normal;
										rayhit.distance = dis;
									}
								}
							}

							// back
							if ((color.a & 64) == 64)
							{
								ci = back.cubeindexs[z][y][x];

								if (ci > -1
										&& RaycastingUtil.rayTest(back.normal, back.vertexdata[ci],
												back.vertexdata[ci + 1], back.vertexdata[ci + 2],
												back.vertexdata[ci + 7], back.vertexdata[ci + 8],
												back.vertexdata[ci + 9], back.vertexdata[ci + 14],
												back.vertexdata[ci + 15], back.vertexdata[ci + 16]) != null
										|| RaycastingUtil.rayTest(back.normal, back.vertexdata[ci],
												back.vertexdata[ci + 1], back.vertexdata[ci + 2],
												back.vertexdata[ci + 14], back.vertexdata[ci + 15],
												back.vertexdata[ci + 16], back.vertexdata[ci + 21],
												back.vertexdata[ci + 22], back.vertexdata[ci + 23]) != null)
								{
									dis = RaycastingUtil.Distance(x * .5f, y * .5f, z * .5f + .5f);

									if (dis < rayhit.distance)
									{
										rayhit.cubelocation.x = x;
										rayhit.cubelocation.y = y;
										rayhit.cubelocation.z = z;
										rayhit.cubenormal = back.normal;
										rayhit.distance = dis;
									}
								}
							}

							// top
							if ((color.a & 8) == 8)
							{
								ci = top.cubeindexs[z][y][x];

								if (ci > -1
										&& RaycastingUtil.rayTest(top.normal, top.vertexdata[ci],
												top.vertexdata[ci + 1], top.vertexdata[ci + 2], top.vertexdata[ci + 7],
												top.vertexdata[ci + 8], top.vertexdata[ci + 9],
												top.vertexdata[ci + 14], top.vertexdata[ci + 15],
												top.vertexdata[ci + 16]) != null
										|| RaycastingUtil.rayTest(top.normal, top.vertexdata[ci],
												top.vertexdata[ci + 1], top.vertexdata[ci + 2],
												top.vertexdata[ci + 14], top.vertexdata[ci + 15],
												top.vertexdata[ci + 16], top.vertexdata[ci + 21],
												top.vertexdata[ci + 22], top.vertexdata[ci + 23]) != null)
								{
									dis = RaycastingUtil.Distance(x * .5f, y * .5f + .5f, z * .5f);

									if (dis < rayhit.distance)
									{
										rayhit.cubelocation.x = x;
										rayhit.cubelocation.y = y;
										rayhit.cubelocation.z = z;
										rayhit.cubenormal = top.normal;
										rayhit.distance = dis;
									}
								}
							}

							// bottom
							if ((color.a & 16) == 16)
							{
								ci = bottom.cubeindexs[z][y][x];

								if (ci > -1
										&& RaycastingUtil.rayTest(bottom.normal, bottom.vertexdata[ci],
												bottom.vertexdata[ci + 1], bottom.vertexdata[ci + 2],
												bottom.vertexdata[ci + 7], bottom.vertexdata[ci + 8],
												bottom.vertexdata[ci + 9], bottom.vertexdata[ci + 14],
												bottom.vertexdata[ci + 15], bottom.vertexdata[ci + 16]) != null
										|| RaycastingUtil.rayTest(bottom.normal, bottom.vertexdata[ci],
												bottom.vertexdata[ci + 1], bottom.vertexdata[ci + 2],
												bottom.vertexdata[ci + 14], bottom.vertexdata[ci + 15],
												bottom.vertexdata[ci + 16], bottom.vertexdata[ci + 21],
												bottom.vertexdata[ci + 22], bottom.vertexdata[ci + 23]) != null)
								{
									dis = RaycastingUtil.Distance(x * .5f, y * .5f - .5f, z * .5f);

									if (dis < rayhit.distance)
									{
										rayhit.cubelocation.x = x;
										rayhit.cubelocation.y = y;
										rayhit.cubelocation.z = z;
										rayhit.cubenormal = bottom.normal;
										rayhit.distance = dis;
									}
								}
							}

							// left
							if ((color.a & 2) == 2)
							{
								ci = left.cubeindexs[z][y][x];

								if (ci > -1
										&& RaycastingUtil.rayTest(left.normal, left.vertexdata[ci],
												left.vertexdata[ci + 1], left.vertexdata[ci + 2],
												left.vertexdata[ci + 7], left.vertexdata[ci + 8],
												left.vertexdata[ci + 9], left.vertexdata[ci + 14],
												left.vertexdata[ci + 15], left.vertexdata[ci + 16]) != null
										|| RaycastingUtil.rayTest(left.normal, left.vertexdata[ci],
												left.vertexdata[ci + 1], left.vertexdata[ci + 2],
												left.vertexdata[ci + 14], left.vertexdata[ci + 15],
												left.vertexdata[ci + 16], left.vertexdata[ci + 21],
												left.vertexdata[ci + 22], left.vertexdata[ci + 23]) != null)
								{
									dis = RaycastingUtil.Distance(x * .5f + .5f, y * .5f, z * .5f);

									if (dis < rayhit.distance)
									{
										rayhit.cubelocation.x = x;
										rayhit.cubelocation.y = y;
										rayhit.cubelocation.z = z;
										rayhit.cubenormal = left.normal;
										rayhit.distance = dis;
									}
								}
							}

							// right
							if ((color.a & 4) == 4)
							{
								ci = right.cubeindexs[z][y][x];

								if (ci > -1
										&& RaycastingUtil.rayTest(right.normal, right.vertexdata[ci],
												right.vertexdata[ci + 1], right.vertexdata[ci + 2],
												right.vertexdata[ci + 7], right.vertexdata[ci + 8],
												right.vertexdata[ci + 9], right.vertexdata[ci + 14],
												right.vertexdata[ci + 15], right.vertexdata[ci + 16]) != null
										|| RaycastingUtil.rayTest(right.normal, right.vertexdata[ci],
												right.vertexdata[ci + 1], right.vertexdata[ci + 2],
												right.vertexdata[ci + 14], right.vertexdata[ci + 15],
												right.vertexdata[ci + 16], right.vertexdata[ci + 21],
												right.vertexdata[ci + 22], right.vertexdata[ci + 23]) != null)
								{
									dis = RaycastingUtil.Distance(x * .5f - .5f, y * .5f, z * .5f);

									if (dis < rayhit.distance)
									{
										rayhit.cubelocation.x = x;
										rayhit.cubelocation.y = y;
										rayhit.cubelocation.z = z;
										rayhit.cubenormal = right.normal;
										rayhit.distance = dis;
									}
								}
							}
						}
					}
				}
			}
		}
		else
		{
			for (int z = 0; z < size.z; z++)
			{
				for (int y = 0; y < size.y; y++)
				{
					for (int x = 0; x < size.x; x++)
					{
						if (cubedirty[z][y][x])// cube that we added
						{
							continue;
						}

						color = cubecolor[z][y][x];

						// no cube
						if (color.a > 0)
						{
							// front
							if ((color.a & 32) == 32)
							{
								ci = front.cubeindexs[z][y][x];

								if (ci > -1
										&& (RaycastingUtil.rayTest(front.normal, front.vertexdata[ci],
												front.vertexdata[ci + 1], front.vertexdata[ci + 2],
												front.vertexdata[ci + 7], front.vertexdata[ci + 8],
												front.vertexdata[ci + 9], front.vertexdata[ci + 14],
												front.vertexdata[ci + 15], front.vertexdata[ci + 16]) != null || RaycastingUtil
												.rayTest(front.normal, front.vertexdata[ci], front.vertexdata[ci + 1],
														front.vertexdata[ci + 2], front.vertexdata[ci + 14],
														front.vertexdata[ci + 15], front.vertexdata[ci + 16],
														front.vertexdata[ci + 21], front.vertexdata[ci + 22],
														front.vertexdata[ci + 23]) != null))
								{
									dis = RaycastingUtil.Distance(x * .5f, y * .5f, z * .5f - .5f);

									if (dis < rayhit.distance)
									{
										rayhit.cubelocation.x = x;
										rayhit.cubelocation.y = y;
										rayhit.cubelocation.z = z;
										rayhit.cubenormal = front.normal;
										rayhit.distance = dis;
									}
								}
							}
							else if (isDirty(x, y, z - 1))
							{
								if (front.raytestUndefinedCube(x, y, z))
								{
									dis = RaycastingUtil.Distance(x * .5f, y * .5f, z * .5f - .5f);

									if (dis < rayhit.distance)
									{
										rayhit.cubelocation.x = x;
										rayhit.cubelocation.y = y;
										rayhit.cubelocation.z = z;
										rayhit.cubenormal = front.normal;
										rayhit.distance = dis;
									}
								}
							}

							// back
							if ((color.a & 64) == 64)
							{
								ci = back.cubeindexs[z][y][x];

								if (ci > -1
										&& (RaycastingUtil.rayTest(back.normal, back.vertexdata[ci],
												back.vertexdata[ci + 1], back.vertexdata[ci + 2],
												back.vertexdata[ci + 7], back.vertexdata[ci + 8],
												back.vertexdata[ci + 9], back.vertexdata[ci + 14],
												back.vertexdata[ci + 15], back.vertexdata[ci + 16]) != null || RaycastingUtil
												.rayTest(back.normal, back.vertexdata[ci], back.vertexdata[ci + 1],
														back.vertexdata[ci + 2], back.vertexdata[ci + 14],
														back.vertexdata[ci + 15], back.vertexdata[ci + 16],
														back.vertexdata[ci + 21], back.vertexdata[ci + 22],
														back.vertexdata[ci + 23]) != null))
								{
									dis = RaycastingUtil.Distance(x * .5f, y * .5f, z * .5f + .5f);

									if (dis < rayhit.distance)
									{
										rayhit.cubelocation.x = x;
										rayhit.cubelocation.y = y;
										rayhit.cubelocation.z = z;
										rayhit.cubenormal = back.normal;
										rayhit.distance = dis;
									}
								}
							}
							else if (isDirty(x, y, z + 1))
							{
								if (back.raytestUndefinedCube(x, y, z))
								{
									dis = RaycastingUtil.Distance(x * .5f, y * .5f, z * .5f + .5f);

									if (dis < rayhit.distance)
									{
										rayhit.cubelocation.x = x;
										rayhit.cubelocation.y = y;
										rayhit.cubelocation.z = z;
										rayhit.cubenormal = back.normal;
										rayhit.distance = dis;
									}
								}
							}

							// top
							if ((color.a & 8) == 8)
							{
								ci = top.cubeindexs[z][y][x];

								if (ci > -1
										&& (RaycastingUtil.rayTest(top.normal, top.vertexdata[ci],
												top.vertexdata[ci + 1], top.vertexdata[ci + 2], top.vertexdata[ci + 7],
												top.vertexdata[ci + 8], top.vertexdata[ci + 9],
												top.vertexdata[ci + 14], top.vertexdata[ci + 15],
												top.vertexdata[ci + 16]) != null || RaycastingUtil.rayTest(top.normal,
												top.vertexdata[ci], top.vertexdata[ci + 1], top.vertexdata[ci + 2],
												top.vertexdata[ci + 14], top.vertexdata[ci + 15],
												top.vertexdata[ci + 16], top.vertexdata[ci + 21],
												top.vertexdata[ci + 22], top.vertexdata[ci + 23]) != null))
								{
									dis = RaycastingUtil.Distance(x * .5f, y * .5f + .5f, z * .5f);

									if (dis < rayhit.distance)
									{
										rayhit.cubelocation.x = x;
										rayhit.cubelocation.y = y;
										rayhit.cubelocation.z = z;
										rayhit.cubenormal = top.normal;
										rayhit.distance = dis;
									}
								}
							}
							else if (isDirty(x, y + 1, z))
							{
								if (top.raytestUndefinedCube(x, y, z))
								{
									dis = RaycastingUtil.Distance(x * .5f, y * .5f + .5f, z * .5f);

									if (dis < rayhit.distance)
									{
										rayhit.cubelocation.x = x;
										rayhit.cubelocation.y = y;
										rayhit.cubelocation.z = z;
										rayhit.cubenormal = top.normal;
										rayhit.distance = dis;
									}
								}
							}

							// bottom
							if ((color.a & 16) == 16)
							{
								ci = bottom.cubeindexs[z][y][x];

								if (ci > -1
										&& (RaycastingUtil.rayTest(bottom.normal, bottom.vertexdata[ci],
												bottom.vertexdata[ci + 1], bottom.vertexdata[ci + 2],
												bottom.vertexdata[ci + 7], bottom.vertexdata[ci + 8],
												bottom.vertexdata[ci + 9], bottom.vertexdata[ci + 14],
												bottom.vertexdata[ci + 15], bottom.vertexdata[ci + 16]) != null || RaycastingUtil
												.rayTest(bottom.normal, bottom.vertexdata[ci],
														bottom.vertexdata[ci + 1], bottom.vertexdata[ci + 2],
														bottom.vertexdata[ci + 14], bottom.vertexdata[ci + 15],
														bottom.vertexdata[ci + 16], bottom.vertexdata[ci + 21],
														bottom.vertexdata[ci + 22], bottom.vertexdata[ci + 23]) != null))
								{
									dis = RaycastingUtil.Distance(x * .5f, y * .5f - .5f, z * .5f);

									if (dis < rayhit.distance)
									{
										rayhit.cubelocation.x = x;
										rayhit.cubelocation.y = y;
										rayhit.cubelocation.z = z;
										rayhit.cubenormal = bottom.normal;
										rayhit.distance = dis;
									}
								}
							}
							else if (isDirty(x, y - 1, z))
							{
								if (bottom.raytestUndefinedCube(x, y, z))
								{
									dis = RaycastingUtil.Distance(x * .5f, y * .5f - .5f, z * .5f);

									if (dis < rayhit.distance)
									{
										rayhit.cubelocation.x = x;
										rayhit.cubelocation.y = y;
										rayhit.cubelocation.z = z;
										rayhit.cubenormal = bottom.normal;
										rayhit.distance = dis;
									}
								}
							}

							// left
							if ((color.a & 2) == 2)
							{
								ci = left.cubeindexs[z][y][x];

								if (ci > -1
										&& (RaycastingUtil.rayTest(left.normal, left.vertexdata[ci],
												left.vertexdata[ci + 1], left.vertexdata[ci + 2],
												left.vertexdata[ci + 7], left.vertexdata[ci + 8],
												left.vertexdata[ci + 9], left.vertexdata[ci + 14],
												left.vertexdata[ci + 15], left.vertexdata[ci + 16]) != null || RaycastingUtil
												.rayTest(left.normal, left.vertexdata[ci], left.vertexdata[ci + 1],
														left.vertexdata[ci + 2], left.vertexdata[ci + 14],
														left.vertexdata[ci + 15], left.vertexdata[ci + 16],
														left.vertexdata[ci + 21], left.vertexdata[ci + 22],
														left.vertexdata[ci + 23]) != null))
								{
									dis = RaycastingUtil.Distance(x * .5f + .5f, y * .5f, z * .5f);

									if (dis < rayhit.distance)
									{
										rayhit.cubelocation.x = x;
										rayhit.cubelocation.y = y;
										rayhit.cubelocation.z = z;
										rayhit.cubenormal = left.normal;
										rayhit.distance = dis;
									}
								}
							}
							else if (isDirty(x + 1, y, z))
							{
								if (left.raytestUndefinedCube(x, y, z))
								{
									dis = RaycastingUtil.Distance(x * .5f + .5f, y * .5f, z * .5f);

									if (dis < rayhit.distance)
									{
										rayhit.cubelocation.x = x;
										rayhit.cubelocation.y = y;
										rayhit.cubelocation.z = z;
										rayhit.cubenormal = left.normal;
										rayhit.distance = dis;
									}
								}
							}

							// right
							if ((color.a & 4) == 4)
							{
								ci = right.cubeindexs[z][y][x];

								if (ci > -1
										&& (RaycastingUtil.rayTest(right.normal, right.vertexdata[ci],
												right.vertexdata[ci + 1], right.vertexdata[ci + 2],
												right.vertexdata[ci + 7], right.vertexdata[ci + 8],
												right.vertexdata[ci + 9], right.vertexdata[ci + 14],
												right.vertexdata[ci + 15], right.vertexdata[ci + 16]) != null || RaycastingUtil
												.rayTest(right.normal, right.vertexdata[ci], right.vertexdata[ci + 1],
														right.vertexdata[ci + 2], right.vertexdata[ci + 14],
														right.vertexdata[ci + 15], right.vertexdata[ci + 16],
														right.vertexdata[ci + 21], right.vertexdata[ci + 22],
														right.vertexdata[ci + 23]) != null))
								{
									dis = RaycastingUtil.Distance(x * .5f - .5f, y * .5f, z * .5f);

									if (dis < rayhit.distance)
									{
										rayhit.cubelocation.x = x;
										rayhit.cubelocation.y = y;
										rayhit.cubelocation.z = z;
										rayhit.cubenormal = right.normal;
										rayhit.distance = dis;
									}
								}
							}
							else if (isDirty(x - 1, y, z))
							{
								if (right.raytestUndefinedCube(x, y, z))
								{
									dis = RaycastingUtil.Distance(x * .5f - .5f, y * .5f, z * .5f);

									if (dis < rayhit.distance)
									{
										rayhit.cubelocation.x = x;
										rayhit.cubelocation.y = y;
										rayhit.cubelocation.z = z;
										rayhit.cubenormal = right.normal;
										rayhit.distance = dis;
									}
								}
							}
						}
					}
				}
			}
		}
		return rayhit.distance == 100000 ? null : rayhit;
	}

	public void genLightingData()
	{
		Color color = null;

		for (int z = 0; z < size.z; z++)
		{
			for (int y = 0; y < size.y; y++)
			{
				for (int x = 0; x < size.x; x++)
				{
					color = cubecolor[z][y][x];

					if (color.a > 1)
					{
						updateLightMap(color.a, x, y, z);
					}
				}
			}
		}
	}

	public void updateLightMapAroundIncluding(int x, int y, int z)
	{
		int minx = x - 3 > -1 ? x - 3 : 0;
		int maxx = (int) (x + 3 < size.x - 1 ? x + 3 : (size.x - x) + x);

		int miny = y - 3 > -1 ? y - 3 : 0;
		int maxy = (int) (y + 3 < size.y - 1 ? y + 3 : (size.y - y) + y);

		int minz = z - 3 > -1 ? z - 3 : 0;
		int maxz = (int) (z + 3 < size.z - 1 ? z + 3 : (size.z - z) + z);

		Color color = null;

		for (int z2 = minz; z2 < maxz; z2++)
			for (int y2 = miny; y2 < maxy; y2++)
				for (int x2 = minx; x2 < maxx; x2++)
				{
					color = cubecolor[z2][y2][x2];
					updateLightMap(color.a, x2, y2, z2);
				}

	}

	public void updateLightMap(int mask, int x, int y, int z)
	{
		// front
		if ((mask & 32) == 32)
		{
			updateLightMapRelative(Side.FRONT, x, y, z);
		}

		// back
		if ((mask & 64) == 64)
		{
			updateLightMapRelative(Side.BACK, x, y, z);
		}

		// top
		if ((mask & 8) == 8)
		{
			updateLightMapRelative(Side.TOP, x, y, z);
		}

		// bottom
		if ((mask & 16) == 16)
		{
			updateLightMapRelative(Side.BOTTOM, x, y, z);
		}

		// left
		if ((mask & 2) == 2)
		{
			updateLightMapRelative(Side.LEFT, x, y, z);
		}

		// right
		if ((mask & 4) == 4)
		{
			updateLightMapRelative(Side.RIGHT, x, y, z);
		}
	}

	float[][] ld = new float[3][3];
	int px;
	int py;
	int pz;

	public void updateLightMapRelative(Side side, int x, int y, int z)
	{
		switch (side)
		{
			case FRONT:
				px = (int) (x + front.normal.x);
				py = (int) (y + front.normal.y);
				pz = (int) (z + front.normal.z);

				ld[0][0] = getLightValue(pz, py - 1, px - 1);
				ld[0][1] = getLightValue(pz, py - 1, px);
				ld[0][2] = getLightValue(pz, py - 1, px + 1);

				ld[1][0] = getLightValue(pz, py, px - 1);
				ld[1][1] = getLightValue(pz, py, px);
				ld[1][2] = getLightValue(pz, py, px + 1);

				ld[2][0] = getLightValue(pz, py + 1, px - 1);
				ld[2][1] = getLightValue(pz, py + 1, px);
				ld[2][2] = getLightValue(pz, py + 1, px + 1);

				front.setLightValues(x, y, z, (ld[0][0] + ld[0][1] + ld[1][0] + ld[1][1]) / 4f, (ld[1][1] + ld[0][1]
						+ ld[0][2] + ld[1][2]) / 4f, (ld[1][1] + ld[1][2] + ld[2][2] + ld[2][1]) / 4f, (ld[1][1]
						+ ld[1][0] + ld[2][0] + ld[2][1]) / 4f);
				break;

			case BACK:
				px = (int) (x + back.normal.x);
				py = (int) (y + back.normal.y);
				pz = (int) (z + back.normal.z);

				ld[0][0] = getLightValue(pz, py - 1, px - 1);
				ld[0][1] = getLightValue(pz, py - 1, px);
				ld[0][2] = getLightValue(pz, py - 1, px + 1);

				ld[1][0] = getLightValue(pz, py, px - 1);
				ld[1][1] = getLightValue(pz, py, px);
				ld[1][2] = getLightValue(pz, py, px + 1);

				ld[2][0] = getLightValue(pz, py + 1, px - 1);
				ld[2][1] = getLightValue(pz, py + 1, px);
				ld[2][2] = getLightValue(pz, py + 1, px + 1);

				back.setLightValues(x, y, z, (ld[0][0] + ld[0][1] + ld[1][0] + ld[1][1]) / 4f, (ld[1][1] + ld[0][1]
						+ ld[0][2] + ld[1][2]) / 4f, (ld[1][1] + ld[1][2] + ld[2][2] + ld[2][1]) / 4f, (ld[1][1]
						+ ld[1][0] + ld[2][0] + ld[2][1]) / 4f);
				break;

			case TOP:
				px = (int) (x + top.normal.x);
				py = (int) (y + top.normal.y);
				pz = (int) (z + top.normal.z);

				ld[0][0] = getLightValue(pz + 1, py, px - 1);
				ld[0][1] = getLightValue(pz + 1, py, px);
				ld[0][2] = getLightValue(pz + 1, py, px + 1);

				ld[1][0] = getLightValue(pz, py, px - 1);
				ld[1][1] = getLightValue(pz, py, px);
				ld[1][2] = getLightValue(pz, py, px + 1);

				ld[2][0] = getLightValue(pz - 1, py, px - 1);
				ld[2][1] = getLightValue(pz - 1, py, px);
				ld[2][2] = getLightValue(pz - 1, py, px + 1);

				top.setLightValues(x, y, z, (ld[0][0] + ld[0][1] + ld[1][0] + ld[1][1]) / 4f, (ld[1][1] + ld[0][1]
						+ ld[0][2] + ld[1][2]) / 4f, (ld[1][1] + ld[1][2] + ld[2][2] + ld[2][1]) / 4f, (ld[1][1]
						+ ld[1][0] + ld[2][0] + ld[2][1]) / 4f);
				break;

			case BOTTOM:
				px = (int) (x + bottom.normal.x);
				py = (int) (y + bottom.normal.y);
				pz = (int) (z + bottom.normal.z);

				ld[0][0] = getLightValue(pz + 1, py, px - 1);
				ld[0][1] = getLightValue(pz + 1, py, px);
				ld[0][2] = getLightValue(pz + 1, py, px + 1);

				ld[1][0] = getLightValue(pz, py, px - 1);
				ld[1][1] = getLightValue(pz, py, px);
				ld[1][2] = getLightValue(pz, py, px + 1);

				ld[2][0] = getLightValue(pz - 1, py, px - 1);
				ld[2][1] = getLightValue(pz - 1, py, px);
				ld[2][2] = getLightValue(pz - 1, py, px + 1);

				bottom.setLightValues(x, y, z, (ld[0][0] + ld[0][1] + ld[1][0] + ld[1][1]) / 4f, (ld[1][1] + ld[0][1]
						+ ld[0][2] + ld[1][2]) / 4f, (ld[1][1] + ld[1][2] + ld[2][2] + ld[2][1]) / 4f, (ld[1][1]
						+ ld[1][0] + ld[2][0] + ld[2][1]) / 4f);
				break;

			case LEFT:
				px = (int) (x + left.normal.x);
				py = (int) (y + left.normal.y);
				pz = (int) (z + left.normal.z);

				ld[0][0] = getLightValue(pz - 1, py - 1, px);
				ld[0][1] = getLightValue(pz, py - 1, px);
				ld[0][2] = getLightValue(pz + 1, py - 1, px);

				ld[1][0] = getLightValue(pz - 1, py, px);
				ld[1][1] = getLightValue(pz, py, px);
				ld[1][2] = getLightValue(pz + 1, py, px);

				ld[2][0] = getLightValue(pz - 1, py + 1, px);
				ld[2][1] = getLightValue(pz, py + 1, px);
				ld[2][2] = getLightValue(pz + 1, py + 1, px);

				left.setLightValues(x, y, z, (ld[0][0] + ld[0][1] + ld[1][0] + ld[1][1]) / 4f, (ld[1][1] + ld[0][1]
						+ ld[0][2] + ld[1][2]) / 4f, (ld[1][1] + ld[1][2] + ld[2][2] + ld[2][1]) / 4f, (ld[1][1]
						+ ld[1][0] + ld[2][0] + ld[2][1]) / 4f);
				break;

			case RIGHT:
				px = (int) (x + right.normal.x);
				py = (int) (y + right.normal.y);
				pz = (int) (z + right.normal.z);

				ld[0][0] = getLightValue(pz - 1, py - 1, px);
				ld[0][1] = getLightValue(pz, py - 1, px);
				ld[0][2] = getLightValue(pz + 1, py - 1, px);

				ld[1][0] = getLightValue(pz - 1, py, px);
				ld[1][1] = getLightValue(pz, py, px);
				ld[1][2] = getLightValue(pz + 1, py, px);

				ld[2][0] = getLightValue(pz - 1, py + 1, px);
				ld[2][1] = getLightValue(pz, py + 1, px);
				ld[2][2] = getLightValue(pz + 1, py + 1, px);

				right.setLightValues(x, y, z, (ld[0][0] + ld[0][1] + ld[1][0] + ld[1][1]) / 4f, (ld[1][1] + ld[0][1]
						+ ld[0][2] + ld[1][2]) / 4f, (ld[1][1] + ld[1][2] + ld[2][2] + ld[2][1]) / 4f, (ld[1][1]
						+ ld[1][0] + ld[2][0] + ld[2][1]) / 4f);

				break;
		}
	}

	public void removeVoxel(Vector3 location)
	{
		removeVoxel((int) location.x, (int) location.y, (int) location.z);
	}

	public void removeVoxel(int x, int y, int z)
	{
		cubedirty[z][y][x] = true;

		Color cubecolor = this.cubecolor[z][y][x];
		cubecolor.a = 0;

		updateVoxelData(cubecolor.a, x, y, z, cubecolor);

		if (hasCube(x, y, z + 1))
		{
			if ((this.cubecolor[z + 1][y][x].a & 32) != 32)
			{
				this.cubecolor[z + 1][y][x].a += 32;
				updateVoxelData(this.cubecolor[z + 1][y][x].a, x, y, z + 1, this.cubecolor[z + 1][y][x]);
			}
		}
		if (hasCube(x, y, z - 1))
		{
			if ((this.cubecolor[z - 1][y][x].a & 64) != 64)
			{
				this.cubecolor[z - 1][y][x].a += 64;
				updateVoxelData(this.cubecolor[z - 1][y][x].a, x, y, z - 1, this.cubecolor[z - 1][y][x]);
			}
		}

		if (hasCube(x, y + 1, z))
		{
			if ((this.cubecolor[z][y + 1][x].a & 16) != 16)
			{
				this.cubecolor[z][y + 1][x].a += 16;
				updateVoxelData(this.cubecolor[z][y + 1][x].a, x, y + 1, z, this.cubecolor[z][y + 1][x]);
			}
		}
		if (hasCube(x, y - 1, z))
		{
			if ((this.cubecolor[z][y - 1][x].a & 8) != 8)
			{
				this.cubecolor[z][y - 1][x].a += 8;
				updateVoxelData(this.cubecolor[z][y - 1][x].a, x, y - 1, z, this.cubecolor[z][y - 1][x]);
			}
		}

		if (hasCube(x + 1, y, z))
		{
			if ((this.cubecolor[z][y][x + 1].a & 4) != 4)
			{
				this.cubecolor[z][y][x + 1].a += 4;
				updateVoxelData(this.cubecolor[z][y][x + 1].a, x + 1, y, z, this.cubecolor[z][y][x + 1]);
			}
		}
		if (hasCube(x - 1, y, z))
		{
			if ((this.cubecolor[z][y][x - 1].a & 2) != 2)
			{
				this.cubecolor[z][y][x - 1].a += 2;
				updateVoxelData(this.cubecolor[z][y][x - 1].a, x - 1, y, z, this.cubecolor[z][y][x - 1]);
			}
		}

		updateLightMapAroundIncluding(x, y, z);
	}

	public void addVoxel(Vector3 location)
	{
		addVoxel((int) location.x, (int) location.y, (int) location.z,
				this.cubecolor[(int) location.z][(int) location.y][(int) location.x]);
	}

	public void addVoxel(int x, int y, int z)
	{
		addVoxel(x, y, z, this.cubecolor[z][y][x]);
	}

	public void addVoxel(Vector3 location, Color color)
	{
		addVoxel((int) location.x, (int) location.y, (int) location.z, color);
	}

	public void addVoxel(int x, int y, int z, Color color)
	{
		cubedirty[z][y][x] = true;

		Color cubecolor = this.cubecolor[z][y][x];
		cubecolor.r = color.r;
		cubecolor.g = color.g;
		cubecolor.b = color.b;

		updateVisibilityMaskRelative(x, y, z);
		updateVoxelData(cubecolor.a, x, y, z, cubecolor);

		if (hasCube(x, y, z + 1))
		{
			if ((this.cubecolor[z + 1][y][x].a & 32) == 32)
			{
				this.cubecolor[z + 1][y][x].a -= 32;
				updateVoxelData(this.cubecolor[z + 1][y][x].a, x, y, z + 1, this.cubecolor[z + 1][y][x]);
			}
		}
		if (hasCube(x, y, z - 1))
		{
			if ((this.cubecolor[z - 1][y][x].a & 64) == 64)
			{
				this.cubecolor[z - 1][y][x].a -= 64;
				updateVoxelData(this.cubecolor[z - 1][y][x].a, x, y, z - 1, this.cubecolor[z - 1][y][x]);
			}
		}

		if (hasCube(x, y + 1, z))
		{
			if ((this.cubecolor[z][y + 1][x].a & 16) == 16)
			{
				this.cubecolor[z][y + 1][x].a -= 16;
				updateVoxelData(this.cubecolor[z][y + 1][x].a, x, y + 1, z, this.cubecolor[z][y + 1][x]);
			}
		}
		if (hasCube(x, y - 1, z))
		{
			if ((this.cubecolor[z][y - 1][x].a & 8) == 8)
			{
				this.cubecolor[z][y - 1][x].a -= 8;
				updateVoxelData(this.cubecolor[z][y - 1][x].a, x, y - 1, z, this.cubecolor[z][y - 1][x]);
			}
		}

		if (hasCube(x + 1, y, z))
		{
			if ((this.cubecolor[z][y][x + 1].a & 4) == 4)
			{
				this.cubecolor[z][y][x + 1].a -= 4;
				updateVoxelData(this.cubecolor[z][y][x + 1].a, x + 1, y, z, this.cubecolor[z][y][x + 1]);
			}
		}
		if (hasCube(x - 1, y, z))
		{
			if ((this.cubecolor[z][y][x - 1].a & 2) == 2)
			{
				this.cubecolor[z][y][x - 1].a -= 2;
				updateVoxelData(this.cubecolor[z][y][x - 1].a, x - 1, y, z, this.cubecolor[z][y][x - 1]);
			}
		}

		updateLightMapAroundIncluding(x, y, z);
	}

	public void setVoxelColor(Vector3 location, Color color)
	{
		setVoxelColor((int) location.x, (int) location.y, (int) location.z, color);
	}

	public void setVoxelColor(int x, int y, int z, Color color)
	{
		cubedirty[z][y][x] = true;

		Color cubecolor = this.cubecolor[z][y][x];
		cubecolor.r = color.r;
		cubecolor.g = color.g;
		cubecolor.b = color.b;

		// front
		if ((cubecolor.a & 32) == 32)
		{
			front.setColor(x, y, z, color);
		}

		// back
		if ((cubecolor.a & 64) == 64)
		{
			back.setColor(x, y, z, color);
		}

		// top
		if ((cubecolor.a & 8) == 8)
		{
			top.setColor(x, y, z, color);
		}

		// bottom
		if ((cubecolor.a & 16) == 16)
		{
			bottom.setColor(x, y, z, color);
		}

		// left
		if ((cubecolor.a & 2) == 2)
		{
			left.setColor(x, y, z, color);
		}

		// right
		if ((cubecolor.a & 4) == 4)
		{
			right.setColor(x, y, z, color);
		}
	}

	public void render()
	{
		if (visible)
		{
			front.updateSubBufferData();
			back.updateSubBufferData();
			top.updateSubBufferData();
			bottom.updateSubBufferData();
			left.updateSubBufferData();
			right.updateSubBufferData();

			final_transform = Matrix.Multiply(Program.camera.modelview, transform);

			Program.shader.WriteUniformMatrix4("modelview\0", final_transform.GetBuffer());

			// if (front.normal.dot(Program.camera.direction) < 0)
			// {
			GL30.glBindVertexArray(front.vertexobjectbufferid);
			GL11.glDrawElements(GL11.GL_TRIANGLES, front.facecount * 6, GL11.GL_UNSIGNED_INT, 0l);
			GL30.glBindVertexArray(0);
			// }
			// if (back.normal.dot(Program.camera.direction) < 0)
			// {
			GL30.glBindVertexArray(back.vertexobjectbufferid);
			GL11.glDrawElements(GL11.GL_TRIANGLES, back.facecount * 6, GL11.GL_UNSIGNED_INT, 0l);
			GL30.glBindVertexArray(0);
			// }
			// if (top.normal.dot(Program.camera.direction) < 0)
			// {
			GL30.glBindVertexArray(top.vertexobjectbufferid);
			GL11.glDrawElements(GL11.GL_TRIANGLES, top.facecount * 6, GL11.GL_UNSIGNED_INT, 0l);
			GL30.glBindVertexArray(0);
			// }
			// if (bottom.normal.dot(Program.camera.direction) < 0)
			// {
			GL30.glBindVertexArray(bottom.vertexobjectbufferid);
			GL11.glDrawElements(GL11.GL_TRIANGLES, bottom.facecount * 6, GL11.GL_UNSIGNED_INT, 0l);
			GL30.glBindVertexArray(0);
			// }
			// if (left.normal.dot(Program.camera.direction) < 0)
			// {
			GL30.glBindVertexArray(left.vertexobjectbufferid);
			GL11.glDrawElements(GL11.GL_TRIANGLES, left.facecount * 6, GL11.GL_UNSIGNED_INT, 0l);
			GL30.glBindVertexArray(0);
			// }
			// if (right.normal.dot(Program.camera.direction) < 0)
			// {
			GL30.glBindVertexArray(right.vertexobjectbufferid);
			GL11.glDrawElements(GL11.GL_TRIANGLES, right.facecount * 6, GL11.GL_UNSIGNED_INT, 0l);
			GL30.glBindVertexArray(0);
			// }
		}
	}

	public String getSizeString()
	{
		return (int) size.x + "_" + (int) size.y + "_" + (int) size.z;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
		GUI.Broadcast(GUI.MESSAGE_QB_MATRIX_RENAMED, name, 100000);
	}

	public void CREATEZEROEDCUBES()
	{
		for (int z = 0; z < size.z; z++)
		{
			for (int y = 0; y < size.y; y++)
			{
				for (int x = 0; x < size.x; x++)
				{
					cubecolor[z][y][x] = new Color(0, 0, 0, 0);
				}
			}
		}
	}

	public void clean()
	{
		for (boolean[][] innerRow : cubedirty)
			for (boolean[] innerInnerRow : innerRow)
				Arrays.fill(innerInnerRow, false);
	}

	public Color getColor(Vector3 location)
	{
		return getColor((int) location.x, (int) location.y, (int) location.z);
	}

	public Color getColor(int x, int y, int z)
	{
		return cubecolor[z][y][x];
	}

	public boolean isDirty(Vector3 location)
	{
		return isDirty((int) location.x, (int) location.y, (int) location.z);
	}

	public boolean isDirty(int x, int y, int z)
	{
		if (z > size.z - 1)
			return false;
		else if (z < 0)
			return false;

		else if (y > size.y - 1)
			return false;
		else if (y < 0)
			return false;

		else if (x > size.x - 1)
			return false;
		else if (x < 0)
			return false;

		return cubedirty[z][y][x];
	}

	public boolean withinRange(Vector3 location)
	{
		return withinRange((int) location.x, (int) location.y, (int) location.z);
	}

	public boolean withinRange(int x, int y, int z)
	{
		if (z > size.z - 1)
			return false;
		else if (z < 0)
			return false;

		else if (y > size.y - 1)
			return false;
		else if (y < 0)
			return false;

		else if (x > size.x - 1)
			return false;
		else if (x < 0)
			return false;

		return true;
	}

	public boolean hasCube(Vector3 location)
	{
		return hasCube((int) location.x, (int) location.y, (int) location.z);
	}

	public boolean hasCube(int x, int y, int z)
	{
		if (z > size.z - 1)
			return false;
		else if (z < 0)
			return false;

		else if (y > size.y - 1)
			return false;
		else if (y < 0)
			return false;

		else if (x > size.x - 1)
			return false;
		else if (x < 0)
			return false;

		return cubecolor[z][y][x].a > 0;
	}

	public float getLightValue(int z, int y, int x)
	{
		if (z > size.z - 1)
			return 1f;
		if (z < 0)
			return 1f;

		if (y > size.y - 1)
			return 1f;
		if (y < 0)
			return 1f;

		if (x > size.x - 1)
			return 1f;
		if (x < 0)
			return 1f;

		return cubecolor[z][y][x].a > 0 ? .65f : 1f;
	}

	public void reSize(int x1, int y1, int z1)
	{
		Color[][][] ncolors = new Color[z1][y1][x1];

		for (int z = 0; z < z1; z++)
		{
			for (int y = 0; y < y1; y++)
			{
				for (int x = 0; x < x1; x++)
				{
					ncolors[z][y][x] = new Color(0, 0, 0, 0);
				}
			}
		}

		Color old = null;

		for (int z = 0; z < size.z; z++)
		{
			for (int y = 0; y < size.y; y++)
			{
				for (int x = 0; x < size.x; x++)
				{
					old = cubecolor[z][y][x];

					if (z >= z1)
						continue;
					if (x >= x1)
						continue;
					if (y >= y1)
						continue;

					ncolors[z][y][x] = new Color(old.r, old.g, old.b, old.a);
				}
			}
		}

		this.cubecolor = ncolors;

		dispose();

		indexbuffer = BufferUtils.createIntBuffer(x1 * z1 * y1 * 36);

		for (int i = 0; i < x1 * y1 * z1 * 6; i++)
		{
			indexbuffer.put(i * 4);
			indexbuffer.put(i * 4 + 1);
			indexbuffer.put(i * 4 + 2);

			indexbuffer.put(i * 4);
			indexbuffer.put(i * 4 + 2);
			indexbuffer.put(i * 4 + 3);
		}

		indexbuffer.flip();

		cubedirty = new boolean[z1][y1][x1];
		front.setSize(x1, y1, z1);
		back.setSize(x1, y1, z1);
		top.setSize(x1, y1, z1);
		bottom.setSize(x1, y1, z1);
		left.setSize(x1, y1, z1);
		right.setSize(x1, y1, z1);

		size = new Vector3(x1, y1, z1);

		generateVoxelData();
		genLightingData();
		// this.sizeX = x1;
		// this.sizeY = y1;
		// this.sizeZ = z1;
		// vertexdata = new float[sizeX * sizeY * sizeZ * 28 * 6];
		// indexdata = new int[sizeX * sizeY * sizeZ * 12 * 6];
		// this.setPosition(0, 0, 0);
		// generateMesh();
		//
		this.clean();
		this.encodeVisibilityMask();
		//
		Program.floor.updatemesh();
		GUI.Broadcast(GUI.MESSAGE_QB_MATRIX_RESIZED, this.getSizeString(), 100000);
	}

	public void hack_shift_model_backwards()
	{
		Color[][][] shift = new Color[(int) size.z][(int) size.y][(int) size.x];
		Color old = null;

		for (int z = 0; z < size.z; z++)
		{
			for (int y = 0; y < size.y; y++)
			{
				for (int x = 0; x < size.x; x++)
				{
					old = cubecolor[z][y][x];
					shift[z][y][x] = new Color(old.r, old.g, old.b, old.a);
				}
			}
		}

		for (int y = 0; y < size.y; y++)
		{
			for (int x = 0; x < size.x; x++)
			{
				if (hasCube(x, y, 0))
				{
					removeVoxel(x, y, 0);
				}
			}
		}

		for (int z = (int) (size.z - 1); z > 0; z--)
		{
			for (int y = 0; y < size.y; y++)
			{
				for (int x = 0; x < size.x; x++)
				{
					if (shift[z - 1][y][x].a <= 0)
					{
						if (hasCube(x, y, z))
						{
							removeVoxel(x, y, z);
						}
						continue;
					}

					if (hasCube(x, y, z))
					{
						setVoxelColor(x, y, z, shift[z - 1][y][x]);
					}
					else
						addVoxel(x, y, z, shift[z - 1][y][x]);
				}
			}
		}
	}

	public void hack_shift_model_forwards()
	{
		Color[][][] shift = new Color[(int) size.z][(int) size.y][(int) size.x];
		Color old = null;

		for (int z = 0; z < size.z; z++)
		{
			for (int y = 0; y < size.y; y++)
			{
				for (int x = 0; x < size.x; x++)
				{
					old = cubecolor[z][y][x];
					shift[z][y][x] = new Color(old.r, old.g, old.b, old.a);
				}
			}
		}

		for (int y = 0; y < size.y; y++)
		{
			for (int x = 0; x < size.x; x++)
			{
				if (hasCube(x, y, (int) size.z - 1))
				{
					removeVoxel(x, y, (int) size.z - 1);
				}
			}
		}

		for (int z = 0; z < size.z - 1; z++)
		{
			for (int y = 0; y < size.y; y++)
			{
				for (int x = 0; x < size.x; x++)
				{
					if (shift[z + 1][y][x].a <= 0)
					{
						if (hasCube(x, y, z))
						{
							removeVoxel(x, y, z);
						}
						continue;
					}

					if (hasCube(x, y, z))
					{
						setVoxelColor(x, y, z, shift[z + 1][y][x]);
					}
					else
						addVoxel(x, y, z, shift[z + 1][y][x]);
				}
			}
		}
	}

	public void hack_shift_model_right()
	{
		Color[][][] shift = new Color[(int) size.z][(int) size.y][(int) size.x];
		Color old = null;

		for (int z = 0; z < size.z; z++)
		{
			for (int y = 0; y < size.y; y++)
			{
				for (int x = 0; x < size.x; x++)
				{
					old = cubecolor[z][y][x];
					shift[z][y][x] = new Color(old.r, old.g, old.b, old.a);
				}
			}
		}

		for (int z = 0; z < size.z; z++)
		{
			for (int y = 0; y < size.y; y++)
			{
				if (hasCube((int) size.x - 1, y, z))
				{
					removeVoxel((int) size.x - 1, y, z);
				}
			}
		}

		for (int z = 0; z < size.z; z++)
		{
			for (int y = 0; y < size.y; y++)
			{
				for (int x = 0; x < size.x - 1; x++)
				{
					if (shift[z][y][x + 1].a <= 0)
					{
						if (hasCube(x, y, z))
						{
							removeVoxel(x, y, z);
						}
						continue;
					}

					if (hasCube(x, y, z))
					{
						setVoxelColor(x, y, z, shift[z][y][x + 1]);
					}
					else
						addVoxel(x, y, z, shift[z][y][x + 1]);
				}
			}
		}
	}

	public void hack_shift_model_left()
	{
		Color[][][] shift = new Color[(int) size.z][(int) size.y][(int) size.x];
		Color old = null;

		for (int z = 0; z < size.z; z++)
		{
			for (int y = 0; y < size.y; y++)
			{
				for (int x = 0; x < size.x; x++)
				{
					old = cubecolor[z][y][x];
					shift[z][y][x] = new Color(old.r, old.g, old.b, old.a);
				}
			}
		}

		for (int z = 0; z < size.z; z++)
		{
			for (int y = 0; y < size.y; y++)
			{
				if (hasCube(0, y, z))
				{
					removeVoxel(0, y, z);
				}
			}
		}

		for (int z = 0; z < size.z; z++)
		{
			for (int y = 0; y < size.y; y++)
			{
				for (int x = (int) (size.x - 1); x > 0; x--)
				{
					if (shift[z][y][x - 1].a <= 0)
					{
						if (hasCube(x, y, z))
						{
							removeVoxel(x, y, z);
						}
						continue;
					}

					if (hasCube(x, y, z))
					{
						setVoxelColor(x, y, z, shift[z][y][x - 1]);
					}
					else
						addVoxel(x, y, z, shift[z][y][x - 1]);
				}
			}
		}
	}

	public void centerMatrixPosition()
	{
		this.pos.x = -(int) (size.x / 2f);
		this.pos.y = 0;
		this.pos.z = -(int) (size.z / 2f);
	}

	public void dispose()
	{
		front.dispose();
		back.dispose();
		top.dispose();
		bottom.dispose();
		left.dispose();
		right.dispose();

		GL15.glDeleteBuffers(this.indexbufferID);
	}
}
