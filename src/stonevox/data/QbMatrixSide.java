package stonevox.data;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class QbMatrixSide
{
	public Vector3 normal;
	public int[][][] cubeindexs;
	public float[] vertexdata;

	public FloatBuffer voxelbuffer;

	public int vertexobjectbufferid;
	public int voxelbufferid;
	public int indexbufferid;

	public ArrayList<Integer> dataholes = new ArrayList<Integer>();

	public QbMatrixSide(Side side)
	{
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
		vertexdata = new float[x * z * y * 28];
		voxelbuffer = BufferUtils.createFloatBuffer(x * z * y * 28);
	}

	public void genVoxelBuffer()
	{
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
		voxelbufferid = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, voxelbufferid);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, voxelbuffer, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
}
