package stonevox.data;

import java.util.ArrayList;

import stonevox.util.GUI;

public class QbModel
{
	public int version;
	public int colorFormat;
	public int zAxisOrientation;
	public int compressed;
	public int visibilityMaskEncoded;
	public int numMatrices;
	public boolean hasPAL;
	public String filepath;

	public ArrayList<QbMatrix> matrixList;

	private int activeMatrixIndex;

	public void setMatrixListLength(int length)
	{
		numMatrices = length;
		matrixList = new ArrayList<QbMatrix>(length);
		for (int i = 0; i < numMatrices; i++)
		{
			matrixList.add(new QbMatrix());
		}
	}

	public void generateMeshs()
	{
		int activeMatrixIndex = hasPAL ? 1 : 0;

		int ii = hasPAL ? 1 : 0;
		for (int i = ii; i < numMatrices; i++)
		{
			QbMatrix def = matrixList.get(i);

			def.generateVoxelData();
			// def.generateMesh();
		}
	}

	public void encodeVisibilityMask()
	{
		int ii = hasPAL ? 1 : 0;
		for (int i = ii; i < numMatrices; i++)
		{
			QbMatrix def = matrixList.get(i);
			def.encodeVisibilityMask();
		}
	}

	public RayHitPoint rayTest(Vector3 origin, Vector3 projection)
	{
		return GetActiveMatrix().rayTest(origin, projection);
	}

	public void draw()
	{
		int ii = hasPAL ? 1 : 0;
		for (int i = ii; i < numMatrices; i++)
		{
			QbMatrix def = matrixList.get(i);

			def.render();
		}
	}

	public QbMatrix GetActiveMatrix()
	{
		int ii = hasPAL ? 1 : 0;
		return matrixList.get(activeMatrixIndex + ii);
	}

	public void dispose()
	{
		for (int i = 0; i < numMatrices; i++)
		{
			matrixList.get(i).dispose();
		}
	}

	public void setActiveMatrix(int index)
	{
		this.activeMatrixIndex = index;
		GetActiveMatrix().clean();
	}

	public void addMatrix()
	{
		addMatrix(GetActiveMatrix().size.x, GetActiveMatrix().size.y, GetActiveMatrix().size.z);
	}

	public void addMatrix(float sizex, float sizey, float sizez)
	{
		this.numMatrices++;
		QbMatrix def = new QbMatrix();
		def.setName("default");
		def.setSize((int) sizex, (int) sizey, (int) sizez);
		def.setPosition(0, 0, 0);

		def.CREATEZEROEDCUBES();
		def.generateVoxelData();
		def.clean();

		this.matrixList.add(def);
		GUI.Broadcast(GUI.MESSAGE_QB_MATRIX_ADDED, "", 10000);
	}

	public void removeActiveMatrix()
	{
		if (activeMatrixIndex == 0 && matrixList.size() == 1)
			return;
		else
		{
			numMatrices--;
			GetActiveMatrix().dispose();
			matrixList.remove(activeMatrixIndex);

			if (activeMatrixIndex - 1 < 0)
			{
				activeMatrixIndex = 0;
			}
			else if (activeMatrixIndex > matrixList.size() - 1)
			{
				activeMatrixIndex = matrixList.size() - 1;
			}
			else if (activeMatrixIndex == 0)
			{

			}

		}

		GUI.Broadcast(GUI.MESSAGE_QB_MATRIX_REMOVED, "", 10000);
	}

	public int getActiveIndex()
	{
		return activeMatrixIndex;
	}

	public QbMatrix getIndex(int index)
	{
		return matrixList.get(index);
	}
}
