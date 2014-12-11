package stonevox.util;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import stonevox.Program;
import stonevox.data.Vector3;

public class RaycastingUtil
{
	public static Vector3 near = new Vector3();
	public static Vector3 far = new Vector3();

	public static Vector3 unproject(int mouseX, int mouseY)
	{
		near = unproject(mouseX, mouseY, 0f); // near
		far = unproject(mouseX, mouseY, 1f); // far

		Vector3 normalized = new Vector3(far.x - near.x, far.y - near.y, far.z - near.z);

		return normalized;
	}

	static Vector3 unproject(int mouseX, int mouseY, float depth)
	{
		IntBuffer viewport = BufferUtils.createIntBuffer(16);
		FloatBuffer modelView = Program.camera.RayCastLookAtMatrix().GetBuffer();
		FloatBuffer projectionView = Program.camera.projection.GetBuffer();
		float winX = mouseX;
		float winY = mouseY;
		FloatBuffer position = BufferUtils.createFloatBuffer(3);

		// GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionView);
		GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

		winY = Program.height - mouseY; // due to inverted coords

		GLU.gluUnProject(winX, winY, depth, modelView, projectionView, viewport, position);
		position.rewind();
		Vector3 result = new Vector3(position.get(0), -position.get(1), position.get(2));

		return result;
	}

	static float dot;
	static float t;
	static float coordRatio;
	static Vector3 intPoint = new Vector3();
	static float intX;
	static float intY;
	static float intZ;
	static final float accuraty = 0.0008f;
	public static Vector3 rayOrigin;
	public static Vector3 rayDirection;
	// static final float accuraty = 0.0008f;
	// static final float accuraty = 0.0005f;

	static float fullArea;
	static float subTriangle1;
	static float subTriangle2;
	static float subTriangle3;
	static float totalSubAreas;

	public static Vector3 rayTest(Vector3 rayOrigin, Vector3 rayDirection, Vector3 planeNormal, Vector3 trianglePoint1,
			Vector3 trianglePoint2, Vector3 trianglePoint3)
	{
		dot = rayDirection.x * planeNormal.x + rayDirection.y * planeNormal.y + rayDirection.z * planeNormal.z;
		t = 0;
		if (dot == 0)
		{
			return null;
		}
		coordRatio =
				trianglePoint1.x * planeNormal.x + trianglePoint1.y * planeNormal.y + trianglePoint1.z * planeNormal.z
						- planeNormal.x * rayOrigin.x - planeNormal.y * rayOrigin.y - planeNormal.z * rayOrigin.z;
		t = coordRatio / dot;
		if (t < 0)
		{
			return null;
		}

		intX = rayOrigin.x + t * rayDirection.x;
		intY = rayOrigin.y + t * rayDirection.y;
		intZ = rayOrigin.z + t * rayDirection.z;

		intPoint = new Vector3(intX, intY, intZ);

		fullArea = calculateTriangleArea(trianglePoint1, trianglePoint2, trianglePoint3);
		subTriangle1 = calculateTriangleArea(trianglePoint1, trianglePoint2, intPoint);

		subTriangle2 = calculateTriangleArea(trianglePoint2, trianglePoint3, intPoint);

		subTriangle3 = calculateTriangleArea(trianglePoint1, trianglePoint3, intPoint);

		totalSubAreas = subTriangle1 + subTriangle2 + subTriangle3;

		if (Math.abs(fullArea - totalSubAreas) < accuraty)
		{
			return intPoint;
		}
		else
		{
			return null;
		}

	}

	public static Vector3 rayTest(Vector3 planeNormal, float p1x, float p1y, float p1z, float p2x, float p2y,
			float p2z, float p3x, float p3y, float p3z)
	{
		dot = rayDirection.x * planeNormal.x + rayDirection.y * planeNormal.y + rayDirection.z * planeNormal.z;
		t = 0;
		if (dot == 0)
		{
			return null;
		}
		coordRatio =
				p1x * planeNormal.x + p1y * planeNormal.y + p1z * planeNormal.z - planeNormal.x * rayOrigin.x
						- planeNormal.y * rayOrigin.y - planeNormal.z * rayOrigin.z;
		t = coordRatio / dot;
		if (t < 0)
		{
			return null;
		}

		intPoint.x = rayOrigin.x + t * rayDirection.x;
		intPoint.y = rayOrigin.y + t * rayDirection.y;
		intPoint.z = rayOrigin.z + t * rayDirection.z;

		fullArea = calculateTriangleArea(p1x, p1y, p1z, p2x, p2y, p2z, p3x, p3y, p3z);
		subTriangle1 = calculateTriangleArea(p1x, p1y, p1z, p2x, p2y, p2z, intPoint.x, intPoint.y, intPoint.z);
		subTriangle2 = calculateTriangleArea(p2x, p2y, p2z, p3x, p3y, p3z, intPoint.x, intPoint.y, intPoint.z);
		subTriangle3 = calculateTriangleArea(p1x, p1y, p1z, p3x, p3y, p3z, intPoint.x, intPoint.y, intPoint.z);

		totalSubAreas = subTriangle1 + subTriangle2 + subTriangle3;

		if (Math.abs(fullArea - totalSubAreas) < accuraty)
		{
			return intPoint;
		}
		else
		{
			return null;
		}

	}

	public static Vector3 calculateTriangleNormal(Vector3 p1, Vector3 p2, Vector3 p3)
	{
		Vector3 u = Vector3.sub(p2, p1);
		Vector3 v = Vector3.sub(p3, p1);

		Vector3 retur = new Vector3();
		retur.x = (u.y * v.z) - (u.z * v.y);
		retur.y = (u.z * v.x) - (u.x * v.z);
		retur.z = (u.x * v.y) - (u.y * v.x);

		return retur;
	}

	private static float calculateTriangleArea(float p1x, float p1y, float p1z, float p2x, float p2y, float p2z,
			float p3x, float p3y, float p3z)
	{
		float a = (float) Math.sqrt((p2x - p1x) * (p2x - p1x) + (p2y - p1y) * (p2y - p1y) + (p2z - p1z) * (p2z - p1z));
		float b = (float) Math.sqrt((p3x - p2x) * (p3x - p2x) + (p3y - p2y) * (p3y - p2y) + (p3z - p2z) * (p3z - p2z));
		float c = (float) Math.sqrt((p3x - p1x) * (p3x - p1x) + (p3y - p1y) * (p3y - p1y) + (p3z - p1z) * (p3z - p1z));
		float s = (a + b + c) / 2;
		float result = (float) Math.sqrt(s * (s - a) * (s - b) * (s - c));
		return result;
	}

	private static float calculateTriangleArea(Vector3 p1, Vector3 p2, Vector3 p3)
	{
		float a =
				(float) Math.sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y) + (p2.z - p1.z)
						* (p2.z - p1.z));
		float b =
				(float) Math.sqrt((p3.x - p2.x) * (p3.x - p2.x) + (p3.y - p2.y) * (p3.y - p2.y) + (p3.z - p2.z)
						* (p3.z - p2.z));
		float c =
				(float) Math.sqrt((p3.x - p1.x) * (p3.x - p1.x) + (p3.y - p1.y) * (p3.y - p1.y) + (p3.z - p1.z)
						* (p3.z - p1.z));
		float s = (a + b + c) / 2;
		float result = (float) Math.sqrt(s * (s - a) * (s - b) * (s - c));
		return result;
	}

	public static boolean rayTestSphere(Vector3 rayOrigin, Vector3 rayDirection, Vector3 sphereOrigin,
			float sphereRadius)
	{
		Vector3 Q = new Vector3(sphereOrigin);
		Q = Vector3.sub(Q, rayOrigin);

		float c = Q.length();
		float v = Dot(Q, rayDirection);
		float d = sphereRadius * sphereRadius - (c * c - v * v);

		if (d < 0.0)
		{
			return false;
		}
		return true;
	}

	public static float Length(Vector3 t)
	{
		float num = t.x * t.x + t.y * t.y + t.z * t.z;
		return (float) Math.sqrt((double) num);
	}

	public static float Distance(float x, float y, float z)
	{
		float num = rayOrigin.x - x;
		float num2 = rayOrigin.y - y;
		float num3 = rayOrigin.z - z;
		float num4 = num * num + num2 * num2 + num3 * num3;
		return (float) Math.sqrt((double) num4);
	}

	public static float Distance(Vector3 value1, Vector3 value2)
	{
		float num = value1.x - value2.x;
		float num2 = value1.y - value2.y;
		float num3 = value1.z - value2.z;
		float num4 = num * num + num2 * num2 + num3 * num3;
		return (float) Math.sqrt((double) num4);
	}

	public static float Dot(Vector3 vector1, Vector3 vector2)
	{
		return vector1.x * vector2.x + vector1.y * vector2.y + vector1.z * vector2.z;
	}

	static Vector3 SubNewVector3(Vector3 vec1, Vector3 vec2)
	{
		Vector3 t = new Vector3(vec1.x, vec1.y, vec1.z);
		t.x -= vec2.x;
		t.y -= vec2.y;
		t.z -= vec2.z;
		return t;
	}

	static Vector3 MulVector3(Vector3 vec1, float value)
	{
		Vector3 vec2 = new Vector3(vec1.x, vec1.y, vec1.z);
		vec2.x *= value;
		vec2.y *= value;
		vec2.z *= value;

		return vec2;
	}

	static Vector3 AddNewVector3(Vector3 vec1, Vector3 vec2)
	{
		Vector3 t = new Vector3(vec1.x, vec1.y, vec1.z);
		t.x += vec2.x;
		t.y += vec2.y;
		t.z += vec2.z;
		return t;
	}

	public static Vector3 Normalize(Vector3 value)
	{
		float num = value.x * value.x + value.y * value.y + value.z * value.z;
		float num2 = 1f / (float) Math.sqrt((double) num);
		Vector3 result = new Vector3();
		result.x = value.x * num2;
		result.y = value.y * num2;
		result.z = value.z * num2;
		return result;
	}
}
