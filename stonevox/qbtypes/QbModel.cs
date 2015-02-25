using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public class QbModel
{
    public string name;
    public List<QbMatrix> matrices;

    public uint version;
    public uint colorFormat;
    public uint zAxisOrientation;
    public uint compressed;
    public uint visibilityMaskEncoded;
    public int numMatrices { get { return matrices.Count; } }

    public int activematrix;

    public QbMatrix getactivematrix { get { return matrices[activematrix]; } }

    public QbModel()
    {
    }


    public void setmatrixcount(uint number)
    {
        matrices = new List<QbMatrix>();

        for (int i = 0; i < number; i++)
            matrices.Add(new QbMatrix());
    }

    public void generatevertexbuffers()
    {
        matrices.ForEach(t => t.generatevertexbuffers());
    }

    public void fillvertexbuffers()
    {
        matrices.ForEach(t => t.fillvertexbuffers());
    }

    public void render(Shader shader)
    {
        matrices.ForEach(t => t.render(shader));
    }
}
