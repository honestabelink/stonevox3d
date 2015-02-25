package stonevox.data;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Vector3f;

public class Vector3
{
	public static final Vector3 zero = new Vector3(0f, 0f, 0f);
	public static final Vector3 one = new Vector3(1f, 1f, 1f);
	public static final Vector3 up = new Vector3(0f, 1f, 0f);
	public static final Vector3 down = new Vector3(0f, -1f, 0f);
	public static final Vector3 right = new Vector3(1f, 0f, 0f);
	public static final Vector3 left = new Vector3(-1f, 0f, 0f);
	public static final Vector3 forward = new Vector3(0f, 0f, -1f);
	public static final Vector3 backward = new Vector3(0f, 0f, 1f);

	public float x;
	public float y;
	public float z;

	public Vector3()
	{

	}

	public boolean isEqual(Vector3 other)
	{
		return this.x == other.x && this.y == other.y && this.z == other.z;
	}

	public Vector3(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3(Vector3 value)
	{
		this.x = value.x;
		this.y = value.y;
		this.z = value.z;
	}

	public Vector3 add(Vector3 value2)
	{
		this.x = this.x + value2.x;
		this.y = this.y + value2.y;
		this.z = this.z + value2.z;
		return this;
	}

	public Vector3 sub(Vector3 value2)
	{
		this.x = this.x - value2.x;
		this.y = this.y - value2.y;
		this.z = this.z - value2.z;
		return this;
	}

	public Vector3 mul(Vector3 value2)
	{
		this.x = this.x * value2.x;
		this.y = this.y * value2.y;
		this.z = this.z * value2.z;
		return this;
	}

	public Vector3 mul(float value2)
	{
		this.x = this.x * value2;
		this.y = this.y * value2;
		this.z = this.z * value2;
		return this;
	}

	public float length()
	{
		float num = this.x * this.x + this.y * this.y + this.z * this.z;
		return (float) Math.sqrt((double) num);
	}

	public void noramlize()
	{
		float num = this.x * this.x + this.y * this.y + this.z * this.z;
		float num2 = 1f / (float) Math.sqrt((double) num);
		this.x *= num2;
		this.y *= num2;
		this.z *= num2;
	}

	public void cross(Vector3 vector2)
	{
		float x = this.y * vector2.z - this.z * vector2.y;
		float y = this.z * vector2.x - this.x * vector2.z;
		float z = this.x * vector2.y - this.y * vector2.x;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public float dot(Vector3 vector2)
	{
		return this.x * vector2.x + this.y * vector2.y + this.z * vector2.z;
	}

	public Vector3f tovecf()
	{
		return new Vector3f(x, y, z);
	}

	public Side toside()
	{
		if (this.x == 1)
			return Side.RIGHT;
		else if (this.x == -1)
			return Side.LEFT;
		else if (this.y == 1)
			return Side.TOP;
		else if (this.y == -1)
			return Side.BOTTOM;
		else if (this.z == 1)
			return Side.BACK;
		else if (this.z == -1)
			return Side.FRONT;

		return Side.BACK;
	}

	public Color fromVect()
	{
		return new Color((int) (x * 256f), (int) (y * 256), (int) (z * 256));
	}

	public FloatBuffer getBuffer()
	{
		FloatBuffer b = BufferUtils.createFloatBuffer(3);
		b.put(x).put(y).put(z);
		b.flip();
		return b;
	}

	public static Color fromVect(Vector3 vect)
	{
		return new Color((int) (vect.x * 256f), (int) (vect.y * 256), (int) (vect.z * 256));
	}

	public static Vector3 fromColor(Color c)
	{
		return new Vector3((int) (c.getRed() / 256f), (int) (c.getGreen() / 256), (int) (c.getBlue() / 256));
	}

	public static float distance(Vector3 value1, Vector3 value2)
	{
		float num = value1.x - value2.x;
		float num2 = value1.y - value2.y;
		float num3 = value1.z - value2.z;
		float num4 = num * num + num2 * num2 + num3 * num3;
		return (float) Math.sqrt((double) num4);
	}

	public static float dot(Vector3 vector1, Vector3 vector2)
	{
		return vector1.x * vector2.x + vector1.y * vector2.y + vector1.z * vector2.z;
	}

	public static Vector3 normalize(Vector3 value)
	{
		float num = value.x * value.x + value.y * value.y + value.z * value.z;
		float num2 = 1f / (float) Math.sqrt((double) num);
		Vector3 result = new Vector3();
		result.x = value.x * num2;
		result.y = value.y * num2;
		result.z = value.z * num2;
		return result;
	}

	public static Vector3 cross(Vector3 vector1, Vector3 vector2)
	{
		float x = vector1.y * vector2.z - vector1.z * vector2.y;
		float y = vector1.z * vector2.x - vector1.x * vector2.z;
		float z = vector1.x * vector2.y - vector1.y * vector2.x;
		Vector3 result = new Vector3();
		result.x = x;
		result.y = y;
		result.z = z;
		return result;
	}

	public static Vector3 add(Vector3 value1, Vector3 value2)
	{
		Vector3 result = new Vector3();
		result.x = value1.x + value2.x;
		result.y = value1.y + value2.y;
		result.z = value1.z + value2.z;
		return result;
	}

	public static Vector3 sub(Vector3 value1, Vector3 value2)
	{
		Vector3 result = new Vector3();
		result.x = value1.x - value2.x;
		result.y = value1.y - value2.y;
		result.z = value1.z - value2.z;
		return result;
	}

	public static Vector3 mul(Vector3 value1, Vector3 value2)
	{
		Vector3 result = new Vector3();
		result.x = value1.x * value2.x;
		result.y = value1.y * value2.y;
		result.z = value1.z * value2.z;
		return result;
	}

	public static Vector3 mul(Vector3 value1, float value2)
	{
		Vector3 result = new Vector3();
		result.x = value1.x * value2;
		result.y = value1.y * value2;
		result.z = value1.z * value2;
		return result;
	}

	public static Vector3 Divide(Vector3 value1, float value2)
	{
		float num = 1f / value2;
		Vector3 result = new Vector3();
		result.x = value1.x * num;
		result.y = value1.y * num;
		result.z = value1.z * num;
		return result;
	}

	public static Vector3 Transform(Vector3 position, Matrix matrix)
	{
		float x = position.x * matrix.M11 + position.y * matrix.M21 + position.z * matrix.M31 + matrix.M41;
		float y = position.x * matrix.M12 + position.y * matrix.M22 + position.z * matrix.M32 + matrix.M42;
		float z = position.x * matrix.M13 + position.y * matrix.M23 + position.z * matrix.M33 + matrix.M43;
		Vector3 result = new Vector3();
		result.x = x;
		result.y = y;
		result.z = z;
		return result;
	}
}
