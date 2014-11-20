package stonevox.data;

import stonevox.util.RaycastingUtil;

public class CubeSide
{
	public Vector3[]	vertex;
	public float[]		lightmap;
	public Vector3		normal;

	private Cube		parent;

	public int			bufferindex;
	public float		lightscale;

	public CubeSide(Cube parent, float lightscale)
	{
		this.parent = parent;
		vertex = new Vector3[4];
		vertex[0] = new Vector3();
		vertex[1] = new Vector3();
		vertex[2] = new Vector3();
		vertex[3] = new Vector3();

		lightmap = new float[]
		{
				1f, 1f, 1f, 1f
		};
		this.lightscale = lightscale;
	}

	public void setVertex(int index, float x, float y, float z)
	{
		Vector3 v = vertex[index];
		v.x = x;
		v.y = y;
		v.z = z;
	}

	public int setDataIntoBuffer(int index, float[] buffer)
	{
		this.bufferindex = index;
		float r = parent.fcolor.r;
		float g = parent.fcolor.g;
		float b = parent.fcolor.b;

		buffer[index] = vertex[0].x;
		buffer[index + 1] = vertex[0].y;
		buffer[index + 2] = vertex[0].z;
		buffer[index + 3] = r;
		buffer[index + 4] = g;
		buffer[index + 5] = b;
		buffer[index + 6] = lightmap[0] * lightscale;

		buffer[index + 7] = vertex[1].x;
		buffer[index + 8] = vertex[1].y;
		buffer[index + 9] = vertex[1].z;
		buffer[index + 10] = r;
		buffer[index + 11] = g;
		buffer[index + 12] = b;
		buffer[index + 13] = lightmap[1] * lightscale;

		buffer[index + 14] = vertex[2].x;
		buffer[index + 15] = vertex[2].y;
		buffer[index + 16] = vertex[2].z;
		buffer[index + 17] = r;
		buffer[index + 18] = g;
		buffer[index + 19] = b;
		buffer[index + 20] = lightmap[2] * lightscale;

		buffer[index + 21] = vertex[3].x;
		buffer[index + 22] = vertex[3].y;
		buffer[index + 23] = vertex[3].z;
		buffer[index + 24] = r;
		buffer[index + 25] = g;
		buffer[index + 26] = b;
		buffer[index + 27] = lightmap[3] * lightscale;

		return index + 28;
	}

	public int setDataIntoBuffer(int index, float[] buffer, float sizeX, float sizeY, float sizeZ)
	{
		this.bufferindex = index;
		float r = parent.fcolor.r;
		float g = parent.fcolor.g;
		float b = parent.fcolor.b;

		buffer[index] = vertex[0].x * sizeX;
		buffer[index + 1] = vertex[0].y * sizeY;
		buffer[index + 2] = vertex[0].z * sizeZ;
		buffer[index + 3] = r;
		buffer[index + 4] = g;
		buffer[index + 5] = b;
		buffer[index + 6] = lightmap[0] * lightscale;

		buffer[index + 7] = vertex[1].x * sizeX;
		buffer[index + 8] = vertex[1].y * sizeY;
		buffer[index + 9] = vertex[1].z * sizeZ;
		buffer[index + 10] = r;
		buffer[index + 11] = g;
		buffer[index + 12] = b;
		buffer[index + 13] = lightmap[1] * lightscale;

		buffer[index + 14] = vertex[2].x * sizeX;
		buffer[index + 15] = vertex[2].y * sizeY;
		buffer[index + 16] = vertex[2].z * sizeZ;
		buffer[index + 17] = r;
		buffer[index + 18] = g;
		buffer[index + 19] = b;
		buffer[index + 20] = lightmap[2] * lightscale;

		buffer[index + 21] = vertex[3].x * sizeX;
		buffer[index + 22] = vertex[3].y * sizeY;
		buffer[index + 23] = vertex[3].z * sizeZ;
		buffer[index + 24] = r;
		buffer[index + 25] = g;
		buffer[index + 26] = b;
		buffer[index + 27] = lightmap[3] * lightscale;

		return index + 28;
	}

	public void ReBuferDate(float[] buffer)
	{
		int index = bufferindex;
		float r = parent.fcolor.r;
		float g = parent.fcolor.g;
		float b = parent.fcolor.b;

		buffer[index] = vertex[0].x;
		buffer[index + 1] = vertex[0].y;
		buffer[index + 2] = vertex[0].z;
		buffer[index + 3] = r;
		buffer[index + 4] = g;
		buffer[index + 5] = b;
		buffer[index + 6] = lightmap[0];

		buffer[index + 7] = vertex[1].x;
		buffer[index + 8] = vertex[1].y;
		buffer[index + 9] = vertex[1].z;
		buffer[index + 10] = r;
		buffer[index + 11] = g;
		buffer[index + 12] = b;
		buffer[index + 13] = lightmap[1];

		buffer[index + 14] = vertex[2].x;
		buffer[index + 15] = vertex[2].y;
		buffer[index + 16] = vertex[2].z;
		buffer[index + 17] = r;
		buffer[index + 18] = g;
		buffer[index + 19] = b;
		buffer[index + 20] = lightmap[2];

		buffer[index + 21] = vertex[3].x;
		buffer[index + 22] = vertex[3].y;
		buffer[index + 23] = vertex[3].z;
		buffer[index + 24] = r;
		buffer[index + 25] = g;
		buffer[index + 26] = b;
		buffer[index + 27] = lightmap[3];
	}

	public boolean RayTest(Vector3 origin, Vector3 projection)
	{
		Vector3 result = RaycastingUtil.rayTest(origin, projection, normal, vertex[0], vertex[1], vertex[2]);

		if (result == null)
		{
			result = RaycastingUtil.rayTest(origin, projection, normal, vertex[0], vertex[2], vertex[3]);
		}

		return result != null;
	}
}
