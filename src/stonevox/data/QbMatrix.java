package stonevox.data;

public class QbMatrix
{
	private float cubesizex = 0.5f;

	public String name;
	public Vector3 pos;
	public Vector3 size;

	public Vector3 posSize;
	public boolean visible;

	public Matrix transform;
	public Matrix final_transform;

	private RayHitPoint rayhit;

	public Color[][][] cubecolor;
	private QbMatrixSide front;
	private QbMatrixSide back;
	private QbMatrixSide top;
	private QbMatrixSide bottom;
	private QbMatrixSide left;
	private QbMatrixSide right;
	private int[] indexdata;
}
