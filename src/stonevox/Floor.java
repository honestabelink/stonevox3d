package stonevox;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.newdawn.slick.Color;

import stonevox.data.Matrix;
import stonevox.data.RayHitPoint;
import stonevox.data.Vector3;
import stonevox.util.RaycastingUtil;

public class Floor
{
	private int[] index;
	private Color color;

	private Vector3[] vertex = new Vector3[4];
	private FloatBuffer vertexbuffer;
	private IntBuffer indexbuffer;

	private int vertexarrayobjectID;
	private int vertexbufferID;
	private int indexbufferID;

	private Matrix final_transform;
	private Matrix transform;
	private Vector3 up = new Vector3(0f, 1f, 0f);

	public boolean visible = true;

	public Floor()
	{
		color = Color.cyan;

		transform = Matrix.CreateTranslation(new Vector3());

		float[] vertexdata = new float[4 * 7];

		float sizex = (float) Program.model.GetActiveMatrix().sizeX * 2f;
		float sizez = (float) Program.model.GetActiveMatrix().sizeZ * 2f;

		transform = Matrix.CreateTranslation(new Vector3(0, 0, 0));

		vertex[0] = new Vector3(-.5f, -.5f, -.5f);
		vertexdata[0] = -.5f;
		vertexdata[1] = -.5f;
		vertexdata[2] = -.5f;
		vertexdata[3] = color.r;
		vertexdata[4] = color.g;
		vertexdata[5] = color.b;
		vertexdata[6] = 1f;

		vertex[1] = new Vector3(-.5f, -.5f, sizez * .5f - .5f);
		vertexdata[7] = -.5f;
		vertexdata[8] = -.5f;
		vertexdata[9] = sizez * .5f - .5f;
		vertexdata[10] = color.r;
		vertexdata[11] = color.g;
		vertexdata[12] = color.b;
		vertexdata[13] = 1f;

		vertex[2] = new Vector3(sizex * .5f - .5f, -.5f, sizez * .5f - .5f);
		vertexdata[14] = sizex * .5f - .5f;
		vertexdata[15] = -.5f;
		vertexdata[16] = sizez * .5f - .5f;
		vertexdata[17] = color.r;
		vertexdata[18] = color.g;
		vertexdata[19] = color.b;
		vertexdata[20] = 1f;

		vertex[3] = new Vector3(sizex * .5f - .5f, -.5f, -.5f);
		vertexdata[21] = sizex * .5f - .5f;
		vertexdata[22] = -.5f;
		vertexdata[23] = -.5f;
		vertexdata[24] = color.r;
		vertexdata[25] = color.g;
		vertexdata[26] = color.b;
		vertexdata[27] = 1f;

		index = new int[6];
		index[0] = 0;
		index[1] = 1;
		index[2] = 2;
		index[3] = 0;
		index[4] = 2;
		index[5] = 3;

		vertexbuffer = BufferUtils.createFloatBuffer(4 * 7);
		vertexbuffer.put(vertexdata);
		vertexbuffer.flip();
		indexbuffer = BufferUtils.createIntBuffer(6);
		indexbuffer.put(index);
		indexbuffer.flip();

		vertexbufferID = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexbufferID);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexbuffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		indexbufferID = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbufferID);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbuffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		vertexarrayobjectID = GL30.glGenVertexArrays();

		GL30.glBindVertexArray(vertexarrayobjectID);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexbufferID);
		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 7 << 2, 0l);
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 7 << 2, 3 << 2);
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, 7 << 2, 6 << 2);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbufferID);
		GL30.glBindVertexArray(0);
	}

	public void updatemesh()
	{
		float sizex = (float) Program.model.GetActiveMatrix().sizeX * 2f;
		float sizez = (float) Program.model.GetActiveMatrix().sizeZ * 2f;

		transform = Matrix.CreateTranslation(new Vector3(0, 0, 0));

		float[] vertexdata = new float[4 * 7];

		vertex[0] = new Vector3(-.5f, -.5f, -.5f);
		vertexdata[0] = -.5f;
		vertexdata[1] = -.5f;
		vertexdata[2] = -.5f;
		vertexdata[3] = color.r;
		vertexdata[4] = color.g;
		vertexdata[5] = color.b;
		vertexdata[6] = 1f;

		vertex[1] = new Vector3(-.5f, -.5f, sizez * .5f - .5f);
		vertexdata[7] = -.5f;
		vertexdata[8] = -.5f;
		vertexdata[9] = sizez * .5f - .5f;
		vertexdata[10] = color.r;
		vertexdata[11] = color.g;
		vertexdata[12] = color.b;
		vertexdata[13] = 1f;

		vertex[2] = new Vector3(sizex * .5f - .5f, -.5f, sizez * .5f - .5f);
		vertexdata[14] = sizex * .5f - .5f;
		vertexdata[15] = -.5f;
		vertexdata[16] = sizez * .5f - .5f;
		vertexdata[17] = color.r;
		vertexdata[18] = color.g;
		vertexdata[19] = color.b;
		vertexdata[20] = 1f;

		vertex[3] = new Vector3(sizex * .5f - .5f, -.5f, -.5f);
		vertexdata[21] = sizex * .5f - .5f;
		vertexdata[22] = -.5f;
		vertexdata[23] = -.5f;
		vertexdata[24] = color.r;
		vertexdata[25] = color.g;
		vertexdata[26] = color.b;
		vertexdata[27] = 1f;

		vertexbuffer.put(vertexdata);
		vertexbuffer.flip();

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexbufferID);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexbuffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	public RayHitPoint rayCast(Vector3 origin, Vector3 projection)
	{
		RayHitPoint hit = new RayHitPoint();
		Vector3 result = RaycastingUtil.rayTest(origin, projection, up, vertex[0], vertex[1], vertex[2]);

		if (result == null)
		{
			result = RaycastingUtil.rayTest(origin, projection, up, vertex[0], vertex[2], vertex[3]);
		}

		if (result != null)
		{
			hit.cubelocation.x = Math.round(result.x);
			hit.cubelocation.y = -1;
			hit.cubelocation.z = Math.round(result.z);
			hit.cubenormal = up;
			return hit;
		}

		return null;
	}

	public void render()
	{
		if (visible)
		{
			final_transform = Matrix.Multiply(Program.camera.modelview, transform);

			Program.shader.WriteUniformMatrix4("modelview\0", final_transform.GetBuffer());

			GL30.glBindVertexArray(vertexarrayobjectID);
			GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0l);
			GL30.glBindVertexArray(0);
		}
	}
}
