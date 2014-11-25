package stonevox.data;


public class Quaternion
{
	public float				X;
	public float				Y;
	public float				Z;
	public float				W;
	public static Quaternion	identity	= new Quaternion(0f, 0f, 0f, 1f);

	public Quaternion()
	{
	}

	public Quaternion(float x, float y, float z, float w)
	{
		this.X = x;
		this.Y = y;
		this.Z = z;
		this.W = w;
	}

	public Quaternion(Vector3 vectorPart, float scalarPart)
	{
		this.X = vectorPart.x;
		this.Y = vectorPart.y;
		this.Z = vectorPart.z;
		this.W = scalarPart;
	}

	public static void CreateFromYawPitchRoll(float yaw, float pitch,
			float roll, Quaternion dist)
	{
		float num = roll * 0.5f;
		float num2 = (float) Math.sin((double) num);
		float num3 = (float) Math.cos((double) num);
		float num4 = pitch * 0.5f;
		float num5 = (float) Math.sin((double) num4);
		float num6 = (float) Math.cos((double) num4);
		float num7 = yaw * 0.5f;
		float num8 = (float) Math.sin((double) num7);
		float num9 = (float) Math.cos((double) num7);
		dist.X = num9 * num5 * num3 + num8 * num6 * num2;
		dist.Y = num8 * num6 * num3 - num9 * num5 * num2;
		dist.Z = num9 * num6 * num2 - num8 * num5 * num3;
		dist.W = num9 * num6 * num3 + num8 * num5 * num2;
	}
}
