using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public static class ImportExportUtil
{
    static List<IImporter> importers = new List<IImporter>();
    static List<IExporter> exporters = new List<IExporter>();

    static ImportExportUtil()
    {
        importers.Add(new ImporterQb());

        exporters.Add(new ExporterQb());
        exporters.Add(new ExporterObj());
    }

    public static bool import(string path, out QbModel model)
    {
        foreach (var importer in importers)
        {
            if (path.EndsWith(importer.extension))
            {
                model = importer.read(path);
                QbManager.models.Add(model);
                Client.window.camera.LookAtModel();
                return true;
            }
        }
        model = null;
        return false;
    }

    public static bool export(string extension, string name, string path, QbModel model)
    {
        foreach (var exporter in exporters)
        {
            if (extension.Contains(exporter.extension))
            {
                exporter.write(path, name, model);
                return true;
            }
        }
        return false;
    }
}