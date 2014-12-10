package stonevox.data;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class QbMatrixSide
{
	public static float cubesize = .5f;

	public Vector3 normal;
	public int[][][] cubeindexs;
	public float[] vertexdata;
	public float[] lightmap;
	public float lightscale;

	public FloatBuffer voxelbuffer;

	public int vertexobjectbufferid;
	public int voxelbufferid;
	public int indexbufferid;

	public int facecount;

	private int bufferindex;
	private Side side;

	private FloatBuffer subUpdateBuffer;
	private ArrayList<Integer> dataholes = new ArrayList<Integer>();
	private HashMap<String, int[]> updateLocations = new HashMap<String, int[]>();

	public QbMatrixSide(Side side)
	{
		subUpdateBuffer = BufferUtils.createFloatBuffer(28);

		this.side = side;
		switch (side)
		{
			case BACK:
				normal = new Vector3(0, 0, 1);
				lightscale = .965f;
				break;
			case BOTTOM:
				normal = new Vector3(0, -1, 0);
				lightscale = 1f;
				break;
			case FRONT:
				normal = new Vector3(0, 0, -1);
				lightscale = .965f;
				break;
			case LEFT:
				normal = new Vector3(1, 0, 0);
				lightscale = .98f;
				break;
			case RIGHT:
				normal = new Vector3(-1, 0, 0);
				lightscale = .98f;
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
		voxelbuffer.put(vertexdata, 0, facecount * 28);
		voxelbuffer.flip();

		voxelbufferid = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, voxelbufferid);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, voxelbuffer, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		vertexobjectbufferid = GL30.glGenVertexArrays();

		GL30.glBindVertexArray(vertexobjectbufferid);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, voxelbufferid);
		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 7 << 2, 0l);
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 7 << 2, 3 << 2);
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, 7 << 2, 6 << 2);
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
					x, y, z
			});
		}
	}

	public void addVoxelData(int x, int y, int z, Color color)
	{
		int index = cubeindexs[z][y][x];

		if (index == -1)
		{
			setVertexPositionData(index, x, y, z);

			vertexdata[index + 3] = color.r;
			vertexdata[index + 4] = color.g;
			vertexdata[index + 5] = color.b;
			vertexdata[index + 6] = 1f * lightscale;

			vertexdata[index + 10] = color.r;
			vertexdata[index + 11] = color.g;
			vertexdata[index + 12] = color.b;
			vertexdata[index + 13] = 1f * lightscale;

			vertexdata[index + 17] = color.r;
			vertexdata[index + 18] = color.g;
			vertexdata[index + 19] = color.b;
			vertexdata[index + 20] = 1f * lightscale;

			vertexdata[index + 24] = color.r;
			vertexdata[index + 25] = color.g;
			vertexdata[index + 26] = color.b;
			vertexdata[index + 27] = 1f * lightscale;

			updateLocations.put(x + "" + y + "" + z, new int[]
			{
					x, y, z
			});
		}
	}

	public void removeVoxelData(int x, int y, int z)
	{
		int index = cubeindexs[z][y][x];

		if (index >= 0)
		{
			dataholes.add(index);
			cubeindexs[z][y][x] = -1;

			for (int i = index; i < index + 28; i++)
			{
				vertexdata[i] = -10000;
			}

			updateLocations.put(x + "" + y + "" + z, new int[]
			{
					x, y, z
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
			}
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			updateLocations.clear();
		}
	}

	public void dispose()
	{
		GL30.glDeleteVertexArrays(this.vertexobjectbufferid);
		GL15.glDeleteBuffers(this.voxelbufferid);
	}
}
