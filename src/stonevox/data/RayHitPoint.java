package stonevox.data;

public class RayHitPoint
{
	public Vector3	cubelocation;
	public Vector3	cubenormal;
	public float	distance;

	public RayHitPoint()
	{
		cubelocation = new Vector3();
		cubenormal = new Vector3();
	}

	public RayHitPoint clone()
	{
		RayHitPoint hit = new RayHitPoint();
		hit.cubelocation.x = this.cubelocation.x;
		hit.cubelocation.y = this.cubelocation.y;
		hit.cubelocation.z = this.cubelocation.z;
		hit.cubenormal.x = this.cubenormal.x;
		hit.cubenormal.y = this.cubenormal.y;
		hit.cubenormal.z = this.cubenormal.z;
		hit.distance = this.distance;
		return hit;
	}

}
