package stonevox.decorator;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import stonevox.Program;
import stonevox.data.GUIdecorator;
import stonevox.data.GradientStop;
import stonevox.data.Matrix;
import stonevox.data.Shader;

public class GradientBackground implements GUIdecorator
{

	private boolean enabled = true;

	private int bufferID;
	private FloatBuffer buffer;

	private int aobufferID;
	private int indexID;

	private GradientStop[] grads;

	private boolean firstRun = true;
	private boolean isHorizontal = false;

	public GradientBackground(GradientStop... grads)
	{
		this.grads = grads;

		buffer = BufferUtils.createFloatBuffer(28 * grads.length);
		buffer.put(new float[28 * grads.length]);
		buffer.flip();

		bufferID = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferID);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		IntBuffer indexbuff = BufferUtils.createIntBuffer(6 * grads.length);
		for (int i = 0; i < grads.length; i++)
		{
			indexbuff.put(i * 4);
			indexbuff.put(i * 4 + 1);
			indexbuff.put(i * 4 + 2);
			indexbuff.put(i * 4);
			indexbuff.put(i * 4 + 2);
			indexbuff.put(i * 4 + 3);
		}
		indexbuff.flip();

		indexID = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexID);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbuff, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		aobufferID = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(aobufferID);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferID);
		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 7 << 2, 0l);
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 7 << 2, 3 << 2);
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, 7 << 2, 6 << 2);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexID);
		GL30.glBindVertexArray(0);
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		enabled = true;
	}

	public void paint(float x, float y, float width, float height)
	{
		Program.shader.UseShader();
		Program.shader.WriteUniformMatrix4("modelview\0", Matrix.CreateTranslation(x, y, 0).GetBuffer());

		GL30.glBindVertexArray(aobufferID);
		GL11.glDrawElements(GL11.GL_TRIANGLES, grads.length * 6, GL11.GL_UNSIGNED_INT, 0l);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		GL30.glBindVertexArray(0);

		Shader.ResetUseShader();
	}

	public void dispose()
	{
	}

	public void updateGradient(float width, float height)
	{
		float lastheight = 0;
		buffer = BufferUtils.createFloatBuffer(28 * grads.length);

		for (int i = 0; i < grads.length; i++)
		{
			GradientStop g = grads[i];
			GradientStop g2 = i + 1 < grads.length ? grads[i + 1] : g;

			float dheight = height * g.distance;

			buffer.put(0);
			buffer.put(lastheight);
			buffer.put(0);
			buffer.put(g.color.r);
			buffer.put(g.color.g);
			buffer.put(g.color.b);
			buffer.put(1);

			buffer.put(width);
			buffer.put(lastheight);
			buffer.put(0);
			buffer.put(g.color.r);
			buffer.put(g.color.g);
			buffer.put(g.color.b);
			buffer.put(1);

			buffer.put(width);
			buffer.put(lastheight + dheight);
			buffer.put(0);
			buffer.put(g2.color.r);
			buffer.put(g2.color.g);
			buffer.put(g2.color.b);
			buffer.put(1);

			buffer.put(0);
			buffer.put(lastheight + dheight);
			buffer.put(0);
			buffer.put(g2.color.r);
			buffer.put(g2.color.g);
			buffer.put(g2.color.b);
			buffer.put(1);

			lastheight += dheight;
		}

		buffer.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferID);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		IntBuffer indexbuff = BufferUtils.createIntBuffer(6 * grads.length);
		for (int i = 0; i < grads.length; i++)
		{
			indexbuff.put(i * 4);
			indexbuff.put(i * 4 + 1);
			indexbuff.put(i * 4 + 2);
			indexbuff.put(i * 4);
			indexbuff.put(i * 4 + 2);
			indexbuff.put(i * 4 + 3);
		}
		indexbuff.flip();

		indexID = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexID);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbuff, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
}
