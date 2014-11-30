package stonevox.data;

import java.util.ArrayList;

public interface Undo
{
	public void resetTool();

	public ArrayList<Float> undo(ArrayList<Float> data);

	public ArrayList<Float> redo(ArrayList<Float> data);
}
