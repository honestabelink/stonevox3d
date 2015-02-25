package stonevox.decorator;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;

import stonevox.data.GUIdecorator;

public class PlainBackground implements GUIdecorator
{
	public Color color;

	private boolean enabled = true;

	public PlainBackground(Color c)
	{
		color = c;
	}

	public void paint(float x, float y, float width, float height)
	{
		GL11.glColor3f(color.r * color.a, color.g * color.a, color.b * color.a);

		GL11.glBegin(GL11.GL_QUADS);

		GL11.glVertex2f(x, y);
		GL11.glVertex2f(x + width, y);
		GL11.glVertex2f(x + width, y + height);
		GL11.glVertex2f(x, y + height);

		GL11.glEnd();
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public void dispose()
	{

	}

}
