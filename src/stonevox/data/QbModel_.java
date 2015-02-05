package stonevox.data;

import java.util.ArrayList;

import stonevox.util.GUI;
import stonevox.util.RaycastingUtil;

public class QbModel_
{
	public int version;
	public int colorFormat;
	public int zAxisOrientation;
	public int compressed;
	public int visibilityMaskEncoded;
	public int numMatrices;
	public String filepath;

	public ArrayList<QbMatrix_> matrixList;
	public ArrayList<Color> colorindex = new ArrayList<Color>();

	private int activeMatrixIndex;

	public void setMatrixListLength(int length)
	{
		numMatrices = length;
		matrixList = new ArrayList<QbMatrix_>(length);
		for (int i = 0; i < numMatrices; i++)
		{
			matrixList.add(new QbMatrix_(this));
		}
	}

	public void generateMeshs()
	{
		for (int i = 0; i < numMatrices; i++)
		{
			QbMatrix_ def = matrixList.get(i);

			def.generateVoxelData();
			def.genLightingData();
		}
	}

	public void encodeVisibilityMask()
	{
		for (int i = 0; i < numMatrices; i++)
		{
			QbMatrix_ def = matrixList.get(i);
			def.encodeVisibilityMask();
		}
	}

	public RayHitPoint rayTest(Vector3 origin, Vector3 projection)
	{
		RaycastingUtil.rayOrigin = origin;
		RaycastingUtil.rayDirection = projection;
		return GetActiveMatrix().rayTest();
	}

	public void draw()
	{
		for (int i = 0; i < numMatrices; i++)
		{
			QbMatrix_ def = matrixList.get(i);

			def.render();
		}
	}

	public QbMatrix_ GetActiveMatrix()
	{
		return matrixList.get(activeMatrixIndex); // + ii);
	}

	public void dispose()
	{
		for (int i = 0; i < numMatrices; i++)
		{
			matrixList.get(i).dispose();
		}
	}

	public void centerMatrixPositions()
	{
		for (int i = 0; i < numMatrices; i++)
		{
			matrixList.get(i).centerMatrixPosition();
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
		QbMatrix_ def = new QbMatrix_(this);
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

	public QbMatrix_ getIndex(int index)
	{
		return matrixList.get(index);
	}
}
