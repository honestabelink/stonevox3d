using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public class ImporterQb : IImporter
{
    public string extension
    {
        get { return ".qb"; }
    }

    public QbModel read(string path)
    {
        StopwatchUtil.startclient("regularqbread", "Begin Qb read");

        QbModel model = _read(path);

        //var packet = PacketWriter.write<Packet_QbImported>(NetEndpoint.CLIENT);
        //packet.write(model);
        //packet.send();

        model.generatevertexbuffers();
        model.fillvertexbuffers();

        StopwatchUtil.stopclient("regularqbread", "End Qb read");

        return model;
    }

    public QbModel _read(string path)
    {
        QbModel model = new QbModel();

        using (FileStream f = new FileStream(path, FileMode.Open))
        using (BinaryReader reader = new BinaryReader(f))
        {
            model.version = reader.ReadUInt32();
            model.colorFormat = reader.ReadUInt32();
            model.zAxisOrientation = reader.ReadUInt32();
            model.compressed = reader.ReadUInt32();
            model.visibilityMaskEncoded = reader.ReadUInt32();
            model.setmatrixcount(reader.ReadUInt32());

            for (int i = 0; i < model.numMatrices; i++)
            {
                QbMatrix m = model.matrices[i];

                byte l = reader.ReadByte();
                m.name = System.Text.Encoding.Default.GetString(reader.ReadBytes(l));
                m.setsize((int)reader.ReadUInt32(), (int)reader.ReadUInt32(), (int)reader.ReadUInt32());
                m.position = new OpenTK.Vector3(reader.ReadUInt32(), reader.ReadUInt32(), reader.ReadUInt32());

                byte r;
                byte g;
                byte b;
                byte a;
                int zz;

                if (model.compressed == 0)
                {

                    for (int z = 0; z < m.size.Z; z++)
                        for (int y = 0; y < m.size.Y; y++)
                            for (int x = 0; x < m.size.X; x++)
                            {
                                r = reader.ReadByte();
                                g = reader.ReadByte();
                                b = reader.ReadByte();
                                a = reader.ReadByte();
                                zz = model.zAxisOrientation == 0 ? z : (int)m.size.Z - z - 1;

                                if (a != 0)
                                {
                                    m.voxels.GetOrAdd(m.gethash(x, y, zz), new Voxel(x, y, zz, a, m.getcolorindex(r, g, b)));
                                }
                            }
                }
                else
                {
                    throw new Exception("qb compression not implemented");
                }
            }
        }


        //Console.WriteLine(string.Format("matrix count : {0}", model.matrices.Count));

        //foreach (var c in model.matrices)
        //{
        //    Console.WriteLine(c.size.ToString());
        //}

        return model;
    }
}
