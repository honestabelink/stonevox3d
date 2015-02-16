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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.Sys;

import stonevox.Program;
import stonevox.data.Color;
import stonevox.data.QbMatrix;
import stonevox.data.QbMatrixSide;
import stonevox.data.QbModel;
import stonevox.gui.Textbox;

public class QbUtil
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

	public static Color getColor(int r, int g, int b, int a, int colorFormat)
	{
		if (colorFormat == 0)
		{
			return new Color(r / 256f, g / 256f, b / 256f, a);
		}
		return new Color(b / 256f, g / 256f, r / 256f, a);
	}

	public static QbModel readQB(String path) throws Exception
	{
		System.out.print(String.format("Begining QB read : %s\n", path));
		long time = Sys.getTime();
		QbModel model = new QbModel();
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
				QbMatrix def = model.matrixList.get(i);

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
				int tz; // normalized z coordinate (z if zAxisOrientation == 0 and width - z - 1 if zAxisOrientation == 1)

				if (model.compressed == 0)
				{

					for (int z = 0; z < def.size.z; z++)
						for (int y = 0; y < def.size.y; y++)
							for (int x = (int) def.size.x - 1; x > -1; x--)
							{
								r = in.readUnsignedByte();
								g = in.readUnsignedByte();
								b = in.readUnsignedByte();
								a = in.readUnsignedByte();
								tz = model.zAxisOrientation == 0 ? z : (int) def.size.z - z - 1;

								def.cubecolor[tz][y][x] = getColor(r, g, b, a, model.colorFormat);
							}
				}
				else
				{
					for (int z=0; z < def.size.z; z++)
					{
						tz = model.zAxisOrientation == 0 ? z : (int) def.size.z - z - 1;
						int index = 0;
						while (true)
						{
							r = in.readUnsignedByte();
							g = in.readUnsignedByte();
							b = in.readUnsignedByte();
							a = in.readUnsignedByte();
							if (r == 6 && g == 0 && b == 0 && a == 0) // NEXTSLICEFLAG
							{
								for (; index < def.size.y * def.size.x; index++) // fill with empty voxels
								{
									int x = index % (int) def.size.x;
									int y = index / (int) def.size.x;
									def.cubecolor[tz][y][(int) def.size.x - x - 1] = new Color(0, 0, 0, 0);
								}
								break;
							}
							else
							{
								if (r == 2 && g == 0 && b == 0 && a == 0) //CODEFLAG
								{
									int count = (int) (getUnsignedInt(in));
									r = in.readUnsignedByte();
									g = in.readUnsignedByte();
									b = in.readUnsignedByte();
									a = in.readUnsignedByte();
									for (int j=0; j < count; j++)
									{
										int x = index % (int) def.size.x;
										int y = index / (int) def.size.x;
										index++;
										def.cubecolor[tz][y][(int) def.size.x - x - 1] = getColor(r, g, b, a, model.colorFormat);
									}
								}
								else 
								{
									int x = index % (int) def.size.x;
									int y = index / (int) def.size.x;
									index++;
									def.cubecolor[tz][y][(int) def.size.x - x - 1] = getColor(r, g, b, a, model.colorFormat);
								}
							}
						}
					}
				}

				if (def.getName().equals("PAL"))
				{
					// model.hasPAL = true;
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
				// use instanceof to verify the references are indeed of the type in question
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

				// String m_name = m.getName();
				//
				// if (m_name.contains("right"))
				// m_name = m_name.replace("right", "left");
				// else if (m_name.contains("left"))
				// m_name = m_name.replace("left", "right");
				//
				// m.setName(m_name);

				out.writeByte((byte) m.getName().length());
				out.writeBytes(m.getName());
				out.write(intToLEndian((int) m.size.x));
				out.write(intToLEndian((int) m.size.y));
				out.write(intToLEndian((int) m.size.z));
				out.write(intToLEndian((int) m.pos.x));
				out.write(intToLEndian((int) m.pos.y));
				out.write(intToLEndian((int) m.pos.z));

				// this is how you export
				// z -axis
				for (int z = 0; z < m.size.z; z++)
					for (int y = 0; y < m.size.y; y++)
						for (int x = (int) m.size.x - 1; x > -1; x--)
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

	public static QbModel GetDefault()
	{
		QbModel model = new QbModel();
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
					model.GetActiveMatrix().cubecolor[z][y][x] = new Color(1, 1, 1, 0);
				}

		model.generateMeshs();
		return model;
	}

	public static void writeOBJ()
	{
		long time = Sys.getTime();
		QbModel model = Program.model;

		Textbox textbox = (Textbox) GUI.get(GUI.PROJECTSETTINGS_NAME);
		String name = textbox.text;

		String path = GetPath.getPath("");

		System.out.print("Begining QBJ write\n");

		File folder = new File(path + "\\export");
		folder.mkdirs();

		File objfile = new File(path + "\\export\\" + name + ".obj");
		try
		{
			objfile.createNewFile();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		File materialFile = new File(path + "\\export\\" + name + ".mtl");
		try
		{
			materialFile.createNewFile();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}

		HashMap<String, Color> colors = new HashMap<String, Color>();

		try
		{
			DataOutputStream out =
					new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path + "\\export\\" + name
							+ ".mtl")));

			System.out.print("	Beginning write mtl \n");

			for (QbMatrix m : model.matrixList)
			{
				for (int z = 0; z < m.size.z; z++)
				{
					for (int y = 0; y < m.size.y; y++)
					{
						for (int x = 0; x < m.size.x; x++)
						{
							Color color = m.cubecolor[z][y][x];

							if (!colors.containsKey(color.r + "_" + color.g + "_" + color.b))
							{
								colors.put(color.r + "_" + color.g + "_" + color.b, color);
							}
						}
					}
				}

				for (Color c : colors.values())
				{
					float r = c.r;
					float g = c.g;
					float b = c.b;

					if (r < 0)
						r = 0f;
					if (r > 1)
						r = 1f;
					if (r <= 0.03928f)
						r = (float) (r / 12.92f);
					else
						r = (float) (Math.exp(2.4 * Math.log((r + 0.055) / 1.055)));

					if (g < 0)
						g = 0f;
					if (g > 1)
						g = 1f;
					if (g <= 0.03928f)
						g = (float) (g / 12.92f);
					else
						g = (float) (Math.exp(2.4 * Math.log((g + 0.055) / 1.055)));

					if (b < 0)
						b = 0f;
					if (b > 1)
						b = 1f;
					if (b <= 0.03928f)
						b = (float) (b / 12.92f);
					else
						b = (float) (Math.exp(2.4 * Math.log((b + 0.055) / 1.055)));

					out.writeBytes("newmtl " + c.r + "_" + c.g + "_" + c.b + '\r' + '\n');
					out.writeBytes("\t Ns 32" + '\r' + '\n');
					out.writeBytes("\t d 1" + '\r' + '\n');
					out.writeBytes("\t Tr 0" + '\r' + '\n');
					out.writeBytes("\t Tf 1 1 1" + '\r' + '\n');
					out.writeBytes("\t illum 2" + '\r' + '\n');
					out.writeBytes("\t Ka " + r + " " + g + " " + b + '\r' + '\n');
					out.writeBytes("\t Kd " + r + " " + g + " " + b + '\r' + '\n');
					out.writeBytes("\t Ks 0.3500 0.3500 0.3500" + '\r' + '\n');
					out.writeBytes("" + '\r' + '\n');
				}
			}

			System.out.print(String.format("	writting : %s \n", "success"));

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

		try
		{
			DataOutputStream out =
					new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path + "\\export\\" + name
							+ ".obj")));

			System.out.print("	Beginning write obj \n");

			out.writeBytes("mtllib " + name + ".mtl" + '\r' + '\n');
			out.writeBytes("" + '\r' + '\n');
			int index = 0;

			for (QbMatrix m : model.matrixList)
			{

				out.writeBytes("#" + '\r' + '\n');
				out.writeBytes("#" + "object " + m.getName() + '\r' + '\n');
				out.writeBytes("#" + '\r' + '\n');

				Map<String, ArrayList<String>> d = new HashMap<String, ArrayList<String>>();

				for (Color color : colors.values())
					d.put(color.r + "_" + color.g + "_" + color.b, new ArrayList<String>());

				writeQbMatrixSideOBJ(d, out, m, m.front, m.getName(), 32);
				writeQbMatrixSideOBJ(d, out, m, m.back, m.getName(), 64);
				writeQbMatrixSideOBJ(d, out, m, m.left, m.getName(), 2);
				writeQbMatrixSideOBJ(d, out, m, m.right, m.getName(), 4);
				writeQbMatrixSideOBJ(d, out, m, m.top, m.getName(), 8);
				writeQbMatrixSideOBJ(d, out, m, m.bottom, m.getName(), 16);

				for (Entry<String, ArrayList<String>> entry : d.entrySet())
				{
					String key = entry.getKey();
					ArrayList<String> value = entry.getValue();

					for (String t : value)
					{
						out.writeBytes(t);
					}
				}

				out.writeBytes("" + '\r' + '\n');

				try
				{
					out.writeBytes("g " + m.getName() + '\r' + '\n');
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				for (Entry<String, ArrayList<String>> entry : d.entrySet())
				{
					String key = entry.getKey();
					ArrayList<String> value = entry.getValue();

					if (value.size() > 0)
					{
						out.writeBytes("usemtl " + key + '\r' + '\n');

						for (int i = 1; i < value.size(); i += 4)
						{
							out.writeBytes("f " + (i + index) + " " + (i + index + 1) + " " + (i + index + 2) + '\r'
									+ '\n');
							out.writeBytes("f " + (i + index) + " " + (i + index + 2) + " " + (i + index + 3) + '\r'
									+ '\n');
						}

						index += value.size();
					}
				}
			}

			System.out.print(String.format("	writting : %s \n", "success"));

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
		System.out.print("Save : obj model encoded and saved - " + (netime - time + " mil" + System.lineSeparator()));
	}

	public static void writeQbMatrixSideOBJ(Map<String, ArrayList<String>> d, DataOutputStream out, QbMatrix m,
			QbMatrixSide side, String name, int mask)
	{
		for (int z = 0; z < m.size.z; z++)
		{
			for (int y = 0; y < m.size.y; y++)
			{
				for (int x = 0; x < m.size.x; x++)
				{
					Color color = m.cubecolor[z][y][x];

					if (color.a > 1)
					{
						if ((color.a & mask) == mask)
						{
							int index = side.cubeindexs[z][y][x];

							if (index > -1)
							{
								if (side.vertexdata[index] <= -1000)
									continue;

								String colorindex = color.r + "_" + color.g + "_" + color.b;
								d.get(colorindex).add(
										"v " + (side.vertexdata[index] + m.pos.x) + " "
												+ (side.vertexdata[index + 1] + m.pos.y) + " "
												+ (side.vertexdata[index + 2] + m.pos.z) + '\r' + '\n');
								d.get(colorindex).add(
										"v " + (side.vertexdata[index + 7] + m.pos.x) + " "
												+ (side.vertexdata[index + 8] + m.pos.y) + " "
												+ (side.vertexdata[index + 9] + m.pos.z) + '\r' + '\n');
								d.get(colorindex).add(
										"v " + (side.vertexdata[index + 14] + m.pos.x) + " "
												+ (side.vertexdata[index + 15] + m.pos.y) + " "
												+ (side.vertexdata[index + 16] + m.pos.z) + '\r' + '\n');
								d.get(colorindex).add(
										"v " + (side.vertexdata[index + 21] + m.pos.x) + " "
												+ (side.vertexdata[index + 22] + m.pos.y) + " "
												+ (side.vertexdata[index + 23] + m.pos.z) + '\r' + '\n');
							}
						}
					}
				}
			}
		}
	}
}
