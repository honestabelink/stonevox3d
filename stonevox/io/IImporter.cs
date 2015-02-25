using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public interface IImporter
{
    string extension { get; }

    QbModel read(string path);
}
