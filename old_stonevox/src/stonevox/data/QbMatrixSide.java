package stonevox.data;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import stonevox.Program;
import stonevox.util.RaycastingUtil;

public class QbMatrixSide
{
	public static float cubesize = .5f;

	public Vector3 normal;
	public int[][][] cubeindexs;
	public float[] vertexdata;
	public float[] lightmap;
	public float lightscale;
	public Vector3[] raycastVerts;

	public FloatBuffer voxelbuffer;

	public int vertexobjectbufferid;
	public int voxelbufferid;
	public int indexbufferid;

	public int facecount;

	private int bufferindex;
	private Side side;

	private FloatBuffer subUpdateBuffer;
	private HashMap<String, int[]> updateLocations = new HashMap<String, int[]>();

	public QbMatrixSide(Side side)
	{
		this.raycastVerts = new Vector3[4];
		for (int i = 0; i < 4; i++)
		{
			raycastVerts[i] = new Vector3();
		}

		subUpdateBuffer = BufferUtils.createFloatBuffer(28);

		this.side = side;
		switch (side)
		{
			case BACK:
				normal = new Vector3(0, 0, 1);
				lightscale = .90f;
				break;
			case BOTTOM:
				normal = new Vector3(0, -1, 0);
				lightscale = 1f;
				break;
			case FRONT:
				normal = new Vector3(0, 0, -1);
				lightscale = .9f;
				break;
			case LEFT:
				normal = new Vector3(1, 0, 0);
				lightscale = .95f;
				break;
			case RIGHT:
				normal = new Vector3(-1, 0, 0);
				lightscale = .95f;
				break;
			case TOP:
				normal = new Vector3(0, 1, 0);
				lightscale = 1f;
				break;
		}
	}

	public void setSize(int x, int y, int z)
	{
		cubeindexs = new int[z][y][x];
		for (int[][] innerRow : cubeindexs)
			for (int[] innerInnerRow : innerRow)
				Arrays.fill(innerInnerRow, -1);

		vertexdata = new float[x * z * y * 28];
		lightmap = new float[x * z * y * 4];
		voxelbuffer = BufferUtils.createFloatBuffer(x * z * y * 28);
	}

	public void genVoxelBuffers()
	{
		voxelbuffer.put(vertexdata);
		voxelbuffer.flip();

		voxelbufferid = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, voxelbufferid);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, voxelbuffer, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		vertexobjectbufferid = GL30.glGenVertexArrays();

		GL30.glBindVertexArray(vertexobjectbufferid);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, voxelbufferid);
		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(Program.shader.GetAttributeAcces("position\0"), 3, GL11.GL_FLOAT, false, 7 << 2, 0l);
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(Program.shader.GetAttributeAcces("color\0"), 3, GL11.GL_FLOAT, false, 7 << 2, 3 << 2);
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(Program.shader.GetAttributeAcces("light\0"), 1, GL11.GL_FLOAT, false, 7 << 2, 6 << 2);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbufferid);
		GL30.glBindVertexArray(0);
	}

	public void bufferData()
	{
		voxelbuffer.put(vertexdata, 0, facecount * 28);
		voxelbuffer.flip();

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, voxelbufferid);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, voxelbuffer, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	public void subBufferData(int index)
	{
		for (int i = 0; i < 28; i++)
		{
			subUpdateBuffer.put(vertexdata[index + i]);
		}
		subUpdateBuffer.flip();

		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, index << 2, subUpdateBuffer);
	}

	public void putVertexData(float x, float y, float z, Color color)
	{
		setVertexPositionData(bufferindex, x, y, z);

		vertexdata[bufferindex + 3] = color.r;
		vertexdata[bufferindex + 4] = color.g;
		vertexdata[bufferindex + 5] = color.b;
		vertexdata[bufferindex + 6] = 1f * lightscale;

		vertexdata[bufferindex + 10] = color.r;
		vertexdata[bufferindex + 11] = color.g;
		vertexdata[bufferindex + 12] = color.b;
		vertexdata[bufferindex + 13] = 1f * lightscale;

		vertexdata[bufferindex + 17] = color.r;
		vertexdata[bufferindex + 18] = color.g;
		vertexdata[bufferindex + 19] = color.b;
		vertexdata[bufferindex + 20] = 1f * lightscale;

		vertexdata[bufferindex + 24] = color.r;
		vertexdata[bufferindex + 25] = color.g;
		vertexdata[bufferindex + 26] = color.b;
		vertexdata[bufferindex + 27] = 1f * lightscale;

		cubeindexs[(int) z][(int) y][(int) x] = bufferindex;
		bufferindex += 28;
		facecount++;
	}

	private void setVertexPositionData(int bufferindex, float x, float y, float z)
	{
		switch (side)
		{
			case BACK:

				vertexdata[bufferindex + 0] = -cubesize + x;
				vertexdata[bufferindex + 1] = -cubesize + y;
				vertexdata[bufferindex + 2] = cubesize + z;

				vertexdata[bufferindex + 7] = cubesize + x;
				vertexdata[bufferindex + 8] = -cubesize + y;
				vertexdata[bufferindex + 9] = cubesize + z;

				vertexdata[bufferindex + 14] = cubesize + x;
				vertexdata[bufferindex + 15] = cubesize + y;
				vertexdata[bufferindex + 16] = cubesize + z;

				vertexdata[bufferindex + 21] = -cubesize + x;
				vertexdata[bufferindex + 22] = cubesize + y;
				vertexdata[bufferindex + 23] = cubesize + z;

				break;
			case FRONT:

				vertexdata[bufferindex + 0] = -cubesize + x;
				vertexdata[bufferindex + 1] = -cubesize + y;
				vertexdata[bufferindex + 2] = -cubesize + z;

				vertexdata[bufferindex + 7] = cubesize + x;
				vertexdata[bufferindex + 8] = -cubesize + y;
				vertexdata[bufferindex + 9] = -cubesize + z;

				vertexdata[bufferindex + 14] = cubesize + x;
				vertexdata[bufferindex + 15] = cubesize + y;
				vertexdata[bufferindex + 16] = -cubesize + z;

				vertexdata[bufferindex + 21] = -cubesize + x;
				vertexdata[bufferindex + 22] = cubesize + y;
				vertexdata[bufferindex + 23] = -cubesize + z;

				break;
			case TOP:

				vertexdata[bufferindex + 0] = -cubesize + x;
				vertexdata[bufferindex + 1] = cubesize + y;
				vertexdata[bufferindex + 2] = cubesize + z;

				vertexdata[bufferindex + 7] = cubesize + x;
				vertexdata[bufferindex + 8] = cubesize + y;
				vertexdata[bufferindex + 9] = cubesize + z;

				vertexdata[bufferindex + 14] = cubesize + x;
				vertexdata[bufferindex + 15] = cubesize + y;
				vertexdata[bufferindex + 16] = -cubesize + z;

				vertexdata[bufferindex + 21] = -cubesize + x;
				vertexdata[bufferindex + 22] = cubesize + y;
				vertexdata[bufferindex + 23] = -cubesize + z;

				break;
			case BOTTOM:

				vertexdata[bufferindex + 0] = -cubesize + x;
				vertexdata[bufferindex + 1] = -cubesize + y;
				vertexdata[bufferindex + 2] = cubesize + z;

				vertexdata[bufferindex + 7] = cubesize + x;
				vertexdata[bufferindex + 8] = -cubesize + y;
				vertexdata[bufferindex + 9] = cubesize + z;

				vertexdata[bufferindex + 14] = cubesize + x;
				vertexdata[bufferindex + 15] = -cubesize + y;
				vertexdata[bufferindex + 16] = -cubesize + z;

				vertexdata[bufferindex + 21] = -cubesize + x;
				vertexdata[bufferindex + 22] = -cubesize + y;
				vertexdata[bufferindex + 23] = -cubesize + z;

				break;
			case RIGHT:

				vertexdata[bufferindex + 0] = -cubesize + x;
				vertexdata[bufferindex + 1] = -cubesize + y;
				vertexdata[bufferindex + 2] = -cubesize + z;

				vertexdata[bufferindex + 7] = -cubesize + x;
				vertexdata[bufferindex + 8] = -cubesize + y;
				vertexdata[bufferindex + 9] = cubesize + z;

				vertexdata[bufferindex + 14] = -cubesize + x;
				vertexdata[bufferindex + 15] = cubesize + y;
				vertexdata[bufferindex + 16] = cubesize + z;

				vertexdata[bufferindex + 21] = -cubesize + x;
				vertexdata[bufferindex + 22] = cubesize + y;
				vertexdata[bufferindex + 23] = -cubesize + z;

				break;
			case LEFT:

				vertexdata[bufferindex + 0] = cubesize + x;
				vertexdata[bufferindex + 1] = -cubesize + y;
				vertexdata[bufferindex + 2] = -cubesize + z;

				vertexdata[bufferindex + 7] = cubesize + x;
				vertexdata[bufferindex + 8] = -cubesize + y;
				vertexdata[bufferindex + 9] = cubesize + z;

				vertexdata[bufferindex + 14] = cubesize + x;
				vertexdata[bufferindex + 15] = cubesize + y;
				vertexdata[bufferindex + 16] = cubesize + z;

				vertexdata[bufferindex + 21] = cubesize + x;
				vertexdata[bufferindex + 22] = cubesize + y;
				vertexdata[bufferindex + 23] = -cubesize + z;

				break;
		}
	}

	public boolean raytestUndefinedCube(float x, float y, float z)
	{
		switch (side)
		{
			case BACK:

				raycastVerts[0].x = -cubesize + x;
				raycastVerts[0].y = -cubesize + y;
				raycastVerts[0].z = cubesize + z;

				raycastVerts[1].x = cubesize + x;
				raycastVerts[1].y = -cubesize + y;
				raycastVerts[1].z = cubesize + z;

				raycastVerts[2].x = cubesize + x;
				raycastVerts[2].y = cubesize + y;
				raycastVerts[2].z = cubesize + z;

				raycastVerts[3].x = -cubesize + x;
				raycastVerts[3].y = cubesize + y;
				raycastVerts[3].z = cubesize + z;
				break;
			case FRONT:

				raycastVerts[0].x = -cubesize + x;
				raycastVerts[0].y = -cubesize + y;
				raycastVerts[0].z = -cubesize + z;

				raycastVerts[1].x = cubesize + x;
				raycastVerts[1].y = -cubesize + y;
				raycastVerts[1].z = -cubesize + z;

				raycastVerts[2].x = cubesize + x;
				raycastVerts[2].y = cubesize + y;
				raycastVerts[2].z = -cubesize + z;

				raycastVerts[3].x = -cubesize + x;
				raycastVerts[3].y = cubesize + y;
				raycastVerts[3].z = -cubesize + z;

				break;
			case TOP:

				raycastVerts[0].x = -cubesize + x;
				raycastVerts[0].y = cubesize + y;
				raycastVerts[0].z = cubesize + z;

				raycastVerts[1].x = cubesize + x;
				raycastVerts[1].y = cubesize + y;
				raycastVerts[1].z = cubesize + z;

				raycastVerts[2].x = cubesize + x;
				raycastVerts[2].y = cubesize + y;
				raycastVerts[2].z = -cubesize + z;

				raycastVerts[3].x = -cubesize + x;
				raycastVerts[3].y = cubesize + y;
				raycastVerts[3].z = -cubesize + z;

				break;
			case BOTTOM:

				raycastVerts[0].x = -cubesize + x;
				raycastVerts[0].y = -cubesize + y;
				raycastVerts[0].z = cubesize + z;

				raycastVerts[1].x = cubesize + x;
				raycastVerts[1].y = -cubesize + y;
				raycastVerts[1].z = cubesize + z;

				raycastVerts[2].x = cubesize + x;
				raycastVerts[2].y = -cubesize + y;
				raycastVerts[2].z = -cubesize + z;

				raycastVerts[3].x = -cubesize + x;
				raycastVerts[3].y = -cubesize + y;
				raycastVerts[3].z = -cubesize + z;

				break;
			case RIGHT:

				raycastVerts[0].x = -cubesize + x;
				raycastVerts[0].y = -cubesize + y;
				raycastVerts[0].z = -cubesize + z;

				raycastVerts[1].x = -cubesize + x;
				raycastVerts[1].y = -cubesize + y;
				raycastVerts[1].z = cubesize + z;

				raycastVerts[2].x = -cubesize + x;
				raycastVerts[2].y = cubesize + y;
				raycastVerts[2].z = cubesize + z;

				raycastVerts[3].x = -cubesize + x;
				raycastVerts[3].y = cubesize + y;
				raycastVerts[3].z = -cubesize + z;

				break;
			case LEFT:

				raycastVerts[0].x = cubesize + x;
				raycastVerts[0].y = -cubesize + y;
				raycastVerts[0].z = -cubesize + z;

				raycastVerts[1].x = cubesize + x;
				raycastVerts[1].y = -cubesize + y;
				raycastVerts[1].z = cubesize + z;

				raycastVerts[2].x = cubesize + x;
				raycastVerts[2].y = cubesize + y;
				raycastVerts[2].z = cubesize + z;

				raycastVerts[3].x = cubesize + x;
				raycastVerts[3].y = cubesize + y;
				raycastVerts[3].z = -cubesize + z;

				break;
		}

		if (RaycastingUtil.rayTest(RaycastingUtil.rayOrigin, RaycastingUtil.rayDirection, this.normal, raycastVerts[0],
				raycastVerts[1], raycastVerts[2]) != null
				|| RaycastingUtil.rayTest(RaycastingUtil.rayOrigin, RaycastingUtil.rayDirection, this.normal,
						raycastVerts[0], raycastVerts[2], raycastVerts[3]) != null)
		{
			return true;
		}
		return false;
	}

	public void putEmptySpace(int x, int y, int z)
	{
		cubeindexs[z][y][x] = -1;
	}

	public void putBlankData(int x, int y, int z)
	{
		setBlankVertexPositionData(bufferindex, x, y, z);

		cubeindexs[(int) z][(int) y][(int) x] = bufferindex;
		bufferindex += 28;
		facecount++;
	}

	public void setBlankVertexPositionData(int bufferindex, float x, float y, float z)
	{
		vertexdata[bufferindex + 0] = -1000;
		vertexdata[bufferindex + 1] = -1000;
		vertexdata[bufferindex + 2] = -1000;

		vertexdata[bufferindex + 7] = -1000;
		vertexdata[bufferindex + 8] = -1000;
		vertexdata[bufferindex + 9] = -1000;

		vertexdata[bufferindex + 14] = -1000;
		vertexdata[bufferindex + 15] = -1000;
		vertexdata[bufferindex + 16] = -1000;

		vertexdata[bufferindex + 21] = -1000;
		vertexdata[bufferindex + 22] = -1000;
		vertexdata[bufferindex + 23] = -1000;
	}

	public void setLightValues(int x, int y, int z, float l1, float l2, float l3, float l4)
	{
		int index = cubeindexs[z][y][x];

		if (index >= 0)
		{
			vertexdata[index + 6] = l1 * lightscale;
			vertexdata[index + 13] = l2 * lightscale;
			vertexdata[index + 20] = l3 * lightscale;
			vertexdata[index + 27] = l4 * lightscale;

			if (!updateLocations.containsKey(x + "" + y + "" + z))
			{
				updateLocations.put(x + "" + y + "" + z, new int[]
				{
						x, y, z, 0
				});
			}
		}
	}

	public void setColor(int x, int y, int z, Color color)
	{
		int index = cubeindexs[z][y][x];

		if (index >= 0)
		{
			vertexdata[index + 3] = color.r;
			vertexdata[index + 4] = color.g;
			vertexdata[index + 5] = color.b;

			vertexdata[index + 10] = color.r;
			vertexdata[index + 11] = color.g;
			vertexdata[index + 12] = color.b;

			vertexdata[index + 17] = color.r;
			vertexdata[index + 18] = color.g;
			vertexdata[index + 19] = color.b;

			vertexdata[index + 24] = color.r;
			vertexdata[index + 25] = color.g;
			vertexdata[index + 26] = color.b;

			updateLocations.put(x + "" + y + "" + z, new int[]
			{
					x, y, z, 0
			});
		}
	}

	public void addVoxelData(int x, int y, int z, Color color)
	{
		int index = cubeindexs[z][y][x];

		if (index == -1)
		{
			putVertexData(x, y, z, color);

			updateLocations.put(x + "" + y + "" + z, new int[]
			{
					x, y, z, 0
			});
		}
		else
		{
			setVertexPositionData(index, x, y, z);

			vertexdata[index + 3] = color.r;
			vertexdata[index + 4] = color.g;
			vertexdata[index + 5] = color.b;

			vertexdata[index + 10] = color.r;
			vertexdata[index + 11] = color.g;
			vertexdata[index + 12] = color.b;

			vertexdata[index + 17] = color.r;
			vertexdata[index + 18] = color.g;
			vertexdata[index + 19] = color.b;

			vertexdata[index + 24] = color.r;
			vertexdata[index + 25] = color.g;
			vertexdata[index + 26] = color.b;

			updateLocations.put(x + "" + y + "" + z, new int[]
			{
					x, y, z, 0
			});
		}
	}

	public void removeVoxelData(int x, int y, int z)
	{
		int index = cubeindexs[z][y][x];

		if (index >= 0)
		{
			// cubeindexs[z][y][x] = -1;

			setBlankVertexPositionData(index, x, y, z);

			updateLocations.put(x + "" + y + "" + z, new int[]
			{
					x, y, z, index
			});
		}
	}

	public void updateSubBufferData()
	{
		if (updateLocations.size() > 0)
		{
			int index = -1;
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, voxelbufferid);
			for (int[] v : updateLocations.values())
			{
				index = cubeindexs[v[2]][v[1]][v[0]];

				if (index >= 0)
				{
					subBufferData(index);
				}
				else
				{
					subBufferData(v[3]);
				}
			}
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			updateLocations.clear();
		}
	}

	public void dispose()
	{
		GL30.glDeleteVertexArrays(this.vertexobjectbufferid);
		GL15.glDeleteBuffers(this.voxelbufferid);
		updateLocations.clear();
	}
}
