using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public class ExporterQb : IExporter
    {
        public string extension
        {
            get { return ".qb"; }
        }

        public void write(string path, string name, QbModel model)
        {
            string fullpath = Path.Combine(path, name + extension);
            using (FileStream f = new FileStream(fullpath, FileMode.OpenOrCreate))
            {
                using (BinaryWriter w = new BinaryWriter(f))
                {
                    w.Write(model.version);
                    w.Write(model.colorFormat);
                    w.Write(model.zAxisOrientation);
                    w.Write(model.compressed);
                    w.Write(model.visibilityMaskEncoded);

                    w.Write((uint)model.matrices.Count);

                    for (int i = 0; i < model.numMatrices; i++)
                    {
                        QbMatrix m = model.matrices[i];
                        if (!m.Visible) continue;

                        w.Write(m.name);
                        w.Write((uint)m.size.X);
                        w.Write((uint)m.size.Y);
                        w.Write((uint)m.size.Z);

                        w.Write((uint)m.position.X);
                        w.Write((uint)m.position.Y);
                        w.Write((uint)m.position.Z);

                        if (model.compressed == 0)
                        {

                            Voxel voxel;
                            for (int z = 0; z < m.size.Z; z++)
                                for (int y = 0; y < m.size.Y; y++)
                                    for (int x = 0; x < m.size.X; x++)
                                    {
                                        int zz = model.zAxisOrientation == (int)0 ? z : (int)(m.size.Z - z - 1);

                                        if (m.voxels.TryGetValue(m.GetHash(x, y, zz), out voxel))
                                        {
                                            Colort c = m.colors[voxel.colorindex];

                                            int r = (int)(c.R * 255f);
                                            int g = (int)(c.G * 255f);
                                            int b = (int)(c.B * 255f);

                                            w.Write((byte)r);
                                            w.Write((byte)g);
                                            w.Write((byte)b);
                                            w.Write(voxel.alphamask);
                                        }
                                        else
                                        {
                                            w.Write((byte)0);
                                            w.Write((byte)0);
                                            w.Write((byte)0);
                                            w.Write((byte)0);
                                        }
                                    }
                        }
                    }
                }
            }
        }
    }
}