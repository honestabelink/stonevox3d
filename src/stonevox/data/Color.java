package stonevox.data;

public class Color
{
	public float r;
	public float g;
	public float b;
	public int a;

	public Color(float r, float g, float b, int a)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public static Color FromNEWDAWN(org.newdawn.slick.Color c)
	{
		return new Color(c.r, c.g, c.b, 0);
	}
}
