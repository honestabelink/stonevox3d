package stonevox.decorator;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;

import stonevox.data.GUIdecorator;
import stonevox.util.Scale;

public class ColorSliderDec implements GUIdecorator
{

	public boolean enabled = true;

	private Color bg;
	private Color border;

	private float w;
	private float h;

	public ColorSliderDec(Color bg, Color border, float width, float height)
	{
		this.bg = bg;
		this.border = border;

		this.w = (float) Scale.hSizeScale(width);
		this.h = (float) Scale.vSizeScale(height);
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
		GL11.glColor3f(bg.r * bg.a, bg.g * bg.a, bg.b * bg.a);

		GL11.glBegin(GL11.GL_QUADS);

		GL11.glVertex2f(x - w - w / 3f, y);
		GL11.glVertex2f(x - w + w - w / 3f, y);
		GL11.glVertex2f(x - w + w - w / 3f, y + h);
		GL11.glVertex2f(x - w - w / 3f, y + h);

		GL11.glVertex2f(x + width + w / 3f, y);
		GL11.glVertex2f(x + width + w + w / 3f, y);
		GL11.glVertex2f(x + width + w + w / 3f, y + h);
		GL11.glVertex2f(x + width + w / 3f, y + h);

		GL11.glEnd();
	}

	@Override
	public void dispose()
	{
	}

}
