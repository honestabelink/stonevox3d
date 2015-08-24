using System;
using System.Collections.Generic;
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

        public void write(string path, string name, QbModel dataType)
        {
        }
    }
}