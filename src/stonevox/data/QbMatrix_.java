package stonevox.data;

import java.util.HashMap;

import org.lwjgl.opengl.GL15;

import stonevox.util.GUI;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class QbMatrix_
{
	private float cubesize = 0.5f;

	public String name;
	public Vector3 position;
	public Vector3 size;

	public boolean visible;
	public Vector3 highlight = new Vector3(1, 1, 1);

	public HashMap<String, Voxel> voxels = new HashMap<String, Voxel>();

	private Matrix final_transform;
	private Matrix transform;
	private int indexbufferID;

	private QbMatrixSide_ front;
	private QbMatrixSide_ back;
	private QbMatrixSide_ top;
	private QbMatrixSide_ bottom;
	private QbMatrixSide_ left;
	private QbMatrixSide_ right;

	private QbModel_ parent;

	public QbMatrix_(QbModel_ parent)
	{
		visible = true;

		this.parent = parent;

		front = new QbMatrixSide_(Side.FRONT);
		back = new QbMatrixSide_(Side.BACK);
		top = new QbMatrixSide_(Side.TOP);
		bottom = new QbMatrixSide_(Side.BOTTOM);
		left = new QbMatrixSide_(Side.LEFT);
		right = new QbMatrixSide_(Side.RIGHT);
	}

	public void setSize(int x, int y, int z)
	{
		throw new NotImplementedException();
	}

	public void setPosition(int x, int y, int z)
	{
		throw new NotImplementedException();
	}

	public void generateVoxelData()
	{
		throw new NotImplementedException();
	}

	public void encodeVisibilityMask()
	{
		throw new NotImplementedException();
	}

	public void updateVisibilityMaskRelative(int x, int y, int z)
	{
		throw new NotImplementedException();
	}

	public void updateVoxelData(int mask, int x, int y, int z, Color color)
	{
		throw new NotImplementedException();
	}

	public RayHitPoint rayTest()
	{
		return null;
	}

	public void genLightingData()
	{

	}

	public void updateLightMapAroundIncluding(int x, int y, int z)
	{
	}

	public void updateLightMap(int mask, int x, int y, int z)
	{
	}

	float[][] ld = new float[3][3];
	int px;
	int py;
	int pz;

	public void updateLightMapRelative(Side side, int x, int y, int z)
	{
	}

	public void removeVoxel(Vector3 location)
	{
		removeVoxel((int) location.x, (int) location.y, (int) location.z);
	}

	public void removeVoxel(int x, int y, int z)
	{
	}

	public void addVoxel(Vector3 location)
	{
		// addVoxel((int) location.x, (int) location.y, (int) location.z,
		// this.cubecolor[(int) location.z][(int) location.y][(int) location.x]);
	}

	public void addVoxel(int x, int y, int z)
	{
		// addVoxel(x, y, z, this.cubecolor[z][y][x]);
	}

	public void addVoxel(Vector3 location, Color color)
	{
		addVoxel((int) location.x, (int) location.y, (int) location.z, color);
	}

	public void addVoxel(int x, int y, int z, Color color)
	{
	}

	public void setVoxelColor(Vector3 location, Color color)
	{
		setVoxelColor((int) location.x, (int) location.y, (int) location.z, color);
	}

	public void setVoxelColor(int x, int y, int z, Color color)
	{
	}

	public void render()
	{
	}

	public String getSizeString()
	{
		return (int) size.x + "_" + (int) size.y + "_" + (int) size.z;
	}

	public String getPositionString()
	{
		return (int) position.x + "_" + (int) position.y + "_" + (int) position.z;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
		GUI.Broadcast(GUI.MESSAGE_QB_MATRIX_RENAMED, name, 100000);
	}

	public void CREATEZEROEDCUBES()
	{
	}

	public void clean()
	{

	}

	public Color getColor(Vector3 location)
	{
		return getColor((int) location.x, (int) location.y, (int) location.z);
	}

	public Color getColor(int x, int y, int z)
	{
		return null; // cubecolor[z][y][x];
	}

	public boolean isDirty(Vector3 location)
	{
		return isDirty((int) location.x, (int) location.y, (int) location.z);
	}

	public boolean isDirty(int x, int y, int z)
	{
		return false;
	}

	public boolean withinRange(Vector3 location)
	{
		return withinRange((int) location.x, (int) location.y, (int) location.z);
	}

	public boolean withinRange(int x, int y, int z)
	{
		return false;
	}

	public boolean hasCube(Vector3 location)
	{
		return hasCube((int) location.x, (int) location.y, (int) location.z);
	}

	public boolean hasCube(int x, int y, int z)
	{
		return false;
	}

	public float getLightValue(int z, int y, int x)
	{
		return 1;
	}

	public void reSize(int x1, int y1, int z1)
	{

	}

	public void hack_shift_model_backwards()
	{

	}

	public void hack_shift_model_forwards()
	{

	}

	public void hack_shift_model_right()
	{

	}

	public void hack_shift_model_left()
	{

	}

	public void centerMatrixPosition()
	{
		this.position.x = -(int) (size.x / 2f);
		this.position.y = 0;
		this.position.z = -(int) (size.z / 2f);
	}

	public void dispose()
	{
		front.dispose();
		back.dispose();
		top.dispose();
		bottom.dispose();
		left.dispose();
		right.dispose();

		GL15.glDeleteBuffers(this.indexbufferID);
	}
}
