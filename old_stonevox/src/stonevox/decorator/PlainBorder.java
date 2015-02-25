package stonevox.decorator;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;

import stonevox.data.GUIdecorator;
import stonevox.util.Scale;

public class PlainBorder implements GUIdecorator
{
	private float bodersize;
	private float borderscale;
	public Color color;

	private boolean enabled = true;

	public PlainBorder(float boderSize, Color c)
	{
		this.color = c;
		this.bodersize = boderSize;
		this.borderscale = (float) Scale.vSizeScale(bodersize / 1.8f);
	}

	public void paint(float x, float y, float width, float height)
	{
		GL11.glColor3f(color.r * color.a, color.g * color.a, color.b * color.a);
		GL11.glLineWidth(bodersize);

		GL11.glBegin(GL11.GL_LINES);

		GL11.glVertex2f(x, y);
		GL11.glVertex2f(x + width, y);

		GL11.glVertex2f(x + width - borderscale, y);
		GL11.glVertex2f(x + width - borderscale, y + height);

		GL11.glVertex2f(x, y + height);
		GL11.glVertex2f(x + width, y + height);

		GL11.glVertex2f(x + borderscale, y);
		GL11.glVertex2f(x + borderscale, y + height);

		GL11.glEnd();

		GL11.glEnd();
		GL11.glLineWidth(1);
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
