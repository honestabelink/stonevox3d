package stonevox.util;

import java.util.ArrayList;
import java.util.HashMap;

import stonevox.Program;
import stonevox.data.Undo;
import stonevox.undo.UndoAdd;
import stonevox.undo.UndoPaint;
import stonevox.undo.UndoRemove;

public class UndoUtil
{
	// issue where matrix is removed, mid fails to index the correct matrix

	public static int MAXHISTORY = 100;

	public static float ADD = 0;
	public static float REMOVE = 1;
	public static float PAINT = 2;

	static HashMap<Float, Undo> undos = new HashMap<Float, Undo>();
	static ArrayList<ArrayList<Float>> undodata = new ArrayList<ArrayList<Float>>();
	static ArrayList<ArrayList<Float>> redodata = new ArrayList<ArrayList<Float>>();

	static
	{
		undos.put(ADD, new UndoAdd());
		undos.put(REMOVE, new UndoRemove());
		undos.put(PAINT, new UndoPaint());
	}

	public static void putData(float id, ArrayList<Float> data)
	{
		data.add(0, (float) Program.model.getActiveIndex());
		data.add(0, id);

		undodata.add(data);

		while (undodata.size() > MAXHISTORY)
		{
			undodata.remove(0);
		}

		redodata.clear();
	}

	public static void undo()
	{
		if (undodata.size() > 0)
		{
			int index = undodata.size() - 1;
			float toolid = undodata.get(index).get(0);
			float mID = undodata.get(index).get(1);
			if (Program.model.matrixList.size() - 1 <= mID)
			{
				redodata.add(undos.get(toolid).undo(undodata.get(index)));
				redodata.get(redodata.size() - 1).add(0, mID);
				redodata.get(redodata.size() - 1).add(0, toolid);
			}
			undodata.remove(index);
		}
	}

	public static void redo()
	{
		if (redodata.size() > 0)
		{
			int index = redodata.size() - 1;
			float toolid = redodata.get(index).get(0);
			float mID = redodata.get(index).get(1);
			if (Program.model.matrixList.size() - 1 <= mID)
			{
				undodata.add(undos.get(toolid).redo(redodata.get(index)));
				undodata.get(undodata.size() - 1).add(0, mID);
				undodata.get(undodata.size() - 1).add(0, toolid);
			}
			redodata.remove(index);
		}
	}
}
