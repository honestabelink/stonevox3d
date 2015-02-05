package stonevox.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Comparator;

import org.lwjgl.Sys;

import stonevox.Program;
import stonevox.data.Color;
import stonevox.data.QbMatrix;
import stonevox.data.QbMatrix_;
import stonevox.data.QbModel;
import stonevox.data.QbModel_;
import stonevox.gui.Textbox;

public class QbUtil_
{
	public static long getUnsignedInt(DataInputStream in) throws IOException
	{
		byte[] b = new byte[4];
		in.read(b, 0, 4);

		ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);

		return (long) (bb.getInt() & 0xFFFFFFFFL);
	}

	public static byte[] longToBytes(long x)
	{
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(x);
		return buffer.array();
	}

	public static byte[] intToBytes(int x)
	{
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		buffer.putInt(x);
		return buffer.array();
	}

	public static byte[] intToLEndian(int x)
	{
		ByteBuffer buffer3 = ByteBuffer.allocate(Integer.BYTES);
		buffer3.order(ByteOrder.LITTLE_ENDIAN);
		buffer3.putInt(x);
		buffer3 = (ByteBuffer) buffer3.flip();

		return buffer3.array();
	}

	public static QbModel_ readQB(String path) throws Exception
	{
		System.out.print(String.format("Begining QB read : %s\n", path));
		long time = Sys.getTime();
		QbModel_ model = new QbModel_();
		model.filepath = path;

		try
		{
			DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(path)));

			model.version = (int) (getUnsignedInt(in));
			model.colorFormat = (int) getUnsignedInt(in);
			model.zAxisOrientation = (int) getUnsignedInt(in);
			model.compressed = (int) getUnsignedInt(in);
			model.visibilityMaskEncoded = (int) getUnsignedInt(in);
			model.setMatrixListLength((int) getUnsignedInt(in));

			for (int i = 0; i < model.numMatrices; i++)
			{
				QbMatrix_ def = model.matrixList.get(i);

				byte namelength = in.readByte();
				byte[] namebytes = new byte[namelength];
				in.read(namebytes, 0, namelength);
				def.setName(new String(namebytes));

				def.setSize((int) (getUnsignedInt(in)), (int) (getUnsignedInt(in)), (int) (getUnsignedInt(in)));

				def.setPosition((int) (getUnsignedInt(in)), (int) (getUnsignedInt(in)), (int) (getUnsignedInt(in)));

				int r;
				int g;
				int b;
				int a;

				if (model.compressed == 0)
				{

					for (int z = 0; z < def.size.z; z++)
						for (int y = 0; y < def.size.y; y++)
							for (int x = 0; x < def.size.x; x++)
							{
								r = in.readUnsignedByte();
								g = in.readUnsignedByte();
								b = in.readUnsignedByte();
								a = in.readUnsignedByte();

								// def.cubecolor[z][y][x] = new Color(r / 256f, g / 256f, b / 256f, a);
							}
				}
				else
				{
					throw new Exception("qb compression not implemented");
				}

				if (def.getName().equals("PAL"))
				{
					model.matrixList.remove(i);
					i--;
					model.numMatrices--;
					continue;
				}

			}
			in.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		Collections.sort(model.matrixList, new Comparator()
		{
			@Override
			public int compare(Object softDrinkOne, Object softDrinkTwo)
			{
				return ((QbMatrix) softDrinkOne).getName().compareTo(((QbMatrix) softDrinkTwo).getName());
			}
		});

		long ntime = Sys.getTime();
		ntime = Math.abs(ntime - time);

		System.out.print(String.format("End Qb read : %s mil \n", ntime));

		return model;
	}

	public static void writeQB()
	{
		long time = Sys.getTime();
		QbModel model = Program.model;
		System.out.print("Begining QB write\n");

		Textbox textbox = (Textbox) GUI.get(GUI.PROJECTSETTINGS_NAME);
		String name = textbox.text;

		model.encodeVisibilityMask();
		System.out.print("	Encoding Visiblity Mask \n");

		String path = GetPath.getPath("");

		File folder = new File(path + "\\export");
		folder.mkdirs();

		File file = new File(path + "\\export\\" + name + ".qb");
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
					new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path + "\\export\\" + name
							+ ".qb")));

			System.out.print("	Beginning write \n");

			out.write(intToLEndian(model.version));
			out.write(intToLEndian(model.colorFormat));
			out.write(intToLEndian(model.zAxisOrientation));
			out.write(intToLEndian(model.compressed));
			out.write(intToLEndian(1));
			out.write(intToLEndian(model.matrixList.size()));

			for (int i = 0; i < model.matrixList.size(); i++)
			{
				QbMatrix m = model.matrixList.get(i);
				System.out.print(String.format("	writting : %s \n", m.getName()));
				out.writeByte((byte) m.getName().length());
				out.writeBytes(m.getName());
				out.write(intToLEndian((int) m.size.x));
				out.write(intToLEndian((int) m.size.y));
				out.write(intToLEndian((int) m.size.z));
				out.write(intToLEndian((int) m.pos.x));
				out.write(intToLEndian((int) m.pos.y));
				out.write(intToLEndian((int) m.pos.z));

				for (int z = 0; z < m.size.z; z++)
					for (int y = 0; y < m.size.y; y++)
						for (int x = 0; x < m.size.x; x++)
						{
							Color c = m.cubecolor[z][y][x];

							int r = (int) (c.r * 255f);
							int g = (int) (c.g * 255f);
							int b = (int) (c.b * 255f);
							int a = c.a;

							out.writeByte((byte) r);
							out.writeByte((byte) g);
							out.writeByte((byte) b);
							out.writeByte((byte) a);
						}

				System.out.print(String.format("	writting : %s \n", "success"));
			}

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

		long netime = Sys.getTime();

		System.out.print("Save : model encoded and saved - " + (netime - time + " mil" + System.lineSeparator()));
	}

	public static QbModel_ GetDefault()
	{
		QbModel_ model = new QbModel_();
		model.filepath = "\\untitled.qb";

		model.zAxisOrientation = 1;
		model.visibilityMaskEncoded = 1;
		model.version = 257;

		model.setMatrixListLength(1);
		model.GetActiveMatrix().setSize(10, 10, 10);
		model.GetActiveMatrix().setPosition(0, 0, 0);

		model.GetActiveMatrix().setName("default");

		for (int z = 0; z < 10; z++)
			for (int y = 0; y < 10; y++)
				for (int x = 0; x < 10; x++)
				{
					// model.GetActiveMatrix().cubecolor[z][y][x] = new Color(1, 1, 1, 0);
				}

		model.generateMeshs();
		return model;
	}

}