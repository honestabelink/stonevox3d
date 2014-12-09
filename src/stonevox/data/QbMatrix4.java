package stonevox.data;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import stonevox.Program;
import stonevox.util.GUI;
import stonevox.util.SideUtil;

public class QbMatrix4
{
	float cubesizex = 0.5f;
	float cubesizey = 0.5f;
	float cubesizez = 0.5f;

	private String name;
	public int sizeX;
	public int sizeY;
	public int sizeZ;
	public int posX;
	public int posY;
	public int posZ;
	public Vector3 pos;
	public Vector3 hackpos;
	public Vector3 pos_size;

	// private int vertexobjectarrayid;
	// private int vertexbufferid;
	// private int indexbufferid;
	// private Matrix transform;
	// private Matrix final_transform;

	private RayHitPoint rayhit;

	public Cube[][][] cubes;
	// float[] vertexdata;
	// int[] indexdata;

	public boolean visible = true;

	// refactor

	public Color[][][] cubecolors;
	public int[][][] cubeindexs;
	public boolean[][][] cubedirtys;
	public float[] cubelightmaps;

	public int cubeindex;

	private int facecount;

	private float[] vertexdata;
	private FloatBuffer vertexbufferdata;
	private IntBuffer indexbufferdata;

	private int vertexobjectarrayid;
	private int vertexbufferid;
	private int indexbufferid;
	private Matrix transform;
	private Matrix final_transform;

	private float[] tempdata = new float[32];

	// side order
	// back
	// front
	// top
	// bottom
	// right
	// left

	public void setSize(int x, int y, int z)
	{
		// cubes = new Cube[z][y][x];
		this.sizeX = x;
		this.sizeY = y;
		this.sizeZ = z;

		// vertexdata = new float[sizeX * sizeY * sizeZ * 28 * 6];
		// indexdata = new int[sizeX * sizeY * sizeZ * 36]; // *12*6

		cubecolors = new Color[sizeZ][sizeY][sizeX];

		cubeindexs = new int[sizeZ][sizeY][sizeX];

		for (int[][] innerRow : cubeindexs)
		{
			for (int[] innerInnerRow : innerRow)
			{
				Arrays.fill(innerInnerRow, -1);
			}
		}

		cubedirtys = new boolean[sizeX][sizeY][sizeX];
		cubelightmaps = new float[sizeZ * sizeY * sizeX * 4 * 6];
		Arrays.fill(cubelightmaps, 1f);

		vertexbufferdata = BufferUtils.createFloatBuffer(sizeX * sizeY * sizeZ * 28 * 6);
		indexbufferdata = BufferUtils.createIntBuffer(sizeX * sizeY * sizeZ * 36);

		for (int i = 0; i < sizeX * sizeY * sizeZ * 6; i++)
		{
			indexbufferdata.put(i * 4);
			indexbufferdata.put(i * 4 + 1);
			indexbufferdata.put(i * 4 + 2);

			indexbufferdata.put(i * 4);
			indexbufferdata.put(i * 4 + 2);
			indexbufferdata.put(i * 4 + 3);
		}

		indexbufferdata.flip();
	}

	public void setPosition(int x, int y, int z)
	{
		this.posX = (int) x;
		this.posY = (int) y;
		this.posZ = (int) z;

		hackpos = new Vector3();
		pos = new Vector3(x * .5f, y * .5f, z * .5f);
		pos_size = new Vector3((float) (sizeX) * .5f - .5f, (float) (sizeY) * .5f - .5f, (float) (sizeZ) * .5f - .5f);

		transform = Matrix.CreateTranslation(0, 0, 0);
	}

	public Cube getCube(Vector3 v)
	{
		return cubes[(int) v.z][(int) v.y][(int) v.x];
	}

	public Cube getCubeSaftly(int x, int y, int z)
	{
		if (z > sizeZ - 1)
			return null;
		if (z < 0)
			return null;

		if (y > sizeY - 1)
			return null;
		if (y < 0)
			return null;

		if (x > sizeX - 1)
			return null;
		if (x < 0)
			return null;

		return cubes[z][y][x];
	}

	public Cube getCubeSaftly(Vector3 l)
	{
		int x = (int) l.x;
		int y = (int) l.y;
		int z = (int) l.z;
		if (z > sizeZ - 1)
			return null;
		if (z < 0)
			return null;

		if (y > sizeY - 1)
			return null;
		if (y < 0)
			return null;

		if (x > sizeX - 1)
			return null;
		if (x < 0)
			return null;

		return cubes[z][y][x];
	}

	public void generateMesh()
	{
		long time = Sys.getTime();
		float size = .5f;

		cubeindex = 0;

		Color c = null;

		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					c = cubecolors[z][y][x];

					// no cube
					if (c.a <= 1)
					{
						// exit no cube
						continue;
					}
					else
					{
						cubeindexs[z][y][x] = cubeindex;

						// back
						generateFrontFace(x, y, z, c);
						vertexbufferdata.put(tempdata);
						facecount++;

						// front
						generateBackFace(x, y, z, c);
						vertexbufferdata.put(tempdata);
						facecount++;

						// top
						generateTopFace(x, y, z, c);
						vertexbufferdata.put(tempdata);
						facecount++;

						// bottom
						generateBottomFace(x, y, z, c);
						vertexbufferdata.put(tempdata);
						facecount++;

						// right
						generateLeftFace(x, y, z, c);
						vertexbufferdata.put(tempdata);
						facecount++;

						// left
						generateRightFace(x, y, z, c);
						vertexbufferdata.put(tempdata);
						facecount++;

						cubeindex++;

						// everything reversed other than top/bottom ??

						// if (mask == 0) // voxel invisble
						// if (mask && 32 == 32) // front side visible
						// if (mask && 64 == 64) // back side visible
						// if (mask && 8 == 8) // top side visible
						// if (mask && 16 == 16) // bottom side visible
						// if (mask && 2 == 2) // left side visible
						// if (mask && 4 == 4) // right side visible
					}
				}
			}
		}

		vertexbufferdata.flip();
		vertexbufferdata.position(0);

		vertexdata = new float[vertexbufferdata.limit()];
		vertexbufferdata.get(vertexdata);
		vertexbufferdata.flip();

		vertexbufferid = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexbufferid);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexbufferdata, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		indexbufferid = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbufferid);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbufferdata, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		vertexobjectarrayid = GL30.glGenVertexArrays();

		GL30.glBindVertexArray(vertexobjectarrayid);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexbufferid);
		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 8 << 2, 0l);
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 8 << 2, 3 << 2);
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, 8 << 2, 7 << 2);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbufferid);
		GL30.glBindVertexArray(0);

		long netime = Sys.getTime();

		if (Program.debug)
			System.out.print(this.name + " : Mesh Generation with lighting - "
					+ (netime - time + " mil" + System.lineSeparator()));
	}

	public int getBufferIndex()
	{
		return cubeindex * 32 * 6;
	}

	public int getLightMapIndex()
	{
		return cubeindex * 4;
	}

	private void generateBackFace(int x, int y, int z, Color color)
	{
		int cubelightmapindex = getLightMapIndex() + 4 * 0;
		float alpha = color.a > 1 ? 1 : 0;

		tempdata[0] = -.5f + x;
		tempdata[1] = -.5f + y;
		tempdata[2] = -.5f + z;
		tempdata[3] = color.r;
		tempdata[4] = color.g;
		tempdata[5] = color.b;
		tempdata[6] = color.a;
		tempdata[7] = cubelightmaps[cubelightmapindex];

		tempdata[8] = .5f + x;
		tempdata[9] = -.5f + y;
		tempdata[10] = -.5f + z;
		tempdata[11] = color.r;
		tempdata[12] = color.g;
		tempdata[13] = color.b;
		tempdata[14] = color.a;
		tempdata[15] = cubelightmaps[1 + cubelightmapindex];

		tempdata[16] = .5f + x;
		tempdata[17] = .5f + y;
		tempdata[18] = -.5f + z;
		tempdata[19] = color.r;
		tempdata[20] = color.g;
		tempdata[21] = color.b;
		tempdata[22] = color.a;
		tempdata[23] = cubelightmaps[2 + cubelightmapindex];

		tempdata[24] = -.5f + x;
		tempdata[25] = .5f + y;
		tempdata[26] = -.5f + z;
		tempdata[27] = color.r;
		tempdata[28] = color.g;
		tempdata[29] = color.b;
		tempdata[30] = color.a;
		tempdata[31] = cubelightmaps[3 + cubelightmapindex];
	}

	private void generateFrontFace(int x, int y, int z, Color color)
	{
		int cubelightmapindex = getLightMapIndex() + 4 * 1;

		tempdata[0] = -.5f + x;
		tempdata[1] = -.5f + y;
		tempdata[2] = .5f + z;
		tempdata[3] = color.r;
		tempdata[4] = color.g;
		tempdata[5] = color.b;
		tempdata[6] = color.a;
		tempdata[7] = cubelightmaps[cubelightmapindex];

		tempdata[8] = .5f + x;
		tempdata[9] = -.5f + y;
		tempdata[10] = .5f + z;
		tempdata[11] = color.r;
		tempdata[12] = color.g;
		tempdata[13] = color.b;
		tempdata[14] = color.a;
		tempdata[15] = cubelightmaps[1 + cubelightmapindex];

		tempdata[16] = .5f + x;
		tempdata[17] = .5f + y;
		tempdata[18] = .5f + z;
		tempdata[19] = color.r;
		tempdata[20] = color.g;
		tempdata[21] = color.b;
		tempdata[22] = color.a;
		tempdata[23] = cubelightmaps[2 + cubelightmapindex];

		tempdata[24] = -.5f + x;
		tempdata[25] = .5f + y;
		tempdata[26] = .5f + z;
		tempdata[27] = color.r;
		tempdata[28] = color.g;
		tempdata[29] = color.b;
		tempdata[30] = color.a;
		tempdata[31] = cubelightmaps[3 + cubelightmapindex];

	}

	private void generateTopFace(int x, int y, int z, Color color)
	{
		int cubelightmapindex = getLightMapIndex() + 4 * 2;

		tempdata[0] = -.5f + x;
		tempdata[1] = .5f + y;
		tempdata[2] = .5f + z;
		tempdata[3] = color.r;
		tempdata[4] = color.g;
		tempdata[5] = color.b;
		tempdata[6] = color.a;
		tempdata[7] = cubelightmaps[cubelightmapindex];

		tempdata[8] = .5f + x;
		tempdata[9] = .5f + y;
		tempdata[10] = .5f + z;
		tempdata[11] = color.r;
		tempdata[12] = color.g;
		tempdata[13] = color.b;
		tempdata[14] = color.a;
		tempdata[15] = cubelightmaps[1 + cubelightmapindex];

		tempdata[16] = .5f + x;
		tempdata[17] = .5f + y;
		tempdata[18] = -.5f + z;
		tempdata[19] = color.r;
		tempdata[20] = color.g;
		tempdata[21] = color.b;
		tempdata[22] = color.a;
		tempdata[23] = cubelightmaps[2 + cubelightmapindex];

		tempdata[24] = -.5f + x;
		tempdata[25] = .5f + y;
		tempdata[26] = -.5f + z;
		tempdata[27] = color.r;
		tempdata[28] = color.g;
		tempdata[29] = color.b;
		tempdata[30] = color.a;
		tempdata[31] = cubelightmaps[3 + cubelightmapindex];
	}

	private void generateBottomFace(int x, int y, int z, Color color)
	{
		int cubelightmapindex = getLightMapIndex() + 4 * 3;

		tempdata[0] = -.5f + x;
		tempdata[1] = -.5f + y;
		tempdata[2] = .5f + z;
		tempdata[3] = color.r;
		tempdata[4] = color.g;
		tempdata[5] = color.b;
		tempdata[6] = color.a;
		tempdata[7] = cubelightmaps[cubelightmapindex];

		tempdata[8] = .5f + x;
		tempdata[9] = -.5f + y;
		tempdata[10] = .5f + z;
		tempdata[11] = color.r;
		tempdata[12] = color.g;
		tempdata[13] = color.b;
		tempdata[14] = color.a;
		tempdata[15] = cubelightmaps[1 + cubelightmapindex];

		tempdata[16] = .5f + x;
		tempdata[17] = -.5f + y;
		tempdata[18] = -.5f + z;
		tempdata[19] = color.r;
		tempdata[20] = color.g;
		tempdata[21] = color.b;
		tempdata[22] = color.a;
		tempdata[23] = cubelightmaps[2 + cubelightmapindex];

		tempdata[24] = -.5f + x;
		tempdata[25] = -.5f + y;
		tempdata[26] = -.5f + z;
		tempdata[27] = color.r;
		tempdata[28] = color.g;
		tempdata[29] = color.b;
		tempdata[30] = color.a;
		tempdata[31] = cubelightmaps[3 + cubelightmapindex];
	}

	private void generateRightFace(int x, int y, int z, Color color)
	{
		int cubelightmapindex = getLightMapIndex() + 4 * 4;

		tempdata[0] = .5f + x;
		tempdata[1] = -.5f + y;
		tempdata[2] = -.5f + z;
		tempdata[3] = color.r;
		tempdata[4] = color.g;
		tempdata[5] = color.b;
		tempdata[6] = color.a;
		tempdata[7] = cubelightmaps[cubelightmapindex];

		tempdata[8] = .5f + x;
		tempdata[9] = -.5f + y;
		tempdata[10] = .5f + z;
		tempdata[11] = color.r;
		tempdata[12] = color.g;
		tempdata[13] = color.b;
		tempdata[14] = color.a;
		tempdata[15] = cubelightmaps[1 + cubelightmapindex];

		tempdata[16] = .5f + x;
		tempdata[17] = .5f + y;
		tempdata[18] = .5f + z;
		tempdata[19] = color.r;
		tempdata[20] = color.g;
		tempdata[21] = color.b;
		tempdata[22] = color.a;
		tempdata[23] = cubelightmaps[2 + cubelightmapindex];

		tempdata[24] = .5f + x;
		tempdata[25] = .5f + y;
		tempdata[26] = -.5f + z;
		tempdata[27] = color.r;
		tempdata[28] = color.g;
		tempdata[29] = color.b;
		tempdata[30] = color.a;
		tempdata[31] = cubelightmaps[3 + cubelightmapindex];
	}

	private void generateLeftFace(int x, int y, int z, Color color)
	{
		int cubelightmapindex = getLightMapIndex() + 4 * 5;

		tempdata[0] = -.5f + x;
		tempdata[1] = -.5f + y;
		tempdata[2] = -.5f + z;
		tempdata[3] = color.r;
		tempdata[4] = color.g;
		tempdata[5] = color.b;
		tempdata[6] = color.a;
		tempdata[7] = cubelightmaps[cubelightmapindex];

		tempdata[8] = -.5f + x;
		tempdata[9] = -.5f + y;
		tempdata[10] = .5f + z;
		tempdata[11] = color.r;
		tempdata[12] = color.g;
		tempdata[13] = color.b;
		tempdata[14] = color.a;
		tempdata[15] = cubelightmaps[1 + cubelightmapindex];

		tempdata[16] = -.5f + x;
		tempdata[17] = .5f + y;
		tempdata[18] = .5f + z;
		tempdata[19] = color.r;
		tempdata[20] = color.g;
		tempdata[21] = color.b;
		tempdata[22] = color.a;
		tempdata[23] = cubelightmaps[2 + cubelightmapindex];

		tempdata[24] = -.5f + x;
		tempdata[25] = .5f + y;
		tempdata[26] = -.5f + z;
		tempdata[27] = color.r;
		tempdata[28] = color.g;
		tempdata[29] = color.b;
		tempdata[30] = color.a;
		tempdata[31] = cubelightmaps[3 + cubelightmapindex];

	}

	// public void generateMesh()
	// {
	// long time = Sys.getTime();
	// Cube cube = null;
	// Color color = null;
	// int index = 0;
	//
	// facecount = 0;
	//
	// for (int z = 0; z < sizeZ; z++)
	// {
	// for (int y = 0; y < sizeY; y++)
	// {
	// for (int x = 0; x < sizeX; x++)
	// {
	// cube = cubes[z][y][x];
	// color = cube.color;
	// if (color.getAlpha() != 0)
	// {
	// if (z + 1 < sizeZ)
	// {
	// if (cubes[z + 1][y][x].color.getAlpha() == 0)
	// {
	// updateLightMapRelative(cube, Side.FRONT, x, y, z);
	// index = cube.front.setDataIntoBuffer(index, vertexdata);
	// facecount++;
	// }
	// }
	// else
	// {
	// updateLightMapRelative(cube, Side.FRONT, x, y, z);
	// index = cube.front.setDataIntoBuffer(index, vertexdata);
	// facecount++;
	// }
	//
	// if (z - 1 >= 0)
	// {
	// if (cubes[z - 1][y][x].color.getAlpha() == 0)
	// {
	// updateLightMapRelative(cube, Side.BACK, x, y, z);
	// index = cube.back.setDataIntoBuffer(index, vertexdata);
	// facecount++;
	// }
	// }
	// else
	// {
	// updateLightMapRelative(cube, Side.BACK, x, y, z);
	// index = cube.back.setDataIntoBuffer(index, vertexdata);
	// facecount++;
	// }
	//
	// if (y + 1 < sizeY)
	// {
	// if (cubes[z][y + 1][x].color.getAlpha() == 0)
	// {
	// updateLightMapRelative(cube, Side.TOP, x, y, z);
	// index = cube.top.setDataIntoBuffer(index, vertexdata);
	// facecount++;
	// }
	// }
	// else
	// {
	// updateLightMapRelative(cube, Side.TOP, x, y, z);
	// index = cube.top.setDataIntoBuffer(index, vertexdata);
	// facecount++;
	// }
	//
	// if (y - 1 >= 0)
	// {
	// if (cubes[z][y - 1][x].color.getAlpha() == 0)
	// {
	// updateLightMapRelative(cube, Side.BOTTOM, x, y, z);
	// index = cube.bottom.setDataIntoBuffer(index, vertexdata);
	// facecount++;
	// }
	// }
	// else
	// {
	// updateLightMapRelative(cube, Side.BOTTOM, x, y, z);
	// index = cube.bottom.setDataIntoBuffer(index, vertexdata);
	// facecount++;
	// }
	//
	// if (x + 1 < sizeX)
	// {
	// if (cubes[z][y][x + 1].color.getAlpha() == 0)
	// {
	// updateLightMapRelative(cube, Side.RIGHT, x, y, z);
	// index = cube.right.setDataIntoBuffer(index, vertexdata);
	// facecount++;
	// }
	// }
	// else
	// {
	// updateLightMapRelative(cube, Side.RIGHT, x, y, z);
	// index = cube.right.setDataIntoBuffer(index, vertexdata);
	// facecount++;
	// }
	//
	// if (x - 1 >= 0)
	// {
	// if (cubes[z][y][x - 1].color.getAlpha() == 0)
	// {
	// updateLightMapRelative(cube, Side.LEFT, x, y, z);
	// index = cube.left.setDataIntoBuffer(index, vertexdata);
	// facecount++;
	// }
	// }
	// else
	// {
	// updateLightMapRelative(cube, Side.LEFT, x, y, z);
	// index = cube.left.setDataIntoBuffer(index, vertexdata);
	// facecount++;
	// }
	// }
	// }
	// }
	// }
	//
	// FloatBuffer vertexbuff = BufferUtils.createFloatBuffer(facecount * 28);
	//
	// vertexbuff.put(vertexdata, 0, facecount * 28);
	// vertexbuff.flip();
	//
	// vertexbufferid = GL15.glGenBuffers();
	// GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexbufferid);
	// GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexbuff, GL15.GL_DYNAMIC_DRAW);
	// GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	//
	// IntBuffer indexbuff = BufferUtils.createIntBuffer(facecount * 6);
	//
	// for (int i = 0; i < facecount; i++)
	// {
	// indexdata[i * 6] = i * 4;
	// indexdata[i * 6 + 1] = i * 4 + 1;
	// indexdata[i * 6 + 2] = i * 4 + 2;
	//
	// indexdata[i * 6 + 3] = i * 4;
	// indexdata[i * 6 + 4] = i * 4 + 2;
	// indexdata[i * 6 + 5] = i * 4 + 3;
	// }
	//
	// indexbuff.put(indexdata, 0, facecount * 6);
	// indexbuff.flip();
	//
	// indexbufferid = GL15.glGenBuffers();
	// GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbufferid);
	// GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbuff, GL15.GL_DYNAMIC_DRAW);
	// GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
	//
	// vertexobjectarrayid = GL30.glGenVertexArrays();
	//
	// GL30.glBindVertexArray(vertexobjectarrayid);
	// GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexbufferid);
	// GL20.glEnableVertexAttribArray(0);
	// GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 7 << 2, 0l);
	// GL20.glEnableVertexAttribArray(1);
	// GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 7 << 2, 3 << 2);
	// GL20.glEnableVertexAttribArray(2);
	// GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, 7 << 2, 6 << 2);
	// GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbufferid);
	// GL30.glBindVertexArray(0);
	//
	// long netime = Sys.getTime();
	//
	// if (Program.debug)
	// System.out.print(this.name + " : Mesh Generation with lighting - "
	// + (netime - time + " mil" + System.lineSeparator()));
	// }

	public void updateMesh()
	{
		// long time = Program.getTime();
		//
		// Cube cube = null;
		// Color color = null;
		// int index = 0;
		//
		// facecount = 0;
		//
		// for (int z = 0; z < sizeZ; z++)
		// {
		// for (int y = 0; y < sizeY; y++)
		// {
		// for (int x = 0; x < sizeX; x++)
		// {
		// cube = cubes[z][y][x];
		// color = cube.color;
		// if (color.getAlpha() != 0)
		// {
		// if (z + 1 < sizeZ)
		// {
		// if (cubes[z + 1][y][x].color.getAlpha() == 0)
		// {
		// index = cube.front.setDataIntoBuffer(index, vertexdata);
		// facecount++;
		// }
		// }
		// else
		// {
		// index = cube.front.setDataIntoBuffer(index, vertexdata);
		// facecount++;
		// }
		//
		// if (z - 1 >= 0)
		// {
		// if (cubes[z - 1][y][x].color.getAlpha() == 0)
		// {
		// index = cube.back.setDataIntoBuffer(index, vertexdata);
		// facecount++;
		// }
		// }
		// else
		// {
		// index = cube.back.setDataIntoBuffer(index, vertexdata);
		// facecount++;
		// }
		//
		// if (y + 1 < sizeY)
		// {
		// if (cubes[z][y + 1][x].color.getAlpha() == 0)
		// {
		// index = cube.top.setDataIntoBuffer(index, vertexdata);
		// facecount++;
		// }
		// }
		// else
		// {
		// index = cube.top.setDataIntoBuffer(index, vertexdata);
		// facecount++;
		// }
		//
		// if (y - 1 >= 0)
		// {
		// if (cubes[z][y - 1][x].color.getAlpha() == 0)
		// {
		// index = cube.bottom.setDataIntoBuffer(index, vertexdata);
		// facecount++;
		// }
		// }
		// else
		// {
		// index = cube.bottom.setDataIntoBuffer(index, vertexdata);
		// facecount++;
		// }
		//
		// if (x + 1 < sizeX)
		// {
		// if (cubes[z][y][x + 1].color.getAlpha() == 0)
		// {
		// index = cube.right.setDataIntoBuffer(index, vertexdata);
		// facecount++;
		// }
		// }
		// else
		// {
		// index = cube.right.setDataIntoBuffer(index, vertexdata);
		// facecount++;
		// }
		//
		// if (x - 1 >= 0)
		// {
		// if (cubes[z][y][x - 1].color.getAlpha() == 0)
		// {
		// index = cube.left.setDataIntoBuffer(index, vertexdata);
		// facecount++;
		// }
		// }
		// else
		// {
		// index = cube.left.setDataIntoBuffer(index, vertexdata);
		// facecount++;
		// }
		// }
		// }
		// }
		// }
		//
		// FloatBuffer vertexbuff = BufferUtils.createFloatBuffer(facecount * 28);
		//
		// vertexbuff.put(vertexdata, 0, facecount * 28);
		// vertexbuff.flip();
		//
		// GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexbufferid);
		// GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexbuff, GL15.GL_DYNAMIC_DRAW);
		// GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		//
		// IntBuffer indexbuff = BufferUtils.createIntBuffer(facecount * 6);
		//
		// for (int i = 0; i < facecount; i++)
		// {
		// indexdata[i * 6] = i * 4;
		// indexdata[i * 6 + 1] = i * 4 + 1;
		// indexdata[i * 6 + 2] = i * 4 + 2;
		//
		// indexdata[i * 6 + 3] = i * 4;
		// indexdata[i * 6 + 4] = i * 4 + 2;
		// indexdata[i * 6 + 5] = i * 4 + 3;
		// }
		//
		// indexbuff.put(indexdata, 0, facecount * 6);
		// indexbuff.flip();
		//
		// GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbufferid);
		// GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbuff, GL15.GL_DYNAMIC_DRAW);
		// GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		//
		// long netime = Program.getTime();
		//
		// if (Program.debug)
		// System.out.print(netime - time + System.lineSeparator());
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

	public void render()
	{
		if (visible)
		{
			final_transform = Matrix.Multiply(Program.camera.modelview, transform);

			Program.shader.WriteUniformMatrix4("modelview\0", final_transform.GetBuffer());

			GL30.glBindVertexArray(vertexobjectarrayid);
			GL11.glDrawElements(GL11.GL_TRIANGLES, facecount * 6, GL11.GL_UNSIGNED_INT, 0l);
			GL30.glBindVertexArray(0);
		}
	}

	// SPAGHETTI
	public RayHitPoint rayTest(Vector3 origin, Vector3 projection)
	{
		Cube cube = null;
		Color color = null;

		float dis = 0;
		boolean allowdirt = Program.rayCaster.raycast_dirt;
		RayHitPoint hit = new RayHitPoint();
		hit.distance = 100000;

		return hit;
	}

	// if (!visible)
	// return hit;

	// if (allowdirt)
	// {
	// for (int z = 0; z < sizeZ; z++)
	// {
	// for (int y = 0; y < sizeY; y++)
	// {
	// for (int x = 0; x < sizeX; x++)
	// {
	// cube = cubes[z][y][x];
	// color = cube.color;
	//
	// if (cube.isDirty || color.getAlpha() != 0)
	// {
	// if (z + 1 < sizeZ)
	// {
	// if (cubes[z + 1][y][x].color.getAlpha() == 0)
	// {
	// if (cube.front.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f, y * .5f, z * .5f + .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.front.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	// }
	// else
	// {
	// if (cube.front.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f, y * .5f, z * .5f + .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.front.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	//
	// if (z - 1 >= 0)
	// {
	// if (cubes[z - 1][y][x].color.getAlpha() == 0)
	// {
	// if (cube.back.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f, y * .5f, z * .5f - .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.back.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	// }
	// else
	// {
	// if (cube.back.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f, y * .5f, z * .5f - .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.back.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	//
	// if (y + 1 < sizeY)
	// {
	// if (cubes[z][y + 1][x].color.getAlpha() == 0)
	// {
	// if (cube.top.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f, y * .5f + .5f, z * .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.top.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	// }
	// else
	// {
	// if (cube.top.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f, y * .5f + .5f, z * .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.top.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	//
	// if (y - 1 >= 0)
	// {
	// if (cubes[z][y - 1][x].color.getAlpha() == 0)
	// {
	// if (cube.bottom.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f, y * .5f - .5f, z * .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.bottom.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	// }
	// else
	// {
	// if (cube.bottom.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f, y * .5f - .5f, z * .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.bottom.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	//
	// if (x + 1 < sizeX)
	// {
	// if (cubes[z][y][x + 1].color.getAlpha() == 0)
	// {
	// if (cube.right.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f + .5f, y * .5f, z * .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.right.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	// }
	// else
	// {
	// if (cube.right.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f + .5f, y * .5f, z * .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.right.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	//
	// if (x - 1 >= 0)
	// {
	// if (cubes[z][y][x - 1].color.getAlpha() == 0)
	// {
	// if (cube.left.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f - .5f, y * .5f, z * .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.left.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	// }
	// else
	// {
	// if (cube.left.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f - .5f, y * .5f, z * .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.left.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	// else
	// {
	// for (int z = 0; z < sizeZ; z++)
	// {
	// for (int y = 0; y < sizeY; y++)
	// {
	// for (int x = 0; x < sizeX; x++)
	// {
	// cube = cubes[z][y][x];
	// color = cube.color;
	//
	// if (cube.isDirty)
	// continue;
	//
	// if (color.getAlpha() != 0)
	// {
	// if (z + 1 < sizeZ)
	// {
	// if (cubes[z + 1][y][x].color.getAlpha() == 0 || cubes[z + 1][y][x].isDirty)
	// {
	// if (cube.front.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f, y * .5f, z * .5f + .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.front.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	// }
	// else
	// {
	// if (cube.front.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f, y * .5f, z * .5f + .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.front.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	//
	// if (z - 1 >= 0)
	// {
	// if (cubes[z - 1][y][x].color.getAlpha() == 0 || cubes[z - 1][y][x].isDirty)
	// {
	// if (cube.back.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f, y * .5f, z * .5f - .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.back.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	// }
	// else
	// {
	// if (cube.back.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f, y * .5f, z * .5f - .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.back.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	//
	// if (y + 1 < sizeY)
	// {
	// if (cubes[z][y + 1][x].color.getAlpha() == 0 || cubes[z][y + 1][x].isDirty)
	// {
	// if (cube.top.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f, y * .5f + .5f, z * .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.top.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	// }
	// else
	// {
	// if (cube.top.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f, y * .5f + .5f, z * .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.top.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	//
	// if (y - 1 >= 0)
	// {
	// if (cubes[z][y - 1][x].color.getAlpha() == 0 || cubes[z][y - 1][x].isDirty)
	// {
	// if (cube.bottom.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f, y * .5f - .5f, z * .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.bottom.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	// }
	// else
	// {
	// if (cube.bottom.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f, y * .5f - .5f, z * .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.bottom.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	//
	// if (x + 1 < sizeX)
	// {
	// if (cubes[z][y][x + 1].color.getAlpha() == 0 || cubes[z][y][x + 1].isDirty)
	// {
	// if (cube.right.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f + .5f, y * .5f, z * .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.right.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	// }
	// else
	// {
	// if (cube.right.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f + .5f, y * .5f, z * .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.right.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	//
	// if (x - 1 >= 0)
	// {
	// if (cubes[z][y][x - 1].color.getAlpha() == 0 || cubes[z][y][x - 1].isDirty)
	// {
	// if (cube.left.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f - .5f, y * .5f, z * .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.left.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	// }
	// else
	// {
	// if (cube.left.RayTest(origin, projection))
	// {
	// dis = RaycastingUtil.Distance(origin, x * .5f - .5f, y * .5f, z * .5f);
	//
	// if (dis < hit.distance)
	// {
	// hit.cubelocation.x = x;
	// hit.cubelocation.y = y;
	// hit.cubelocation.z = z;
	// hit.cubenormal = cube.left.normal;
	//
	// hit.distance = dis;
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	//
	// return hit.distance == 100000 ? null : hit;
	// }
	//
	public void encodeVisibilityMask()
	{
		Color color = null;

		int mask = 1;

		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{

					color = cubecolors[z][y][x];
					mask = 1;
					if (color.a != 0)
					{
						if (z + 1 < sizeZ)
						{
							if (cubecolors[z + 1][y][x].a == 0)
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
							if (cubecolors[z - 1][y][x].a == 0)
							{
								mask += SideUtil.getVisibilityMask(Side.BACK);
							}
						}
						else
						{
							mask += SideUtil.getVisibilityMask(Side.BACK);
						}

						if (y + 1 < sizeY)
						{
							if (cubecolors[z][y + 1][x].a == 0)
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
							if (cubecolors[z][y - 1][x].a == 0)
							{
								mask += SideUtil.getVisibilityMask(Side.BOTTOM);
							}
						}
						else
						{
							mask += SideUtil.getVisibilityMask(Side.BOTTOM);
						}

						if (x + 1 < sizeX)
						{
							if (cubecolors[z][y][x + 1].a == 0)
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
							if (cubecolors[z][y][x - 1].a == 0)
							{
								mask += SideUtil.getVisibilityMask(Side.LEFT);
							}
						}
						else
						{
							mask += SideUtil.getVisibilityMask(Side.LEFT);
						}

						cubecolors[z][y][x].a = mask;
					}
					else
					{
						cubecolors[z][y][x].a = 0;
					}
				}
			}
		}
	}

	public float getLightValue(int z, int y, int x)
	{
		if (z > sizeZ - 1)
			return 1f;
		if (z < 0)
			return 1f;

		if (y > sizeY - 1)
			return 1f;
		if (y < 0)
			return 1f;

		if (x > sizeX - 1)
			return 1f;
		if (x < 0)
			return 1f;

		return cubes[z][y][x].color.getAlpha() > 0 ? .65f : 1f;
	}

	public String getSizeString()
	{
		return sizeX + "_" + sizeY + "_" + sizeZ;
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

	float[][] ld = new float[3][3];
	int px;
	int py;
	int pz;

	public void updateLightMapRelative(Cube cube, Side side, int x, int y, int z)
	{
		switch (side)
		{
			case FRONT:
				px = (int) (x + cube.front.normal.x);
				py = (int) (y + cube.front.normal.y);
				pz = (int) (z + cube.front.normal.z);

				ld[0][0] = getLightValue(pz, py - 1, px - 1);
				ld[0][1] = getLightValue(pz, py - 1, px);
				ld[0][2] = getLightValue(pz, py - 1, px + 1);

				ld[1][0] = getLightValue(pz, py, px - 1);
				ld[1][1] = getLightValue(pz, py, px);
				ld[1][2] = getLightValue(pz, py, px + 1);

				ld[2][0] = getLightValue(pz, py + 1, px - 1);
				ld[2][1] = getLightValue(pz, py + 1, px);
				ld[2][2] = getLightValue(pz, py + 1, px + 1);

				cube.front.lightmap[0] = (ld[0][0] + ld[0][1] + ld[1][0] + ld[1][1]) / 4f;
				cube.front.lightmap[1] = (ld[1][1] + ld[0][1] + ld[0][2] + ld[1][2]) / 4f;
				cube.front.lightmap[2] = (ld[1][1] + ld[1][2] + ld[2][2] + ld[2][1]) / 4f;
				cube.front.lightmap[3] = (ld[1][1] + ld[1][0] + ld[2][0] + ld[2][1]) / 4f;
				break;

			case BACK:
				px = (int) (x + cube.back.normal.x);
				py = (int) (y + cube.back.normal.y);
				pz = (int) (z + cube.back.normal.z);

				ld[0][0] = getLightValue(pz, py - 1, px - 1);
				ld[0][1] = getLightValue(pz, py - 1, px);
				ld[0][2] = getLightValue(pz, py - 1, px + 1);

				ld[1][0] = getLightValue(pz, py, px - 1);
				ld[1][1] = getLightValue(pz, py, px);
				ld[1][2] = getLightValue(pz, py, px + 1);

				ld[2][0] = getLightValue(pz, py + 1, px - 1);
				ld[2][1] = getLightValue(pz, py + 1, px);
				ld[2][2] = getLightValue(pz, py + 1, px + 1);

				cube.back.lightmap[0] = (ld[0][0] + ld[0][1] + ld[1][0] + ld[1][1]) / 4f;
				cube.back.lightmap[1] = (ld[1][1] + ld[0][1] + ld[0][2] + ld[1][2]) / 4f;
				cube.back.lightmap[2] = (ld[1][1] + ld[1][2] + ld[2][2] + ld[2][1]) / 4f;
				cube.back.lightmap[3] = (ld[1][1] + ld[1][0] + ld[2][0] + ld[2][1]) / 4f;
				break;

			case TOP:
				px = (int) (x + cube.top.normal.x);
				py = (int) (y + cube.top.normal.y);
				pz = (int) (z + cube.top.normal.z);

				ld[0][0] = getLightValue(pz + 1, py, px - 1);
				ld[0][1] = getLightValue(pz + 1, py, px);
				ld[0][2] = getLightValue(pz + 1, py, px + 1);

				ld[1][0] = getLightValue(pz, py, px - 1);
				ld[1][1] = getLightValue(pz, py, px);
				ld[1][2] = getLightValue(pz, py, px + 1);

				ld[2][0] = getLightValue(pz - 1, py, px - 1);
				ld[2][1] = getLightValue(pz - 1, py, px);
				ld[2][2] = getLightValue(pz - 1, py, px + 1);

				cube.top.lightmap[0] = (ld[0][0] + ld[0][1] + ld[1][0] + ld[1][1]) / 4f;
				cube.top.lightmap[1] = (ld[1][1] + ld[0][1] + ld[0][2] + ld[1][2]) / 4f;
				cube.top.lightmap[2] = (ld[1][1] + ld[1][2] + ld[2][2] + ld[2][1]) / 4f;
				cube.top.lightmap[3] = (ld[1][1] + ld[1][0] + ld[2][0] + ld[2][1]) / 4f;
				break;

			case BOTTOM:
				px = (int) (x + cube.bottom.normal.x);
				py = (int) (y + cube.bottom.normal.y);
				pz = (int) (z + cube.bottom.normal.z);

				ld[0][0] = getLightValue(pz + 1, py, px - 1);
				ld[0][1] = getLightValue(pz + 1, py, px);
				ld[0][2] = getLightValue(pz + 1, py, px + 1);

				ld[1][0] = getLightValue(pz, py, px - 1);
				ld[1][1] = getLightValue(pz, py, px);
				ld[1][2] = getLightValue(pz, py, px + 1);

				ld[2][0] = getLightValue(pz - 1, py, px - 1);
				ld[2][1] = getLightValue(pz - 1, py, px);
				ld[2][2] = getLightValue(pz - 1, py, px + 1);

				cube.bottom.lightmap[0] = (ld[0][0] + ld[0][1] + ld[1][0] + ld[1][1]) / 4f;
				cube.bottom.lightmap[1] = (ld[1][1] + ld[0][1] + ld[0][2] + ld[1][2]) / 4f;
				cube.bottom.lightmap[2] = (ld[1][1] + ld[1][2] + ld[2][2] + ld[2][1]) / 4f;
				cube.bottom.lightmap[3] = (ld[1][1] + ld[1][0] + ld[2][0] + ld[2][1]) / 4f;
				break;

			case LEFT:
				px = (int) (x + cube.left.normal.x);
				py = (int) (y + cube.left.normal.y);
				pz = (int) (z + cube.left.normal.z);

				ld[0][0] = getLightValue(pz - 1, py - 1, px);
				ld[0][1] = getLightValue(pz, py - 1, px);
				ld[0][2] = getLightValue(pz + 1, py - 1, px);

				ld[1][0] = getLightValue(pz - 1, py, px);
				ld[1][1] = getLightValue(pz, py, px);
				ld[1][2] = getLightValue(pz + 1, py, px);

				ld[2][0] = getLightValue(pz - 1, py + 1, px);
				ld[2][1] = getLightValue(pz, py + 1, px);
				ld[2][2] = getLightValue(pz + 1, py + 1, px);

				cube.left.lightmap[0] = (ld[0][0] + ld[0][1] + ld[1][0] + ld[1][1]) / 4f;
				cube.left.lightmap[1] = (ld[1][1] + ld[0][1] + ld[0][2] + ld[1][2]) / 4f;
				cube.left.lightmap[2] = (ld[1][1] + ld[1][2] + ld[2][2] + ld[2][1]) / 4f;
				cube.left.lightmap[3] = (ld[1][1] + ld[1][0] + ld[2][0] + ld[2][1]) / 4f;
				break;

			case RIGHT:
				px = (int) (x + cube.right.normal.x);
				py = (int) (y + cube.right.normal.y);
				pz = (int) (z + cube.right.normal.z);
				float[][] ld = new float[3][3];

				ld[0][0] = getLightValue(pz - 1, py - 1, px);
				ld[0][1] = getLightValue(pz, py - 1, px);
				ld[0][2] = getLightValue(pz + 1, py - 1, px);

				ld[1][0] = getLightValue(pz - 1, py, px);
				ld[1][1] = getLightValue(pz, py, px);
				ld[1][2] = getLightValue(pz + 1, py, px);

				ld[2][0] = getLightValue(pz - 1, py + 1, px);
				ld[2][1] = getLightValue(pz, py + 1, px);
				ld[2][2] = getLightValue(pz + 1, py + 1, px);

				cube.right.lightmap[0] = (ld[0][0] + ld[0][1] + ld[1][0] + ld[1][1]) / 4f;
				cube.right.lightmap[1] = (ld[1][1] + ld[0][1] + ld[0][2] + ld[1][2]) / 4f;
				cube.right.lightmap[2] = (ld[1][1] + ld[1][2] + ld[2][2] + ld[2][1]) / 4f;
				cube.right.lightmap[3] = (ld[1][1] + ld[1][0] + ld[2][0] + ld[2][1]) / 4f;
				break;
		}
	}

	public void updateLightMap(Vector3 location)
	{
		updateLightMap((int) location.x, (int) location.y, (int) location.z);
	}

	public void updateLightMap(int x, int y, int z)
	{
		int minx = x - 3 > -1 ? x - 3 : 0;
		int maxx = x + 3 < sizeX - 1 ? x + 3 : (sizeX - x) + x;

		int miny = y - 3 > -1 ? y - 3 : 0;
		int maxy = y + 3 < sizeY - 1 ? y + 3 : (sizeY - y) + y;

		int minz = z - 3 > -1 ? z - 3 : 0;
		int maxz = z + 3 < sizeZ - 1 ? z + 3 : (sizeZ - z) + z;

		Cube cube = null;

		for (int z2 = minz; z2 < maxz; z2++)
			for (int y2 = miny; y2 < maxy; y2++)
				for (int x2 = minx; x2 < maxx; x2++)
				{
					cube = cubes[z2][y2][x2];
					updateLightMapRelative(cube, Side.FRONT, x2, y2, z2);
					updateLightMapRelative(cube, Side.BACK, x2, y2, z2);
					updateLightMapRelative(cube, Side.LEFT, x2, y2, z2);
					updateLightMapRelative(cube, Side.RIGHT, x2, y2, z2);
					updateLightMapRelative(cube, Side.TOP, x2, y2, z2);
					updateLightMapRelative(cube, Side.BOTTOM, x2, y2, z2);
				}

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

	public void CREATEZEROEDCUBES()
	{
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					cubes[z][y][x] = new Cube();
					cubes[z][y][x].setPos(x, y, z);
					cubes[z][y][x].setAlpha(0);
				}
			}
		}
	}

	public void dispose()
	{
		GL30.glDeleteVertexArrays(vertexobjectarrayid);
		GL15.glDeleteBuffers(vertexbufferid);
		GL15.glDeleteBuffers(indexbufferid);
	}
}