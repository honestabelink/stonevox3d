using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public interface IExporter
{
    string extension { get; }

    void write(string path, string name, QbModel model);
}
