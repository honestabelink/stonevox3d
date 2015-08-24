using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public interface IRenderer
    {
        void Render(QbModel model);
    }
}
