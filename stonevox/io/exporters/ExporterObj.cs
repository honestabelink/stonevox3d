using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    // obj export will be removed...

    // someone just need this for something thez were working on
    public class ExporterObj : IExporter
    {
        public string extension
        {
            get { return ".obj"; }
        }

        public void write(string path, string name, QbModel dataType)
        {
            for (int t = 0; t < dataType.matrices.Count; t++)
            {
                QbMatrix m = dataType.matrices[t];
                using (FileStream material = new FileStream(path + "\\" + name + "_" + m.name + ".mtl", FileMode.OpenOrCreate))
                {
                    using (StreamWriter _out = new StreamWriter(material))
                        foreach (var color in m.colors)
                        {
                            float r = color.R;
                            float g = color.G;
                            float b = color.B;
                            if (r < 0.0F)
                            {
                                r = 0.0F;
                            }
                            if (r > 1.0F)
                            {
                                r = 1.0F;
                            }
                            if (r <= 0.03928F)
                            {
                                r /= 12.92F;
                            }
                            else
                            {
                                r = (float)Math.Exp(2.4D * Math.Log((r + 0.055D) / 1.055D));
                            }
                            if (g < 0.0F)
                            {
                                g = 0.0F;
                            }
                            if (g > 1.0F)
                            {
                                g = 1.0F;
                            }
                            if (g <= 0.03928F)
                            {
                                g /= 12.92F;
                            }
                            else
                            {
                                g = (float)Math.Exp(2.4D * Math.Log((g + 0.055D) / 1.055D));
                            }
                            if (b < 0.0F)
                            {
                                b = 0.0F;
                            }
                            if (b > 1.0F)
                            {
                                b = 1.0F;
                            }
                            if (b <= 0.03928F)
                            {
                                b /= 12.92F;
                            }
                            else
                            {
                                b = (float)Math.Exp(2.4D * Math.Log((b + 0.055D) / 1.055D));
                            }
                            _out.WriteLine("newmtl " + color.R + "_" + color.G + "_" + color.B);
                            _out.WriteLine("Ns 32");
                            _out.WriteLine("d 1");
                            _out.WriteLine("Tr 0");
                            _out.WriteLine("Tf 1 1 1");
                            _out.WriteLine("illum 2");
                            _out.WriteLine("Ka " + r + " " + g + " " + b);
                            _out.WriteLine("Kd " + r + " " + g + " " + b);
                            _out.WriteLine("Ks 0.3500 0.3500 0.3500");
                            _out.WriteLine("");
                        }
                }

                using (FileStream obj = new FileStream(path + "\\" + name + "_" + m.name + ".obj", FileMode.OpenOrCreate))
                {
                    using (StreamWriter _out = new StreamWriter(obj))
                    {
                        int indexcount = 0;
                        foreach (var c in m.voxels.Values)
                        {
                            float cubesize = .5f;
                            float x = c.x;
                            float z = c.z;
                            float y = c.z;
                            //front
                            if ((c.alphamask & 32) == 32)
                            {
                                indexcount++;

                                _out.WriteLine(string.Format("v {0} {1} {2}", -cubesize + x,
                                -cubesize + y,
                                cubesize + z));

                                _out.WriteLine(string.Format("v {0} {1} {2}", cubesize + x,
                                -cubesize + y,
                                cubesize + z));

                                _out.WriteLine(string.Format("v {0} {1} {2}", cubesize + x,
                                cubesize + y,
                                 cubesize + z));

                                _out.WriteLine(string.Format("v {0} {1} {2}", -cubesize + x,
                                 cubesize + y,
                                 cubesize + z));
                            }

                            //back
                            if ((c.alphamask & 64) == 64)
                            {
                                indexcount++;
                                _out.WriteLine(string.Format("v {0} {1} {2}", -cubesize + x,
                                -cubesize + y,
                                -cubesize + z));

                                _out.WriteLine(string.Format("v {0} {1} {2}", cubesize + x,
                                -cubesize + y,
                                -cubesize + z)) ;

                                _out.WriteLine(string.Format("v {0} {1} {2}", cubesize + x,
                                cubesize + y,
                                 -cubesize + z));

                                _out.WriteLine(string.Format("v {0} {1} {2}", -cubesize + x,
                                 cubesize + y,
                                 -cubesize + z));
                            }

                            //top
                            if ((c.alphamask & 8) == 8)
                            {
                                indexcount++;

                                _out.WriteLine(string.Format("v {0} {1} {2}", -cubesize + x,
                                cubesize + y,
                                cubesize + z));

                                _out.WriteLine(string.Format("v {0} {1} {2}", cubesize + x,
                                cubesize + y,
                                cubesize + z));

                                _out.WriteLine(string.Format("v {0} {1} {2}", cubesize + x,
                                cubesize + y,
                                 -cubesize + z));

                                _out.WriteLine(string.Format("v {0} {1} {2}", -cubesize + x,
                                 cubesize + z,
                                 -cubesize + z));
                            }

                            //bottom
                            if ((c.alphamask & 16) == 16)
                            {
                                indexcount++;

                                _out.WriteLine(string.Format("v {0} {1} {2}", -cubesize + x,
                               -cubesize + y,
                               cubesize + z));

                                _out.WriteLine(string.Format("v {0} {1} {2}", cubesize + x,
                               -cubesize + y,
                               cubesize + z));

                                _out.WriteLine(string.Format("v {0} {1} {2}", cubesize + x,
                               -cubesize + y,
                                -cubesize + z));

                                _out.WriteLine(string.Format("v {0} {1} {2}", -cubesize + x,
                                -cubesize + y,
                                -cubesize + z));
                            }

                            //left
                            if ((c.alphamask & 2) == 2)
                            {
                                indexcount++;

                                _out.WriteLine(string.Format("v {0} {1} {2}", cubesize + x,
                                -cubesize + y,
                                -cubesize + z));

                                _out.WriteLine(string.Format("v {0} {1} {2}", cubesize + x,
                                -cubesize + y,
                                cubesize + z));

                                _out.WriteLine(string.Format("v {0} {1} {2}", cubesize + x,
                                cubesize + y,
                                 cubesize + z));

                                _out.WriteLine(string.Format("v {0} {1} {2}", cubesize + x,
                                 cubesize + y,
                                 -cubesize + z));
                            }

                            //right
                            if ((c.alphamask & 4) == 4)
                            {
                                indexcount++;

                                _out.WriteLine(string.Format("v {0} {1} {2}", -cubesize + x,
                                -cubesize + y,
                                -cubesize + z));

                                _out.WriteLine(string.Format("v {0} {1} {2}", -cubesize + x,
                                -cubesize + y,
                                cubesize + z));

                                _out.WriteLine(string.Format("v {0} {1} {2}", -cubesize + x,
                                cubesize + y,
                                 cubesize + z));

                                _out.WriteLine(string.Format("v {0} {1} {2}", -cubesize + x,
                                 cubesize + y,
                                 -cubesize + z));
                            }
                        }
                        for (int i = 1; i < indexcount*4; i += 4)
                        {
                            _out.WriteLine(string.Format("f {0} {1} {2}", i, i +1, i +2));
                            _out.WriteLine(string.Format("f {0} {1} {2}", i, i +2, i +3));
                        }
                    }
                }
            }
        }
    }
}