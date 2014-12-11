package stonevox.undo;

import java.util.ArrayList;

import org.newdawn.slick.Color;

import stonevox.Program;
import stonevox.data.Undo;

public class UndoPaint implements Undo
{

	@Override
	public ArrayList<Float> undo(ArrayList<Float> data)
	{
		Color c = null;
		float x = 0;
		float y = 0;
		float z = 0;
		float r = 0;
		float g = 0;
		float b = 0;

		data.remove(0);

		float mID = data.get(0);

		data.remove(0);

		for (int i = 0; i < data.size() / 6; i++)
		{
			x = data.get(i * 6);
			y = data.get(i * 6 + 1);
			z = data.get(i * 6 + 2);
			r = data.get(i * 6 + 3);
			g = data.get(i * 6 + 4);
			b = data.get(i * 6 + 5);

			c = Program.model.matrixList.get((int) mID).getColor((int) x, (int) y, (int) z).toNEWDAWN();

			data.set(i * 6 + 3, c.r);
			data.set(i * 6 + 4, c.g);
			data.set(i * 6 + 5, c.b);

			Program.model.matrixList.get((int) mID).setVoxelColor((int) x, (int) y, (int) z,
					new stonevox.data.Color(r, g, b, 0));
		}

		Program.model.matrixList.get((int) mID).clean();
		resetTool();

		return data;
	}

	@Override
	public ArrayList<Float> redo(ArrayList<Float> data)
	{
		return undo(data);
	}

	@Override
	public void resetTool()
	{
		Program.toolpainter.resetUndoRedo();
	}

}
