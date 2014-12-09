package stonevox.data;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;

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

	public FloatBuffer voxelbuffer;

	public int vertexobjectbufferid;
	public int voxelbufferid;
	public int indexbufferid;

	public int facecount;

	private int bufferindex;
	private Side side;

	public ArrayList<Integer> dataholes = new ArrayList<Integer>();

	public QbMatrixSide(Side side)
	{
		this.side = side;
		switch (side)
		{
			case BACK:
				normal = new Vector3(0, 0, -1);
				break;
			case BOTTOM:
				normal = new Vector3(0, -1, 0);
				break;
			case FRONT:
				normal = new Vector3(0, 0, 1);
				break;
			case LEFT:
				normal = new Vector3(-1, 0, 0);
				break;
			case RIGHT:
				normal = new Vector3(1, 0, 0);
				break;
			case TOP:
				normal = new Vector3(0, 1, 0);
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

		voxelbufferid = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, voxelbufferid);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, voxelbuffer, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	public void putVertexData(int x, int y, int z, Color color)
	{
		switch (side)
		{
			case FRONT:

				vertexdata[0 + bufferindex] = -cubesize + x;
				vertexdata[1 + bufferindex] = -cubesize + y;
				vertexdata[2 + bufferindex] = cubesize + z;

				vertexdata[7 + bufferindex] = cubesize + x;
				vertexdata[8 + bufferindex] = -cubesize + y;
				vertexdata[9 + bufferindex] = cubesize + z;

				vertexdata[14 + bufferindex] = cubesize + x;
				vertexdata[15 + bufferindex] = cubesize + y;
				vertexdata[16 + bufferindex] = cubesize + z;

				vertexdata[21 + bufferindex] = -cubesize + x;
				vertexdata[22 + bufferindex] = cubesize + y;
				vertexdata[23 + bufferindex] = cubesize + z;

				break;
			case BACK:

				vertexdata[0 + bufferindex] = -cubesize + x;
				vertexdata[1 + bufferindex] = -cubesize + y;
				vertexdata[2 + bufferindex] = -cubesize + z;

				vertexdata[7 + bufferindex] = cubesize + x;
				vertexdata[8 + bufferindex] = -cubesize + y;
				vertexdata[9 + bufferindex] = -cubesize + z;

				vertexdata[14 + bufferindex] = cubesize + x;
				vertexdata[15 + bufferindex] = cubesize + y;
				vertexdata[16 + bufferindex] = -cubesize + z;

				vertexdata[21 + bufferindex] = -cubesize + x;
				vertexdata[22 + bufferindex] = cubesize + y;
				vertexdata[23 + bufferindex] = -cubesize + z;

				break;
			case TOP:

				vertexdata[0 + bufferindex] = -cubesize + x;
				vertexdata[1 + bufferindex] = cubesize + y;
				vertexdata[2 + bufferindex] = cubesize + z;

				vertexdata[7 + bufferindex] = cubesize + x;
				vertexdata[8 + bufferindex] = cubesize + y;
				vertexdata[9 + bufferindex] = cubesize + z;

				vertexdata[14 + bufferindex] = cubesize + x;
				vertexdata[15 + bufferindex] = cubesize + y;
				vertexdata[16 + bufferindex] = -cubesize + z;

				vertexdata[21 + bufferindex] = -cubesize + x;
				vertexdata[22 + bufferindex] = cubesize + y;
				vertexdata[23 + bufferindex] = -cubesize + z;

				break;
			case BOTTOM:

				vertexdata[0 + bufferindex] = -cubesize + x;
				vertexdata[1 + bufferindex] = -cubesize + y;
				vertexdata[2 + bufferindex] = cubesize + z;

				vertexdata[7 + bufferindex] = cubesize + x;
				vertexdata[8 + bufferindex] = -cubesize + y;
				vertexdata[9 + bufferindex] = cubesize + z;

				vertexdata[14 + bufferindex] = cubesize + x;
				vertexdata[15 + bufferindex] = -cubesize + y;
				vertexdata[16 + bufferindex] = -cubesize + z;

				vertexdata[21 + bufferindex] = -cubesize + x;
				vertexdata[22 + bufferindex] = -cubesize + y;
				vertexdata[23 + bufferindex] = -cubesize + z;

				break;
			case LEFT:

				vertexdata[0 + bufferindex] = -cubesize + x;
				vertexdata[1 + bufferindex] = -cubesize + y;
				vertexdata[2 + bufferindex] = -cubesize + z;

				vertexdata[7 + bufferindex] = -cubesize + x;
				vertexdata[8 + bufferindex] = -cubesize + y;
				vertexdata[9 + bufferindex] = cubesize + z;

				vertexdata[14 + bufferindex] = -cubesize + x;
				vertexdata[15 + bufferindex] = cubesize + y;
				vertexdata[16 + bufferindex] = cubesize + z;

				vertexdata[21 + bufferindex] = -cubesize + x;
				vertexdata[22 + bufferindex] = cubesize + y;
				vertexdata[23 + bufferindex] = -cubesize + z;

				break;
			case RIGHT:

				vertexdata[0 + bufferindex] = cubesize + x;
				vertexdata[1 + bufferindex] = -cubesize + y;
				vertexdata[2 + bufferindex] = -cubesize + z;

				vertexdata[7 + bufferindex] = cubesize + x;
				vertexdata[8 + bufferindex] = -cubesize + y;
				vertexdata[9 + bufferindex] = cubesize + z;

				vertexdata[14 + bufferindex] = cubesize + x;
				vertexdata[15 + bufferindex] = cubesize + y;
				vertexdata[16 + bufferindex] = cubesize + z;

				vertexdata[21 + bufferindex] = cubesize + x;
				vertexdata[22 + bufferindex] = cubesize + y;
				vertexdata[23 + bufferindex] = -cubesize + z;

				break;
		}

		vertexdata[3 + bufferindex] = color.r;
		vertexdata[4 + bufferindex] = color.g;
		vertexdata[5 + bufferindex] = color.b;
		vertexdata[6 + bufferindex] = 1f;

		vertexdata[10 + bufferindex] = color.r;
		vertexdata[11 + bufferindex] = color.g;
		vertexdata[12 + bufferindex] = color.b;
		vertexdata[13 + bufferindex] = 1f;

		vertexdata[17 + bufferindex] = color.r;
		vertexdata[18 + bufferindex] = color.g;
		vertexdata[19 + bufferindex] = color.b;
		vertexdata[20 + bufferindex] = 1f;

		vertexdata[24 + bufferindex] = color.r;
		vertexdata[25 + bufferindex] = color.g;
		vertexdata[26 + bufferindex] = color.b;
		vertexdata[27 + bufferindex] = 1f;

		cubeindexs[z][y][x] = bufferindex / 28;
		bufferindex += 28;
		facecount++;
	}

	public void dispose()
	{
		GL30.glDeleteVertexArrays(this.vertexobjectbufferid);
		GL15.glDeleteBuffers(this.voxelbufferid);
	}
}
