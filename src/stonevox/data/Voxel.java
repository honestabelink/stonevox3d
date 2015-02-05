package stonevox.data;

public class Voxel
{
	public int colorid;
	public int alphamask;

	public int bufferindex;

	public Vector3 position;
	public boolean dirty;

	public Voxel()
	{
		position = new Vector3();
	}
}
