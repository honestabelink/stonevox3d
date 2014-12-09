package stonevox.data;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import stonevox.Program;
import stonevox.util.GUI;
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

	private RayHitPoint rayhit;

	public Color[][][] cubecolor;
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
		rayhit = new RayHitPoint();

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

						// back
						if ((color.a & 64) == 64)
						{
							back.putVertexData(x, y, z, color);
						}

						// top
						if ((color.a & 8) == 8)
						{
							top.putVertexData(x, y, z, color);
						}

						// bottom
						if ((color.a & 16) == 16)
						{
							bottom.putVertexData(x, y, z, color);
						}

						// left
						if ((color.a & 2) == 2)
						{
							left.putVertexData(x, y, z, color);
						}

						// right
						if ((color.a & 4) == 4)
						{
							right.putVertexData(x, y, z, color);
						}
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
		Color color = null;

		int mask = 1;

		for (int z = 0; z < size.z; z++)
		{
			for (int y = 0; y < size.y; y++)
			{
				for (int x = 0; x < size.x; x++)
				{

					color = cubecolor[z][y][x];
					mask = 1;
					if (color.a != 0)
					{
						if (z + 1 < size.z)
						{
							if (cubecolor[z + 1][y][x].a == 0)
							{
								mask += SideUtil.getVisibilityMask(Side.FRONT);
							}
						}
						else
						{
							mask += SideUtil.getVisibilityMask(Side.FRONT);
						}

						if (z - 1 >= 0)
						{
							if (cubecolor[z - 1][y][x].a == 0)
							{
								mask += SideUtil.getVisibilityMask(Side.BACK);
							}
						}
						else
						{
							mask += SideUtil.getVisibilityMask(Side.BACK);
						}

						if (y + 1 < size.y)
						{
							if (cubecolor[z][y + 1][x].a == 0)
							{
								mask += SideUtil.getVisibilityMask(Side.TOP);
							}
						}
						else
						{
							mask += SideUtil.getVisibilityMask(Side.TOP);
						}

						if (y - 1 >= 0)
						{
							if (cubecolor[z][y - 1][x].a == 0)
							{
								mask += SideUtil.getVisibilityMask(Side.BOTTOM);
							}
						}
						else
						{
							mask += SideUtil.getVisibilityMask(Side.BOTTOM);
						}

						if (x + 1 < size.x)
						{
							if (cubecolor[z][y][x + 1].a == 0)
							{
								mask += SideUtil.getVisibilityMask(Side.RIGHT);
							}
						}
						else
						{
							mask += SideUtil.getVisibilityMask(Side.RIGHT);
						}

						if (x - 1 >= 0)
						{
							if (cubecolor[z][y][x - 1].a == 0)
							{
								mask += SideUtil.getVisibilityMask(Side.LEFT);
							}
						}
						else
						{
							mask += SideUtil.getVisibilityMask(Side.LEFT);
						}

						cubecolor[z][y][x].a = mask;
					}
					else
					{
						cubecolor[z][y][x].a = 0;
					}
				}
			}
		}
	}

	public RayHitPoint rayTest(Vector3 origin, Vector3 projection)
	{
		rayhit.cubelocation.y = -10000;
		return rayhit;
	}

	public void render()
	{
		if (visible)
		{
			final_transform = Matrix.Multiply(Program.camera.modelview, transform);

			Program.shader.WriteUniformMatrix4("modelview\0", final_transform.GetBuffer());

			if (front.normal.dot(Program.camera.direction) < 0)
			{
				GL30.glBindVertexArray(front.vertexobjectbufferid);
				GL11.glDrawElements(GL11.GL_TRIANGLES, front.facecount * 6, GL11.GL_UNSIGNED_INT, 0l);
				GL30.glBindVertexArray(0);
			}
			if (back.normal.dot(Program.camera.direction) < 0)
			{
				GL30.glBindVertexArray(back.vertexobjectbufferid);
				GL11.glDrawElements(GL11.GL_TRIANGLES, back.facecount * 6, GL11.GL_UNSIGNED_INT, 0l);
				GL30.glBindVertexArray(0);
			}
			if (top.normal.dot(Program.camera.direction) < .12)
			{
				GL30.glBindVertexArray(top.vertexobjectbufferid);
				GL11.glDrawElements(GL11.GL_TRIANGLES, top.facecount * 6, GL11.GL_UNSIGNED_INT, 0l);
				GL30.glBindVertexArray(0);
			}
			if (bottom.normal.dot(Program.camera.direction) < -.018f)
			{
				GL30.glBindVertexArray(bottom.vertexobjectbufferid);
				GL11.glDrawElements(GL11.GL_TRIANGLES, bottom.facecount * 6, GL11.GL_UNSIGNED_INT, 0l);
				GL30.glBindVertexArray(0);
			}
			if (left.normal.dot(Program.camera.direction) < 0)
			{
				GL30.glBindVertexArray(left.vertexobjectbufferid);
				GL11.glDrawElements(GL11.GL_TRIANGLES, left.facecount * 6, GL11.GL_UNSIGNED_INT, 0l);
				GL30.glBindVertexArray(0);
			}
			if (right.normal.dot(Program.camera.direction) < 0)
			{
				GL30.glBindVertexArray(right.vertexobjectbufferid);
				GL11.glDrawElements(GL11.GL_TRIANGLES, right.facecount * 6, GL11.GL_UNSIGNED_INT, 0l);
				GL30.glBindVertexArray(0);
			}
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
		System.out.print("qb matrix clean called \n");
		// for (int z = 0; z < sizeZ; z++)
		// {
		// for (int y = 0; y < sizeY; y++)
		// {
		// for (int x = 0; x < sizeX; x++)
		// {
		// cubes[z][y][x].isDirty = false;
		// }
		// }
		// }
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
