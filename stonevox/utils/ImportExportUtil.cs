using System.Collections.Generic;

namespace stonevox
{
    public static class ImportExportUtil
    {
        static List<IImporter> importers = new List<IImporter>();
        static List<IExporter> exporters = new List<IExporter>();

        static ImportExportUtil()
        {
            importers.Add(new ImporterQb());
            importers.Add(new ImporterSVP());

            exporters.Add(new ExporterQb());
            exporters.Add(new ExporterSVP());
            exporters.Add(new ExporterObj());
        }

        public static bool Import(string path, bool setActive = true)
        {
            foreach (var importer in importers)
            {
                if (path.EndsWith(importer.extension))
                {
                    var model = importer.read(path);
                    //model.Sort();

                    Singleton<QbManager>.INSTANCE.AddModel(model, setActive);
                    if (setActive)
                        Singleton<Camera>.INSTANCE.LookAtModel();

                    // hacks
                    if (!Client.window.isfocused)
                    {
                        Program.SetForegroundWindow(Client.window.WindowInfo.Handle);
                    }
                    return true;
                }
            }
            return false;
        }

        public static bool Export(string extension, string name, string path, QbModel model)
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
}