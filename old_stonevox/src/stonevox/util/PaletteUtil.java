package stonevox.util;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import stonevox.gui.ColorOption;

public class PaletteUtil
{
	public static String extension = ".scp";

	public static void WritePalette()
	{
		String path = GetPath.getPath("");

		File folder = new File(path + "\\palette");
		folder.mkdirs();

		int count = new File(folder.getAbsolutePath()).listFiles().length;

		File file = new File(path + "\\palette\\" + "untitled_" + count + extension);
		try
		{
			file.createNewFile();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}

		try
		{
			DataOutputStream out =
					new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path + "\\palette\\"
							+ "untitled_" + count + extension)));

			System.out.print("Beginning write .scp \n");

			for (int i = 0; i < 10; i++)
			{
				ColorOption co = (ColorOption) GUI.get(GUI.coloroptionStartID + i);
				out.writeFloat(co.color.r);
				out.writeFloat(co.color.g);
				out.writeFloat(co.color.b);
				out.writeFloat(co.color.a);
				out.writeFloat(co.huecolor.r);
				out.writeFloat(co.huecolor.g);
				out.writeFloat(co.huecolor.b);
				out.writeFloat(co.huecolor.a);
				out.writeFloat(co.colorsquarelocation.x);
				out.writeFloat(co.colorsquarelocation.y);
				out.writeFloat(co.colorsquarelocation.z);
			}

			System.out.print(String.format("writting : %s \n", "success"));
			out.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
