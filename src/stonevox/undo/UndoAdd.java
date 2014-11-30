package stonevox.undo;

import java.util.ArrayList;

import stonevox.Program;
import stonevox.data.Undo;

public class UndoAdd implements Undo
{

	@Override
	public ArrayList<Float> undo(ArrayList<Float> data)
	{
		float x = 0;
		float y = 0;
		float z = 0;

		data.remove(0);

		float mID = data.get(0);

		data.remove(0);

		for (int i = 0; i < data.size() / 3; i++)
		{
			x = data.get(i * 3);
			y = data.get(i * 3 + 1);
			z = data.get(i * 3 + 2);
			Program.model.matrixList.get((int) mID).cubes[(int) z][(int) y][(int) x].setAlpha(0);
			Program.model.matrixList.get((int) mID).updateLightMap((int) x, (int) y, (int) z);
		}

		Program.model.matrixList.get((int) mID).updateMesh();
		resetTool();

		return data;
	}

	@Override
	public ArrayList<Float> redo(ArrayList<Float> data)
	{
		float x = 0;
		float y = 0;
		float z = 0;

		data.remove(0);

		float mID = data.get(0);

		data.remove(0);

		for (int i = 0; i < data.size() / 3; i++)
		{
			x = data.get(i * 3);
			y = data.get(i * 3 + 1);
			z = data.get(i * 3 + 2);
			Program.model.matrixList.get((int) mID).cubes[(int) z][(int) y][(int) x].setAlpha(255);
			Program.model.matrixList.get((int) mID).updateLightMap((int) x, (int) y, (int) z);
		}

		Program.model.matrixList.get((int) mID).updateMesh();
		resetTool();

		return data;
	}

	@Override
	public void resetTool()
	{
		Program.tooladd.resetUndoRedo();
	}

}
