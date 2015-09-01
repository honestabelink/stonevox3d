using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public class QbModelMatrixListbox : Widget
    {
        public QbModelMatrixListbox(float width, float height)
            : base(-1)
        {
            ID = GetNextAvailableID();
        }
    }
}
