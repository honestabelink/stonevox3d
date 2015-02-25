using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public class ExporterObj : IExporter
{
    public string extension
    {
        get { return ".obj"; }
    }

    public void write(string path, string name, QbModel datatype)
    {
    }
}
