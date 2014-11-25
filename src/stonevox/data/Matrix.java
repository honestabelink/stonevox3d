package stonevox.data;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class Matrix
{
	public float M11;
	public float M12;
	public float M13;
	public float M14;
	public float M21;
	public float M22;
	public float M23;
	public float M24;
	public float M31;
	public float M32;
	public float M33;
	public float M34;
	public float M41;
	public float M42;
	public float M43;
	public float M44;
	public static Matrix identity = new Matrix(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f);

	public Matrix()
	{
	}

	public Matrix(float m11, float m12, float m13, float m14, float m21, float m22, float m23, float m24, float m31,
			float m32, float m33, float m34, float m41, float m42, float m43, float m44)
	{
		this.M11 = m11;
		this.M12 = m12;
		this.M13 = m13;
		this.M14 = m14;
		this.M21 = m21;
		this.M22 = m22;
		this.M23 = m23;
		this.M24 = m24;
		this.M31 = m31;
		this.M32 = m32;
		this.M33 = m33;
		this.M34 = m34;
		this.M41 = m41;
		this.M42 = m42;
		this.M43 = m43;
		this.M44 = m44;
	}

	public FloatBuffer GetBuffer()
	{
		FloatBuffer f = BufferUtils.createFloatBuffer(16);
		f.put(this.M11);
		f.put(this.M12);
		f.put(this.M13);
		f.put(this.M14);
		f.put(this.M21);
		f.put(this.M22);
		f.put(this.M23);
		f.put(this.M24);
		f.put(this.M31);
		f.put(this.M32);
		f.put(this.M33);
		f.put(this.M34);
		f.put(this.M41);
		f.put(this.M42);
		f.put(this.M43);
		f.put(this.M44);

		f.flip();

		return f;
	}

	public static Matrix CreateFromYawPitchRoll(float yaw, float pitch, float roll)
	{
		Quaternion quaternion = new Quaternion();
		Quaternion.CreateFromYawPitchRoll(yaw, pitch, roll, quaternion);
		Matrix result = new Matrix();
		Matrix.CreateFromQuaternion(quaternion, result);
		return result;
	}

	public static void CreateFromQuaternion(Quaternion quaternion, Matrix result)
	{
		float num = quaternion.X * quaternion.X;
		float num2 = quaternion.Y * quaternion.Y;
		float num3 = quaternion.Z * quaternion.Z;
		float num4 = quaternion.X * quaternion.Y;
		float num5 = quaternion.Z * quaternion.W;
		float num6 = quaternion.Z * quaternion.X;
		float num7 = quaternion.Y * quaternion.W;
		float num8 = quaternion.Y * quaternion.Z;
		float num9 = quaternion.X * quaternion.W;
		result.M11 = 1f - 2f * (num2 + num3);
		result.M12 = 2f * (num4 + num5);
		result.M13 = 2f * (num6 - num7);
		result.M14 = 0f;
		result.M21 = 2f * (num4 - num5);
		result.M22 = 1f - 2f * (num3 + num);
		result.M23 = 2f * (num8 + num9);
		result.M24 = 0f;
		result.M31 = 2f * (num6 + num7);
		result.M32 = 2f * (num8 - num9);
		result.M33 = 1f - 2f * (num2 + num);
		result.M34 = 0f;
		result.M41 = 0f;
		result.M42 = 0f;
		result.M43 = 0f;
		result.M44 = 1f;
	}

	public static Matrix CreateLookAt(Vector3 cameraPosition, Vector3 cameraTarget, Vector3 cameraUpVector)
	{
		Vector3 vector = Vector3.normalize(Vector3.sub(cameraPosition, cameraTarget));
		Vector3 vector2 = Vector3.normalize(Vector3.cross(cameraUpVector, vector));
		Vector3 vector3 = Vector3.cross(vector, vector2);
		Matrix result = new Matrix();
		result.M11 = vector2.x;
		result.M12 = vector3.x;
		result.M13 = vector.x;
		result.M14 = 0f;
		result.M21 = vector2.y;
		result.M22 = vector3.y;
		result.M23 = vector.y;
		result.M24 = 0f;
		result.M31 = vector2.z;
		result.M32 = vector3.z;
		result.M33 = vector.z;
		result.M34 = 0f;
		result.M41 = -Vector3.dot(vector2, cameraPosition);
		result.M42 = -Vector3.dot(vector3, cameraPosition);
		result.M43 = -Vector3.dot(vector, cameraPosition);
		result.M44 = 1f;
		return result;
	}

	public static Matrix CreatePerspectiveFieldOfView(float fieldOfView, float aspectRatio, float nearPlaneDistance,
			float farPlaneDistance)
	{
		float num = 1f / (float) Math.tan((double) (fieldOfView * 0.5f));
		float m = num / aspectRatio;
		Matrix result = new Matrix();
		result.M11 = m;
		result.M12 = (result.M13 = (result.M14 = 0f));
		result.M22 = num;
		result.M21 = (result.M23 = (result.M24 = 0f));
		result.M31 = (result.M32 = 0f);
		result.M33 = farPlaneDistance / (nearPlaneDistance - farPlaneDistance);
		result.M34 = -1f;
		result.M41 = (result.M42 = (result.M44 = 0f));
		result.M43 = nearPlaneDistance * farPlaneDistance / (nearPlaneDistance - farPlaneDistance);
		return result;
	}

	public static Matrix CreateOrthographicOffCenter(float left, float right, float bottom, float top,
			float zNearPlane, float zFarPlane)
	{
		Matrix result = new Matrix();
		result.M11 = 2f / (right - left);
		result.M12 = (result.M13 = (result.M14 = 0f));
		result.M22 = 2f / (top - bottom);
		result.M21 = (result.M23 = (result.M24 = 0f));
		result.M33 = 1f / (zNearPlane - zFarPlane);
		result.M31 = (result.M32 = (result.M34 = 0f));
		result.M41 = (left + right) / (left - right);
		result.M42 = (top + bottom) / (bottom - top);
		result.M43 = zNearPlane / (zNearPlane - zFarPlane);
		result.M44 = 1f;
		return result;
	}

	public static Matrix CreateRotationX(float radians)
	{
		float num = (float) Math.cos((double) radians);
		float num2 = (float) Math.sin((double) radians);
		Matrix result = new Matrix();
		result.M11 = 1f;
		result.M12 = 0f;
		result.M13 = 0f;
		result.M14 = 0f;
		result.M21 = 0f;
		result.M22 = num;
		result.M23 = num2;
		result.M24 = 0f;
		result.M31 = 0f;
		result.M32 = -num2;
		result.M33 = num;
		result.M34 = 0f;
		result.M41 = 0f;
		result.M42 = 0f;
		result.M43 = 0f;
		result.M44 = 1f;
		return result;
	}

	public static Matrix CreateRotationY(float radians)
	{
		float num = (float) Math.cos((double) radians);
		float num2 = (float) Math.sin((double) radians);
		Matrix result = new Matrix();
		result.M11 = num;
		result.M12 = 0f;
		result.M13 = -num2;
		result.M14 = 0f;
		result.M21 = 0f;
		result.M22 = 1f;
		result.M23 = 0f;
		result.M24 = 0f;
		result.M31 = num2;
		result.M32 = 0f;
		result.M33 = num;
		result.M34 = 0f;
		result.M41 = 0f;
		result.M42 = 0f;
		result.M43 = 0f;
		result.M44 = 1f;
		return result;
	}

	public static Matrix CreateRotationZ(float radians)
	{
		float num = (float) Math.cos((double) radians);
		float num2 = (float) Math.sin((double) radians);
		Matrix result = new Matrix();
		result.M11 = num;
		result.M12 = num2;
		result.M13 = 0f;
		result.M14 = 0f;
		result.M21 = -num2;
		result.M22 = num;
		result.M23 = 0f;
		result.M24 = 0f;
		result.M31 = 0f;
		result.M32 = 0f;
		result.M33 = 1f;
		result.M34 = 0f;
		result.M41 = 0f;
		result.M42 = 0f;
		result.M43 = 0f;
		result.M44 = 1f;
		return result;
	}

	public static Matrix Multiply(Matrix matrix1, Matrix matrix2)
	{
		Matrix result = new Matrix();
		result.M11 =
				matrix1.M11 * matrix2.M11 + matrix1.M12 * matrix2.M21 + matrix1.M13 * matrix2.M31 + matrix1.M14
						* matrix2.M41;
		result.M12 =
				matrix1.M11 * matrix2.M12 + matrix1.M12 * matrix2.M22 + matrix1.M13 * matrix2.M32 + matrix1.M14
						* matrix2.M42;
		result.M13 =
				matrix1.M11 * matrix2.M13 + matrix1.M12 * matrix2.M23 + matrix1.M13 * matrix2.M33 + matrix1.M14
						* matrix2.M43;
		result.M14 =
				matrix1.M11 * matrix2.M14 + matrix1.M12 * matrix2.M24 + matrix1.M13 * matrix2.M34 + matrix1.M14
						* matrix2.M44;
		result.M21 =
				matrix1.M21 * matrix2.M11 + matrix1.M22 * matrix2.M21 + matrix1.M23 * matrix2.M31 + matrix1.M24
						* matrix2.M41;
		result.M22 =
				matrix1.M21 * matrix2.M12 + matrix1.M22 * matrix2.M22 + matrix1.M23 * matrix2.M32 + matrix1.M24
						* matrix2.M42;
		result.M23 =
				matrix1.M21 * matrix2.M13 + matrix1.M22 * matrix2.M23 + matrix1.M23 * matrix2.M33 + matrix1.M24
						* matrix2.M43;
		result.M24 =
				matrix1.M21 * matrix2.M14 + matrix1.M22 * matrix2.M24 + matrix1.M23 * matrix2.M34 + matrix1.M24
						* matrix2.M44;
		result.M31 =
				matrix1.M31 * matrix2.M11 + matrix1.M32 * matrix2.M21 + matrix1.M33 * matrix2.M31 + matrix1.M34
						* matrix2.M41;
		result.M32 =
				matrix1.M31 * matrix2.M12 + matrix1.M32 * matrix2.M22 + matrix1.M33 * matrix2.M32 + matrix1.M34
						* matrix2.M42;
		result.M33 =
				matrix1.M31 * matrix2.M13 + matrix1.M32 * matrix2.M23 + matrix1.M33 * matrix2.M33 + matrix1.M34
						* matrix2.M43;
		result.M34 =
				matrix1.M31 * matrix2.M14 + matrix1.M32 * matrix2.M24 + matrix1.M33 * matrix2.M34 + matrix1.M34
						* matrix2.M44;
		result.M41 =
				matrix1.M41 * matrix2.M11 + matrix1.M42 * matrix2.M21 + matrix1.M43 * matrix2.M31 + matrix1.M44
						* matrix2.M41;
		result.M42 =
				matrix1.M41 * matrix2.M12 + matrix1.M42 * matrix2.M22 + matrix1.M43 * matrix2.M32 + matrix1.M44
						* matrix2.M42;
		result.M43 =
				matrix1.M41 * matrix2.M13 + matrix1.M42 * matrix2.M23 + matrix1.M43 * matrix2.M33 + matrix1.M44
						* matrix2.M43;
		result.M44 =
				matrix1.M41 * matrix2.M14 + matrix1.M42 * matrix2.M24 + matrix1.M43 * matrix2.M34 + matrix1.M44
						* matrix2.M44;
		return result;
	}

	public static Matrix CreateTranslation(Vector3 position)
	{
		Matrix result = new Matrix();
		result.M11 = 1f;
		result.M12 = 0f;
		result.M13 = 0f;
		result.M14 = 0f;
		result.M21 = 0f;
		result.M22 = 1f;
		result.M23 = 0f;
		result.M24 = 0f;
		result.M31 = 0f;
		result.M32 = 0f;
		result.M33 = 1f;
		result.M34 = 0f;
		result.M41 = position.x;
		result.M42 = position.y;
		result.M43 = position.z;
		result.M44 = 1f;
		return result;
	}

	public static Matrix CreateTranslation(float xPosition, float yPosition, float zPosition)
	{
		Matrix result = new Matrix();
		result.M11 = 1f;
		result.M12 = 0f;
		result.M13 = 0f;
		result.M14 = 0f;
		result.M21 = 0f;
		result.M22 = 1f;
		result.M23 = 0f;
		result.M24 = 0f;
		result.M31 = 0f;
		result.M32 = 0f;
		result.M33 = 1f;
		result.M34 = 0f;
		result.M41 = xPosition;
		result.M42 = yPosition;
		result.M43 = zPosition;
		result.M44 = 1f;
		return result;
	}

	public static Matrix Invert(Matrix matrix)
	{
		float m = matrix.M11;
		float m2 = matrix.M12;
		float m3 = matrix.M13;
		float m4 = matrix.M14;
		float m5 = matrix.M21;
		float m6 = matrix.M22;
		float m7 = matrix.M23;
		float m8 = matrix.M24;
		float m9 = matrix.M31;
		float m10 = matrix.M32;
		float m11 = matrix.M33;
		float m12 = matrix.M34;
		float m13 = matrix.M41;
		float m14 = matrix.M42;
		float m15 = matrix.M43;
		float m16 = matrix.M44;
		float num = m11 * m16 - m12 * m15;
		float num2 = m10 * m16 - m12 * m14;
		float num3 = m10 * m15 - m11 * m14;
		float num4 = m9 * m16 - m12 * m13;
		float num5 = m9 * m15 - m11 * m13;
		float num6 = m9 * m14 - m10 * m13;
		float num7 = m6 * num - m7 * num2 + m8 * num3;
		float num8 = -(m5 * num - m7 * num4 + m8 * num5);
		float num9 = m5 * num2 - m6 * num4 + m8 * num6;
		float num10 = -(m5 * num3 - m6 * num5 + m7 * num6);
		float num11 = 1f / (m * num7 + m2 * num8 + m3 * num9 + m4 * num10);
		Matrix result = new Matrix();
		result.M11 = num7 * num11;
		result.M21 = num8 * num11;
		result.M31 = num9 * num11;
		result.M41 = num10 * num11;
		result.M12 = -(m2 * num - m3 * num2 + m4 * num3) * num11;
		result.M22 = (m * num - m3 * num4 + m4 * num5) * num11;
		result.M32 = -(m * num2 - m2 * num4 + m4 * num6) * num11;
		result.M42 = (m * num3 - m2 * num5 + m3 * num6) * num11;
		float num12 = m7 * m16 - m8 * m15;
		float num13 = m6 * m16 - m8 * m14;
		float num14 = m6 * m15 - m7 * m14;
		float num15 = m5 * m16 - m8 * m13;
		float num16 = m5 * m15 - m7 * m13;
		float num17 = m5 * m14 - m6 * m13;
		result.M13 = (m2 * num12 - m3 * num13 + m4 * num14) * num11;
		result.M23 = -(m * num12 - m3 * num15 + m4 * num16) * num11;
		result.M33 = (m * num13 - m2 * num15 + m4 * num17) * num11;
		result.M43 = -(m * num14 - m2 * num16 + m3 * num17) * num11;
		float num18 = m7 * m12 - m8 * m11;
		float num19 = m6 * m12 - m8 * m10;
		float num20 = m6 * m11 - m7 * m10;
		float num21 = m5 * m12 - m8 * m9;
		float num22 = m5 * m11 - m7 * m9;
		float num23 = m5 * m10 - m6 * m9;
		result.M14 = -(m2 * num18 - m3 * num19 + m4 * num20) * num11;
		result.M24 = (m * num18 - m3 * num21 + m4 * num22) * num11;
		result.M34 = -(m * num19 - m2 * num21 + m4 * num23) * num11;
		result.M44 = (m * num20 - m2 * num22 + m3 * num23) * num11;
		return result;
	}

	public static FloatBuffer GetBuffer(Matrix m)
	{
		FloatBuffer f = BufferUtils.createFloatBuffer(16);
		f.put(m.M11);
		f.put(m.M12);
		f.put(m.M13);
		f.put(m.M14);
		f.put(m.M21);
		f.put(m.M22);
		f.put(m.M23);
		f.put(m.M24);
		f.put(m.M31);
		f.put(m.M32);
		f.put(m.M33);
		f.put(m.M34);
		f.put(m.M41);
		f.put(m.M42);
		f.put(m.M43);
		f.put(m.M44);

		f.flip();

		return f;
	}

	private static boolean WithinEpsilon(float a, float b)
	{
		float num = a - b;
		return -1.401298E-45f <= num && num <= 1.401298E-45f;
	}

	public static Vector3 Unproject(Vector3 source, Matrix projection, Matrix view, Matrix world)
	{
		Matrix matrix = Matrix.Multiply(world, view);
		matrix = Matrix.Multiply(matrix, projection);
		matrix = Matrix.Invert(matrix);
		source.x = (source.x) / (float) 1280f * 2f - 1f;
		source.y = -((source.y) / (float) 800f * 2f - 1f);
		source.z = (source.z) / (1f);
		Vector3 vector = Vector3.Transform(source, matrix);
		float num = source.x * matrix.M14 + source.y * matrix.M24 + source.z * matrix.M34 + matrix.M44;
		if (!WithinEpsilon(num, 1f))
		{
			return Vector3.Divide(vector, num);
		}
		return vector;
	}
}
