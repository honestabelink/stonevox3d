package stonevox.data;

import java.util.ArrayList;

public class QbModel
{
	public int								version;
	public int								colorFormat;
	public int								zAxisOrientation;
	public int								compressed;
	public int								visibilityMaskEncoded;
	public int								numMatrices;
	public boolean							hasPAL;
	public String							filepath;

	public ArrayList<QbMatrixDefination>	matrixList;

	public void setMatrixListLength(int length)
	{
		numMatrices = length;
		matrixList = new ArrayList<QbMatrixDefination>(length);
		for (int i = 0; i < numMatrices; i++)
		{
			matrixList.add(new QbMatrixDefination());
		}
	}

	public void generateMeshs()
	{
		int ii = hasPAL ? 1 : 0;
		for (int i = ii; i < numMatrices; i++)
		{
			QbMatrixDefination def = matrixList.get(i);

			def.generateMesh();
		}
	}

	public void encodeVisibilityMask()
	{
		int ii = hasPAL ? 1 : 0;
		for (int i = ii; i < numMatrices; i++)
		{
			QbMatrixDefination def = matrixList.get(i);
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
			QbMatrixDefination def = matrixList.get(i);

			def.render();
		}
	}

	public QbMatrixDefination GetActiveMatrix()
	{
		int ii = hasPAL ? 1 : 0;
		return matrixList.get(ii);
	}

	public void dispose()
	{
		for (int i = 0; i < numMatrices; i++)
		{
			matrixList.get(i).dispose();
		}
	}
}
