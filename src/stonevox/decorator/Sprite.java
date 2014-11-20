package stonevox.decorator;

import org.lwjgl.opengl.GL11;

import stonevox.data.GUIdecorator;
import stonevox.data.GUIelement;
import stonevox.data.Vector3;
import stonevox.util.TextureUtil;

public class Sprite implements GUIdecorator
{
	private int		texHandleID;

	public boolean	visible	= true;

	private int		twidth;
	private int		theight;

	private boolean	enabled	= true;

	public Sprite(String path)
	{
		Vector3 data = TextureUtil.loadFile(path);
		texHandleID = (int) data.x;
		twidth = (int) data.y;
		theight = (int) data.z;
	}

	public Sprite(String path, GUIelement parent)
	{
		Vector3 data = TextureUtil.loadFile(path);
		texHandleID = (int) data.x;
		twidth = (int) data.y;
		theight = (int) data.z;

		parent.setSize(twidth, theight);
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
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(1, 1, 1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texHandleID);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0f, 1f);
		GL11.glVertex2f(x, y);
		GL11.glTexCoord2f(1f, 1f);
		GL11.glVertex2f(x + width, y);
		GL11.glTexCoord2f(1f, 0f);
		GL11.glVertex2f(x + width, y + height);
		GL11.glTexCoord2f(0f, 0f);
		GL11.glVertex2f(x, y + height);
		GL11.glEnd();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}

	public void dispose()
	{
		GL11.glDeleteTextures(texHandleID);
	}
}
