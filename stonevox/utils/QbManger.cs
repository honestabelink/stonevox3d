using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public static class QbManager
{
    public static int activeindex;
    public static int activematrixindex;

    public static List<QbModel> models = new List<QbModel>();

    public static QbModel getactivemodel()
    {
        return models[models.Count-1];
    }

    public static QbMatrix getactivematrix()
    {
        return getactivemodel().matrices[getactivemodel().activematrix];
    }
}