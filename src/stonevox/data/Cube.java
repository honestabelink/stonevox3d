package stonevox.data;

import org.lwjgl.util.Color;

public class Cube
{
	final float						sizex		= 0.5f;
	final float						sizey		= 0.5f;
	final float						sizez		= 0.5f;

	static final Vector3			UP			= new Vector3(0, 1, 0);
	static final Vector3			DOWN		= new Vector3(0, -1, 0);
	static final Vector3			LEFT		= new Vector3(-1, 0, 0);
	static final Vector3			RIGHT		= new Vector3(1, 0, 0);
	static final Vector3			FORWARD		= new Vector3(0, 0, 1);
	static final Vector3			BACKWARD	= new Vector3(0, 0, -1);

	public Vector3					pos;
	public Color					color;
	public org.newdawn.slick.Color	fcolor;

	public CubeSide					top;
	public CubeSide					bottom;
	public CubeSide					left;
	public CubeSide					right;
	public CubeSide					front;
	public CubeSide					back;

	public boolean					isDirty;

	public Cube()
	{
		pos = new Vector3();
		color = new Color();
		fcolor = new org.newdawn.slick.Color(1, 1, 1, 0);

		top = new CubeSide(this, 1f);
		bottom = new CubeSide(this, 1f);
		left = new CubeSide(this, .985f);
		right = new CubeSide(this, .985f);
		front = new CubeSide(this, .970f);
		back = new CubeSide(this, .970f);

		top.normal = UP;
		bottom.normal = DOWN;
		left.normal = LEFT;
		right.normal = RIGHT;
		front.normal = FORWARD;
		back.normal = BACKWARD;
	}

	public void setPos(float x, float y, float z)
	{
		pos.x = x;
		pos.y = y;
		pos.z = z;

		generateBackFace();
		generateFrontFace();
		generateTopFace();
		generateBottomFace();
		generateLeftFace();
		generateRightFace();
	}

	public void setColor(int r, int g, int b, int a)
	{
		r = r < 0 ? color.getRed() : r;
		g = g < 0 ? color.getGreen() : g;
		b = b < 0 ? color.getBlue() : b;
		color.set(r, g, b, a);

		fcolor.r = r / 256f;
		fcolor.g = g / 256f;
		fcolor.b = b / 256f;
		fcolor.a = 1f;

		isDirty = true;
	}

	public void setColor(org.newdawn.slick.Color tcolor)
	{
		color.set((int) (tcolor.r * tcolor.a * 256f), (int) (tcolor.g * tcolor.a * 256f),
				(int) (tcolor.b * tcolor.a * 256f), 255);

		fcolor.r = tcolor.r * tcolor.a;
		fcolor.g = tcolor.g * tcolor.a;
		fcolor.b = tcolor.b * tcolor.a;
		fcolor.a = 1f;

		isDirty = true;
	}

	public void setAlpha(int value)
	{
		color.setAlpha(value);
	}

	public void generateface(Side side)
	{
		switch (side)
		{
			case FRONT:
				generateFrontFace();
				break;

			case BACK:
				generateBackFace();
				break;

			case TOP:
				generateTopFace();
				break;

			case BOTTOM:
				generateBottomFace();
				break;

			case LEFT:
				generateLeftFace();
				break;

			case RIGHT:
				generateRightFace();
				break;
		}
	}

	// changes in a lot of face generation for lighting, no culling
	private void generateFrontFace()
	{
		front.setVertex(0, -sizex + pos.x, -sizey + pos.y, sizez + pos.z);
		front.setVertex(1, sizex + pos.x, -sizey + pos.y, sizez + pos.z);
		front.setVertex(2, sizex + pos.x, sizey + pos.y, sizez + pos.z);
		front.setVertex(3, -sizex + pos.x, sizey + pos.y, sizez + pos.z);
	}

	private void generateBackFace()
	{
		// back.setVertex(0, -sizex + pos.x, -sizey + pos.y, -sizez + pos.z);
		// back.setVertex(1, -sizex + pos.x, sizey + pos.y, -sizez + pos.z);
		// back.setVertex(2, sizex + pos.x, sizey + pos.y, -sizez + pos.z);
		// back.setVertex(3, sizex + pos.x, -sizey + pos.y, -sizez + pos.z);

		back.setVertex(0, -sizex + pos.x, -sizey + pos.y, -sizez + pos.z);
		back.setVertex(1, sizex + pos.x, -sizey + pos.y, -sizez + pos.z);
		back.setVertex(2, sizex + pos.x, sizey + pos.y, -sizez + pos.z);
		back.setVertex(3, -sizex + pos.x, sizey + pos.y, -sizez + pos.z);
	}

	private void generateTopFace()
	{
		top.setVertex(0, -sizex + pos.x, sizey + pos.y, sizez + pos.z);
		top.setVertex(1, sizex + pos.x, sizey + pos.y, sizez + pos.z);
		top.setVertex(2, sizex + pos.x, sizey + pos.y, -sizez + pos.z);
		top.setVertex(3, -sizex + pos.x, sizey + pos.y, -sizez + pos.z);
	}

	private void generateBottomFace()
	{
		// bottom.setVertex(0, -sizex + pos.x, -sizey + pos.y, sizez + pos.z);
		// bottom.setVertex(1, -sizex + pos.x, -sizey + pos.y, -sizez + pos.z);
		// bottom.setVertex(2, sizex + pos.x, -sizey + pos.y, -sizez + pos.z);
		// bottom.setVertex(3, sizex + pos.x, -sizey + pos.y, sizez + pos.z);

		bottom.setVertex(0, -sizex + pos.x, -sizey + pos.y, sizez + pos.z);
		bottom.setVertex(1, sizex + pos.x, -sizey + pos.y, sizez + pos.z);
		bottom.setVertex(2, sizex + pos.x, -sizey + pos.y, -sizez + pos.z);
		bottom.setVertex(3, -sizex + pos.x, -sizey + pos.y, -sizez + pos.z);
	}

	private void generateLeftFace()
	{
		// left.setVertex(0, -sizex + pos.x, -sizey + pos.y, sizez + pos.z);
		// left.setVertex(1, -sizex + pos.x, sizey + pos.y, sizez + pos.z);
		// left.setVertex(2, -sizex + pos.x, sizey + pos.y, -sizez + pos.z);
		// left.setVertex(3, -sizex + pos.x, -sizey + pos.y, -sizez + pos.z);

		left.setVertex(0, -sizex + pos.x, -sizey + pos.y, -sizez + pos.z);
		left.setVertex(1, -sizex + pos.x, -sizey + pos.y, sizez + pos.z);
		left.setVertex(2, -sizex + pos.x, sizey + pos.y, sizez + pos.z);
		left.setVertex(3, -sizex + pos.x, sizey + pos.y, -sizez + pos.z);
	}

	private void generateRightFace()
	{
		// right.setVertex(0, sizex + pos.x, -sizey + pos.y, -sizez + pos.z);
		// right.setVertex(1, sizex + pos.x, sizey + pos.y, -sizez + pos.z);
		// right.setVertex(2, sizex + pos.x, sizey + pos.y, sizez + pos.z);
		// right.setVertex(3, sizex + pos.x, -sizey + pos.y, sizez + pos.z);

		right.setVertex(0, sizex + pos.x, -sizey + pos.y, -sizez + pos.z);
		right.setVertex(1, sizex + pos.x, -sizey + pos.y, sizez + pos.z);
		right.setVertex(2, sizex + pos.x, sizey + pos.y, sizez + pos.z);
		right.setVertex(3, sizex + pos.x, sizey + pos.y, -sizez + pos.z);
	}
}
