package stonevox;

import stonevox.data.Matrix;
import stonevox.data.Vector3;

public class Camera
{
	public Vector3	position	= new Vector3(0f, 1f, -1f);
	public Vector3	direction	= new Vector3(0f, 0f, 1f);
	public Vector3	up			= Vector3.up;

	public Matrix	projection;
	public Matrix	lookat;
	public Matrix	modelview;
	public Matrix	ortho2d;

	public Matrix	raycast_lookat;

	public Camera()
	{

	}

	public void Update()
	{
		lookat = Matrix.CreateLookAt(position, Vector3.add(position, direction), up);
		modelview = Matrix.Multiply(lookat, projection);
	}

	public Matrix RayCastLookAtMatrix()
	{
		Vector3 ndirection = new Vector3();
		ndirection.x = position.x + direction.x;
		ndirection.y = position.y - direction.y; // have to invert y here, window origin???
		ndirection.z = position.z + direction.z;

		raycast_lookat = Matrix.CreateLookAt(position, ndirection, up);
		return raycast_lookat;
	}

	public void LookAtModel()
	{
		position =
				new Vector3(Program.model.GetActiveMatrix().sizeX * .5f - .5f,
						Program.model.GetActiveMatrix().sizeY * .5f * 3.0f,
						-Program.model.GetActiveMatrix().sizeZ * .5f * 3.5f);

		direction = Vector3.sub(Program.model.GetActiveMatrix().pos_size, position);
		direction.noramlize();

		lookat = Matrix.CreateLookAt(position, Vector3.add(position, direction), up);
		modelview = Matrix.Multiply(lookat, projection);
	}
}
