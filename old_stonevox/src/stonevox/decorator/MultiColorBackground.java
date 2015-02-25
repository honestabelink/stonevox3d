package stonevox.decorator;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.newdawn.slick.Color;

import stonevox.Program;
import stonevox.data.GUIdecorator;
import stonevox.data.Matrix;
import stonevox.data.Shader;

public class MultiColorBackground implements GUIdecorator
{

	private boolean enabled = true;
	private Color[] color = new Color[4];

	private int vertexbufferID;
	private FloatBuffer vertexbuffer;

	private int aobufferID;

	private int indexID;

	private boolean firstPaint = true;

	private float lastwidth = 0;
	private float lastheight = 0;

	public MultiColorBackground(Color color1, Color color2, Color color3, Color color4)
	{
		setColor(0, color1);
		setColor(1, color2);
		setColor(2, color3);
		setColor(3, color4);

		vertexbuffer = BufferUtils.createFloatBuffer(28);
		vertexbuffer.put(new float[28]);
		vertexbuffer.flip();

		vertexbufferID = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexbufferID);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexbuffer, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		IntBuffer indexbuff = BufferUtils.createIntBuffer(6);
		indexbuff.put(0);
		indexbuff.put(1);
		indexbuff.put(2);
		indexbuff.put(0);
		indexbuff.put(2);
		indexbuff.put(3);
		indexbuff.flip();

		indexID = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexID);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbuff, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		aobufferID = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(aobufferID);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexbufferID);
		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(Program.shader.GetAttributeAcces("position\0"), 3, GL11.GL_FLOAT, false, 7 << 2, 0l);
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(Program.shader.GetAttributeAcces("color\0"), 3, GL11.GL_FLOAT, false, 7 << 2, 3 << 2);
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(Program.shader.GetAttributeAcces("light\0"), 1, GL11.GL_FLOAT, false, 7 << 2, 6 << 2);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexID);
		GL30.glBindVertexArray(0);
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public void paint(float x, float y, float width, float height)
	{
		Program.shader.UseShader();
		Program.shader.WriteUniformMatrix4("modelview\0", Matrix.CreateTranslation(x, y, 0).GetBuffer());

		GL30.glBindVertexArray(aobufferID);
		GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0l);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		GL30.glBindVertexArray(0);

		Shader.ResetUseShader();
	}

	public void dispose()
	{
	}

	public void setColor(int index, Color color)
	{
		this.color[index] = color;
	}

	public void setColor(Color... colors)
	{
		for (int i = 0; i < colors.length; i++)
			setColor(i, colors[i]);
	}

	public void updateGradient()
	{
		updateGradient(lastwidth, lastheight);
	}

	public void updateGradient(float width, float height)
	{
		float[] vertexdata = new float[]
		{
				0, 0, 0, color[0].r, color[0].g, color[0].b, 1f,

				width, 0, 0, color[1].r, color[1].g, color[1].b, 1f,

				width, height, 0, color[2].r, color[2].g, color[2].b, 1f,

				0, height, 0, color[3].r, color[3].g, color[3].b, 1f
		};
		vertexbuffer.put(vertexdata);
		vertexbuffer.flip();

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexbufferID);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexbuffer, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		lastheight = height;
		lastwidth = width;
	}
}
