package stonevox.decorator;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;

import stonevox.data.GUIdecorator;
import stonevox.util.Scale;

public class PlainMarker implements GUIdecorator
{
	static float s = (float) Scale.hSizeScale(10f);
	static float ss = (float) Scale.vSizeScale(10f);

	private boolean enabled = true;

	private float x;
	private float y;
	private Color color = Color.black;

	public PlainMarker()
	{
		s = (float) Scale.hSizeScale(10f);
		ss = (float) Scale.vSizeScale(10f);
	}

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	@Override
	public void paint(float x, float y, float width, float height)
	{
		GL11.glColor3f(color.r * color.a, color.g * color.a, color.b * color.a);

		GL11.glBegin(GL11.GL_QUADS);

		GL11.glVertex2f(this.x + x, this.y + y);
		GL11.glVertex2f(this.x + s + x, this.y + y);
		GL11.glVertex2f(this.x + s + x, this.y + ss + y);
		GL11.glVertex2f(this.x + x, this.y + ss + y);

		GL11.glEnd();
	}

	@Override
	public void dispose()
	{
	}

	public void setPosition(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public void setPositionColor(float x, float y, Color color)
	{
		setPosition(x, y);
		setColor(color);
	}
}
