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
						// exit no cube
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
		if (tempcolor.a != 0)
		{
			if (z + 1 < size.z)
			{
				if (cubecolor[z + 1][y][x].a == 0)
				{
					tempmask += SideUtil.getVisibilityMask(Side.FRONT);
				}
			}
			else
			{
				tempmask += SideUtil.getVisibilityMask(Side.FRONT);
			}

			if (z - 1 >= 0)
			{
				if (cubecolor[z - 1][y][x].a == 0)
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
					tempmask += SideUtil.getVisibilityMask(Side.RIGHT);
				}
			}
			else
			{
				tempmask += SideUtil.getVisibilityMask(Side.RIGHT);
			}

			if (x - 1 >= 0)
			{
				if (cubecolor[z][y][x - 1].a == 0)
				{
					tempmask += SideUtil.getVisibilityMask(Side.LEFT);
				}
			}
			else
			{
				tempmask += SideUtil.getVisibilityMask(Side.LEFT);
			}

			cubecolor[z][y][x].a = tempmask;
		}
		else
		{
			cubecolor[z][y][x].a = 0;
		}
	}

	public void updateVoxelData(int mask, int x, int y, int z)
	{
		// front
		if ((mask & 32) == 32)
		{

		}

		// back
		if ((mask & 64) == 64)
		{

		}

		// top
		if ((mask & 8) == 8)
		{
		}

		// bottom
		if ((mask & 16) == 16)
		{
		}

		// left
		if ((mask & 2) == 2)
		{
		}

		// right
		if ((mask & 4) == 4)
		{
		}
	}

	public RayHitPoint rayTest()
	{
		RayHitPoint rayhit = new RayHitPoint();
		rayhit.distance = 100000;

		Color color = null;
		int ci = 0;
		float dis = 0;

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
						// exit no cube
						continue;
					}
					else
					{

						// front
						if ((color.a & 32) == 32)
						{
							ci = front.cubeindexs[z][y][x];

							if (RaycastingUtil.rayTest(front.normal, front.vertexdata[ci], front.vertexdata[ci + 1],
									front.vertexdata[ci + 2], front.vertexdata[ci + 7], front.vertexdata[ci + 8],
									front.vertexdata[ci + 9], front.vertexdata[ci + 14], front.vertexdata[ci + 15],
									front.vertexdata[ci + 16]) != null
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

							if (RaycastingUtil.rayTest(back.normal, back.vertexdata[ci], back.vertexdata[ci + 1],
									back.vertexdata[ci + 2], back.vertexdata[ci + 7], back.vertexdata[ci + 8],
									back.vertexdata[ci + 9], back.vertexdata[ci + 14], back.vertexdata[ci + 15],
									back.vertexdata[ci + 16]) != null
									|| RaycastingUtil.rayTest(back.normal, back.vertexdata[ci],
											back.vertexdata[ci + 1], back.vertexdata[ci + 2], back.vertexdata[ci + 14],
											back.vertexdata[ci + 15], back.vertexdata[ci + 16],
											back.vertexdata[ci + 21], back.vertexdata[ci + 22],
											back.vertexdata[ci + 23]) != null)
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

							if (RaycastingUtil.rayTest(top.normal, top.vertexdata[ci], top.vertexdata[ci + 1],
									top.vertexdata[ci + 2], top.vertexdata[ci + 7], top.vertexdata[ci + 8],
									top.vertexdata[ci + 9], top.vertexdata[ci + 14], top.vertexdata[ci + 15],
									top.vertexdata[ci + 16]) != null
									|| RaycastingUtil.rayTest(top.normal, top.vertexdata[ci], top.vertexdata[ci + 1],
											top.vertexdata[ci + 2], top.vertexdata[ci + 14], top.vertexdata[ci + 15],
											top.vertexdata[ci + 16], top.vertexdata[ci + 21], top.vertexdata[ci + 22],
											top.vertexdata[ci + 23]) != null)
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

							if (RaycastingUtil.rayTest(bottom.normal, bottom.vertexdata[ci], bottom.vertexdata[ci + 1],
									bottom.vertexdata[ci + 2], bottom.vertexdata[ci + 7], bottom.vertexdata[ci + 8],
									bottom.vertexdata[ci + 9], bottom.vertexdata[ci + 14], bottom.vertexdata[ci + 15],
									bottom.vertexdata[ci + 16]) != null
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

							if (RaycastingUtil.rayTest(left.normal, left.vertexdata[ci], left.vertexdata[ci + 1],
									left.vertexdata[ci + 2], left.vertexdata[ci + 7], left.vertexdata[ci + 8],
									left.vertexdata[ci + 9], left.vertexdata[ci + 14], left.vertexdata[ci + 15],
									left.vertexdata[ci + 16]) != null
									|| RaycastingUtil.rayTest(left.normal, left.vertexdata[ci],
											left.vertexdata[ci + 1], left.vertexdata[ci + 2], left.vertexdata[ci + 14],
											left.vertexdata[ci + 15], left.vertexdata[ci + 16],
											left.vertexdata[ci + 21], left.vertexdata[ci + 22],
											left.vertexdata[ci + 23]) != null)
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

							if (RaycastingUtil.rayTest(right.normal, right.vertexdata[ci], right.vertexdata[ci + 1],
									right.vertexdata[ci + 2], right.vertexdata[ci + 7], right.vertexdata[ci + 8],
									right.vertexdata[ci + 9], right.vertexdata[ci + 14], right.vertexdata[ci + 15],
									right.vertexdata[ci + 16]) != null
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
		return rayhit.distance == 100000 ? null : rayhit;
	}

	public void addVoxel(int x, int y, int z, Color color)
	{
		cubedirty[z][y][x] = true;

		Color cubecolor = this.cubecolor[z][y][x];
		cubecolor.r = color.r;
		cubecolor.g = color.g;
		cubecolor.b = color.b;

		updateVisibilityMaskRelative(x, y, z);
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
		return size.x + "_" + size.y + "_" + size.z;
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
		System.out.print("qb matrix createzeroedcubes called \n");
		// for (int z = 0; z < sizeZ; z++)
		// {
		// for (int y = 0; y < sizeY; y++)
		// {
		// for (int x = 0; x < sizeX; x++)
		// {
		// cubes[z][y][x] = new Cube();
		// cubes[z][y][x].setPos(x, y, z);
		// cubes[z][y][x].setAlpha(0);
		// }
		// }
		// }
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

		return cubecolor[z][y][x].a > 1;
	}

	public void reSize(int x1, int y1, int z1)
	{
		// Cube[][][] cubes = new Cube[z1][y1][x1];
		// Color color = new Color();
		//
		// for (int z = 0; z < z1; z++)
		// {
		// for (int y = 0; y < y1; y++)
		// {
		// for (int x = 0; x < x1; x++)
		// {
		// cubes[z][y][x] = new Cube();
		// cubes[z][y][x].setPos(x, y, z);
		// cubes[z][y][x].setColor(0, 0, 0, 0);
		// cubes[z][y][x].setAlpha(0);
		// }
		// }
		// }
		//
		// for (int z = 0; z < sizeZ; z++)
		// {
		// for (int y = 0; y < sizeY; y++)
		// {
		// for (int x = 0; x < sizeX; x++)
		// {
		// if (z > z1 - 1)
		// continue;
		// if (y > y1 - 1)
		// continue;
		// if (x > x1 - 1)
		// continue;
		//
		// color = this.cubes[z][y][x].color;
		// cubes[z][y][x].setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		// }
		// }
		// }
		//
		// this.cubes = cubes;
		//
		// dispose();
		// this.sizeX = x1;
		// this.sizeY = y1;
		// this.sizeZ = z1;
		// vertexdata = new float[sizeX * sizeY * sizeZ * 28 * 6];
		// indexdata = new int[sizeX * sizeY * sizeZ * 12 * 6];
		// this.setPosition(0, 0, 0);
		// generateMesh();
		//
		// this.clean();
		//
		// Program.floor.updatemesh();
		// GUI.Broadcast(GUI.MESSAGE_QB_MATRIX_RESIZED, this.getSizeString(), 100000);
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
