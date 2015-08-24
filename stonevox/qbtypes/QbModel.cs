using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
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

        public void GenerateVertexBuffers()
        {
            matrices.ForEach(t => t.GenerateVertexBuffers());
        }

        public void FillVertexBuffers()
        {
            matrices.ForEach(t => t.FillVertexBuffers());
        }

        public void Render(Shader shader)
        {
            matrices.ForEach(t => t.Render(shader));
        }

        public void Render()
        {
            matrices.ForEach(t => t.Render());
        }

        public void RenderAll()
        {
            matrices.ForEach(t => t.RenderAll());
        }

        public void RenderAll(Shader shader)
        {
            matrices.ForEach(t => t.RenderAll(shader));
        }
    }
}